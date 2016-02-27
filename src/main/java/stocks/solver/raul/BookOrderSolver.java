package stocks.solver.raul;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Primary entry point into the program to solve the book order problem.
 * @author Raul
 *
 */
public class BookOrderSolver {

	private static final Logger log = Logger.getLogger(BookOrderSolver.class.getName());
	
	/**
	 * Main function to take in a target size
	 * @param args Contains target size variable
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Charset charset;
		BufferedWriter bufferedWriter = null;
		
		if (args.length == 0) {
			System.out.println("usage: input target-size");
			return;
		}
		
		int target;
		
		try{
			target = Integer.valueOf(args[0]);	
		} catch(NumberFormatException nfe){
			log.log(Level.SEVERE, "Bad target input, cannot continue", nfe);
			return;
		}
		
		MasterBook masterBook = new MasterBook(target);
		
		try{			
			String previousBidsResultNoTimestamp = "";
			String previousAsksResultNoTimestamp = "";
			boolean canStartBidCalc = false;
			boolean canStartAskCalc = false;
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			String input;
			Long timestamp;
			
			Calculator calculator = new Calculator();
			
			while( (input=bufferedReader.readLine()) != null && input.length() != 0){
				String[] parts = input.split("\\s+");
				
				Order order;
				try {
					timestamp = Long.valueOf(parts[0]);
					order = new Order(parts);

					// Need to set the side explicitly for reduce orders
					if(order.getOrderAction().equals(OrderAction.REDUCE)){
						if(masterBook.getLiveMarketBids().containsKey(order.getOrderId())){
							order.setSide(masterBook.getLiveMarketBids().get(order.getOrderId()).getSide());
						} else if(masterBook.getLiveMarketAsks().containsKey(order.getOrderId())){
							order.setSide(masterBook.getLiveMarketAsks().get(order.getOrderId()).getSide());
						} else{
							throw new ReduceMarketLookupException("Cannot find order to reduce: " + order.toString());
						}
					}
					
					UpdateBookResult updateBookResult = masterBook.updateBookStructures(order);
					
					if(masterBook.getTotalBidSize() >= target && canStartBidCalc == false){
						canStartBidCalc = true;
					}
					
					if(masterBook.getTotalAskSize() >= target && canStartAskCalc == false){
						canStartAskCalc = true;
					}
					
					if(canStartBidCalc && order.getSide().equals(OrderSide.B) && updateBookResult.recalculateProfit()){
							CalculatorResult calcResult = calculator.calculate(masterBook, target, order);
							String stringToWrite = calcResult.getCompleteOutputResult();
							// Only output unique calculations, IE, don't output the same result twice
							if(!previousBidsResultNoTimestamp.equals(calcResult.getOutputResultNoTimestamp())){
								System.out.print(stringToWrite);
								System.out.print("\n");
							}
							
				   		  previousBidsResultNoTimestamp = calcResult.getOutputResultNoTimestamp();
					}
					
					if(canStartAskCalc && order.getSide().equals(OrderSide.S) && updateBookResult.recalculateCost()){
							CalculatorResult calcResult = calculator.calculate(masterBook, target, order);
							String stringToWrite = calcResult.getCompleteOutputResult();
							// Only output unique calculations, IE, don't output the same result twice
					   		if(!previousAsksResultNoTimestamp.equals(calcResult.getOutputResultNoTimestamp())){
					   			System.out.print(stringToWrite);
								System.out.print("\n");
					   		}				   		
				   		  previousAsksResultNoTimestamp = calcResult.getOutputResultNoTimestamp();
					}
				} catch (ParsingException e) {
					log.log(Level.SEVERE, "Bad market order, skipping it", e);
				} catch(NumberFormatException nfe){
					log.log(Level.SEVERE, "Bad timestamp in input, skipping it", nfe);
				} catch (ReduceMarketLookupException e) {
					log.log(Level.SEVERE, "Bad market reduction order, skipping it", e);
				}
				// Will assume on bad market input, to just skip that order.  This would need to be agreed upon by the team.
			}
		} catch(IOException io){
			io.printStackTrace();
		} finally {
			if(bufferedWriter != null){
				bufferedWriter.flush();				
			}
		}
	}
}
