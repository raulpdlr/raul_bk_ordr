package stocks.solver.raul;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Class that contains a calculation function to process the profit or loss for a {@link MasterBook} object and target.
 * @author Raul
 *
 */
public class Calculator {
	
	/**
	 * Function to calculate profit or cost
	 * @param masterBook MasterBook containing market structures on which to perform the calculation
	 * @param target Target size for the calculation
	 * @param order	Latest market order on which to operate
	 * @return CalculatorResult containing the required output for caller
	 */
	public CalculatorResult calculate(MasterBook masterBook, int target, Order order){
		
		NumberFormat formatter = new DecimalFormat("#0.00");
		CalculatorResult calculatorResult = new CalculatorResult();
		Map<Double, PriceLevelShares> priceLevels = null;
		
		String profitOrCostString = "";
		String resultNoTimestamp = "";
		
		double calculatedResultDouble = 0;
		// The BigDecimal version of profitOrCost will be used for final formatting, using 
		// the double version for calculations since operations on primitive double is much faster
		// than on BigDecimal
		BigDecimal calculatedResultBigDecimal = BigDecimal.ZERO;
		
		StringBuilder sb = new StringBuilder();		
		
		int remainingShares = target;				
		boolean doneWithCalc = false;
		
		if(order.getSide().equals(order.getSide().B)) {
			priceLevels = masterBook.getBidPriceLevels();
		} else if(order.getSide().equals(order.getSide().S)) {
			priceLevels = masterBook.getAskPriceLevels();			
		}
		
		// Not enough shares to calculate result, return NA
		if(order.getSide().equals(order.getSide().B) && masterBook.getTotalBidSize() < target){
			resultNoTimestamp = "S NA";
			calculatorResult.setOutputResultNoTimestamp(resultNoTimestamp);
			calculatorResult.setCompleteOutputResult(order.getTimestamp() + " " + resultNoTimestamp);
			return(calculatorResult);
		} else if(order.getSide().equals(order.getSide().S) && masterBook.getTotalAskSize() < target){
			resultNoTimestamp = "B NA";
			calculatorResult.setOutputResultNoTimestamp(resultNoTimestamp);
			calculatorResult.setCompleteOutputResult(order.getTimestamp() + " " + resultNoTimestamp);
			return(calculatorResult);
		}

		if(order.getSide().equals(order.getSide().B)){
			masterBook.getBidPriceLevelsUsed().clear();					
		} else if(order.getSide().equals(order.getSide().S)){
			masterBook.getAskPriceLevelsUsed().clear();
		}
		
		// Walk priceLevels until target shares filled
		Iterator iterPriceLevels = priceLevels.entrySet().iterator();
		while(iterPriceLevels.hasNext()){
			Map.Entry<Double, PriceLevelShares> entry = (Map.Entry<Double, PriceLevelShares>) iterPriceLevels.next();
			double currentPriceAtLevel = entry.getKey();
			PriceLevelShares priceLevelShares = entry.getValue();
			
			Integer sharesAtPriceLevel = priceLevelShares.totalShares;
			
			if(sharesAtPriceLevel >= remainingShares){
				calculatedResultDouble += remainingShares * currentPriceAtLevel;
				
				if(order.getSide().equals(order.getSide().B)){
					masterBook.setLowestBidPriceUsed(currentPriceAtLevel);
					masterBook.getBidPriceLevelsUsed().put(entry.getKey(), entry.getValue());
				} else if(order.getSide().equals(order.getSide().S)){
					masterBook.setHighestAskPriceUsed(currentPriceAtLevel);					
					masterBook.getAskPriceLevelsUsed().put(entry.getKey(), entry.getValue());
				}					

				priceLevelShares.sharesUsedInLastCalc = remainingShares; 
				
				doneWithCalc = true;
				
				break;
			} else {
				calculatedResultDouble += sharesAtPriceLevel * currentPriceAtLevel;

				remainingShares = remainingShares - sharesAtPriceLevel;
				priceLevelShares.sharesUsedInLastCalc = sharesAtPriceLevel;
				
				if(order.getSide().equals(order.getSide().B)){
					masterBook.getBidPriceLevelsUsed().put(entry.getKey(), entry.getValue());
				} else if(order.getSide().equals(order.getSide().S)){
					masterBook.getAskPriceLevelsUsed().put(entry.getKey(), entry.getValue());
				}
			}			
			
			if(doneWithCalc){
				break;
			}
		}

		calculatedResultBigDecimal = new BigDecimal(calculatedResultDouble);
		calculatedResultBigDecimal = calculatedResultBigDecimal.setScale(2, RoundingMode.HALF_UP);

		sb.append(order.getSide().getOppositeSide(order.getSide()).toString());
		sb.append(" ");
		sb.append(formatter.format(calculatedResultBigDecimal));
		resultNoTimestamp = sb.toString();
		sb.insert(0, " ");
		sb.insert(0, order.getTimestamp());
		profitOrCostString = sb.toString();
		
		calculatorResult.setOutputResultNoTimestamp(resultNoTimestamp);
		calculatorResult.setCompleteOutputResult(profitOrCostString);

		return(calculatorResult);		
	}
}
