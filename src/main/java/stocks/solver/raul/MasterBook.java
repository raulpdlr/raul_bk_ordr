package stocks.solver.raul;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class containing all the needed data structures to represent the market and functions to maintain them
 * @author Raul
 *
 */
public class MasterBook {
	// Maps represent all current bids or asks in the market
	private Map<String, Order> liveMarketBids = new HashMap<String, Order>();
	private Map<String, Order> liveMarketAsks = new HashMap<String, Order>();
	
	// Maps to maintain the market prices and the number of shares total and used (in latest calculation) at each price
	// Want bids sorted from highest to lowest and asks sorted from lowest to highest
	private Map<Double, PriceLevelShares> bidPriceLevels = new TreeMap<Double, PriceLevelShares>(Collections.reverseOrder());
	private Map<Double, PriceLevelShares> askPriceLevels = new TreeMap<Double, PriceLevelShares>();

	// Maps to maintain which price levels are used for profit/cost calculation.  Primarily used to determine whether or not a 
	// profit/cost calculation is really needed after a reduce order.
	private Map<Double, PriceLevelShares> bidPriceLevelsUsed = new HashMap<Double, PriceLevelShares>();
	private Map<Double, PriceLevelShares> askPriceLevelsUsed = new HashMap<Double, PriceLevelShares>();
	
	private double lowestBidPriceUsed = 0.0;
	private double highestAskPriceUsed = Double.MAX_VALUE;
	
	private int totalBidSize = 0;
	private int totalAskSize = 0;
	
	private int target = -1;
	
	/**
	 * Constructor for MasterBook
	 * @param target Number of target shares
	 */
	public MasterBook(int target){
		this.setTarget(target);
	}
	
	/**
	 * Function will update bid or ask priceLevels map based on a market order to add shares
	 * @param order Incoming order on which to operate.
	 */
	private void updatePriceLevelsViaAdd(Order order){

		Map<Double, PriceLevelShares> priceLevels = null;
		
		if(order.getSide().equals(OrderSide.B)){
			priceLevels = bidPriceLevels;
		} else if(order.getSide().equals(OrderSide.S)){
			priceLevels = askPriceLevels;
		}

		PriceLevelShares priceLevelShares = priceLevels.get(order.getPrice());
		
		if(priceLevelShares != null){
			priceLevelShares.addShares(order.getSize());
		} else{
			priceLevelShares = new PriceLevelShares();
			priceLevelShares.addShares(order.getSize());
			priceLevels.put(order.getPrice(), priceLevelShares);	
		}
	}
	
	/**
	 * Will maintain liveMarketMap and priceLevels map
	 * @param orderRequest Order on which to operate
	 * @param liveMarketMap Bids or asks live market map
	 * @param priceLevelSharesMap Bid or ask price levels map
	 */
	private void processReduction(Order orderRequest, Map<String, Order> liveMarketMap, Map<Double, PriceLevelShares> priceLevelSharesMap){	
		Order orderToReduce = liveMarketMap.get(orderRequest.getOrderId());
		
		int shareReduction;
		
		if(orderToReduce.getSize() <= orderRequest.getSize()){
			shareReduction = orderToReduce.getSize();
			liveMarketMap.remove(orderRequest.getOrderId());
		} else {
			shareReduction = orderRequest.getSize();			
		}

		orderToReduce.reduceOrder(shareReduction);

		if(orderRequest.getSide().equals(OrderSide.B)){
			setTotalBidSize(totalBidSize - shareReduction);			
		} else if(orderRequest.getSide().equals(OrderSide.S)){
			setTotalAskSize(totalAskSize - shareReduction);
		}
		
		PriceLevelShares priceLevelShares = priceLevelSharesMap.get(orderToReduce.getPrice());
		priceLevelShares.reduceShares(shareReduction);
		
		// No shares left at this PriceLevelShares, remove it
		if(priceLevelShares.totalShares == 0){
			priceLevelSharesMap.remove(orderToReduce.getPrice());
		}
	}
	
	/**
	 * Function to maintain the data structures for the market.
	 * @param orderRequest Incoming order on which to operate
	 * @return UpdateBookResult Object to let caller know whether a profit or cost calculation needs to be done.
	 */
	public UpdateBookResult updateBookStructures(Order orderRequest){
	
		UpdateBookResult updateBookResult = new UpdateBookResult();
		
		if(orderRequest.getOrderAction().equals(OrderAction.ADD)){
			updatePriceLevelsViaAdd(orderRequest);
			
			if(orderRequest.getSide().equals(OrderSide.B)){
				liveMarketBids.put(orderRequest.getOrderId(), orderRequest);
				
				int priorBidSize = totalBidSize;
				setTotalBidSize(totalBidSize + orderRequest.getSize());

				//  Latest order's price is better than the lowest bid price used, there is more profit to be made
				if(orderRequest.getPrice() > lowestBidPriceUsed){
					updateBookResult.setRecalculateProfit(true);
				}			
								
				// Shares added to market put us at or over target where before we were below target, must recalculate profit
				if(priorBidSize < target && totalBidSize >= target){
					updateBookResult.setRecalculateProfit(true);
				}
			} else if(orderRequest.getSide().equals(OrderSide.S)){
				liveMarketAsks.put(orderRequest.getOrderId(), orderRequest);
				
				int priorAskSize = totalAskSize;
				setTotalAskSize(totalAskSize + orderRequest.getSize());

				//  Latest order's price is better than the highest ask price used, there is money to be saved
				if(orderRequest.getPrice() < highestAskPriceUsed){
					updateBookResult.setRecalculateCost(true);	
				}
				
				// Shares added to market put us at or over target where before we were below target, must recalculate cost
				if(priorAskSize < target && totalAskSize >= target){
					updateBookResult.setRecalculateCost(true);
				}
			}
		} else if(orderRequest.getOrderAction().equals(OrderAction.REDUCE)){
			// Must ensure the order to reduce actually exists in the market!
			if(liveMarketBids.containsKey(orderRequest.getOrderId())){
				Order orderToReduce = liveMarketBids.get(orderRequest.getOrderId());
				processReduction(orderRequest, liveMarketBids, bidPriceLevels);

				// Reduction in market order *may* cause a recalculation in profit to occur
				if(orderToReduce.getPrice() >= lowestBidPriceUsed){				
					if(orderToReduce.getPrice() == lowestBidPriceUsed){
						PriceLevelShares priceLevelSharesAtOrder = bidPriceLevelsUsed.get(orderToReduce.getPrice());
						// Reduction order will cause lowest bid price level used to not have enough shares between it and
						// all the other price levels above to fulfill the target, must recalculate profit.  Otherwise
						// no recalculation needed.
						if(priceLevelSharesAtOrder.totalShares < priceLevelSharesAtOrder.sharesUsedInLastCalc){
							updateBookResult.setRecalculateProfit(true);
						}						
					} else{
						updateBookResult.setRecalculateProfit(true);						
					}
				}
			} else if(liveMarketAsks.containsKey(orderRequest.getOrderId())){
				Order orderToReduce = liveMarketAsks.get(orderRequest.getOrderId());
				processReduction(orderRequest, liveMarketAsks, askPriceLevels);

				// Reduction in market order *may* cause a recalculation in cost to occur
				if(orderToReduce.getPrice() <= highestAskPriceUsed){					
					if(orderToReduce.getPrice() ==  highestAskPriceUsed){
						PriceLevelShares priceLevelAtOrder = askPriceLevelsUsed.get(orderToReduce.getPrice());
						// Reduction order will cause highest ask price level used to not have enough shares between it and
						// all the other price levels below it to fulfill the target, must recalculate cost.  Otherwise
						// no recalculation needed.
						if(priceLevelAtOrder.totalShares < priceLevelAtOrder.sharesUsedInLastCalc){
							updateBookResult.setRecalculateCost(true);
						}						
					} else{
						updateBookResult.setRecalculateCost(true);						
					}					
				}			
			}
		}
		
		return(updateBookResult);
	}

	Map<String, Order> getLiveMarketBids() {
		return liveMarketBids;
	}

	void setLiveMarketBids(Map<String, Order> liveMarketBids) {
		this.liveMarketBids = liveMarketBids;
	}

	Map<String, Order> getLiveMarketAsks() {
		return liveMarketAsks;
	}

	void setLiveMarketAsks(Map<String, Order> liveMarketAsks) {
		this.liveMarketAsks = liveMarketAsks;
	}

	Map<Double, PriceLevelShares> getBidPriceLevels() {
		return bidPriceLevels;
	}

	void setBidPriceLevels(Map<Double, PriceLevelShares> bidPriceLevels) {
		this.bidPriceLevels = bidPriceLevels;
	}

	Map<Double, PriceLevelShares> getAskPriceLevels() {
		return askPriceLevels;
	}

	void setAskPriceLevels(Map<Double, PriceLevelShares> askPriceLevels) {
		this.askPriceLevels = askPriceLevels;
	}

	Map<Double, PriceLevelShares> getBidPriceLevelsUsed() {
		return bidPriceLevelsUsed;
	}

	void setBidPriceLevelsUsed(Map<Double, PriceLevelShares> bidPriceLevelsUsed) {
		this.bidPriceLevelsUsed = bidPriceLevelsUsed;
	}

	Map<Double, PriceLevelShares> getAskPriceLevelsUsed() {
		return askPriceLevelsUsed;
	}

	void setAskPriceLevelsUsed(Map<Double, PriceLevelShares> askPriceLevelsUsed) {
		this.askPriceLevelsUsed = askPriceLevelsUsed;
	}

	double getLowestBidPriceUsed() {
		return lowestBidPriceUsed;
	}

	void setLowestBidPriceUsed(double lowestBidPriceUsed) {
		this.lowestBidPriceUsed = lowestBidPriceUsed;
	}

	double getHighestAskPriceUsed() {
		return highestAskPriceUsed;
	}

	void setHighestAskPriceUsed(double highestAskPriceUsed) {
		this.highestAskPriceUsed = highestAskPriceUsed;
	}

	int getTotalBidSize() {
		return totalBidSize;
	}

	void setTotalBidSize(int totalBidSize) {
		this.totalBidSize = totalBidSize;
	}

	int getTotalAskSize() {
		return totalAskSize;
	}

	void setTotalAskSize(int totalAskSize) {
		this.totalAskSize = totalAskSize;
	}



	int getTarget() {
		return target;
	}

	void setTarget(int target) {
		this.target = target;
	}
	
}
