package stocks.solver.raul;

/**
 * Exception to represent situation where a market order is expected to be found but not
 * in the collection of market bids or asks for a reduce operation.
 * @author Raul
 *
 */
public class ReduceMarketLookupException extends Exception {

	public ReduceMarketLookupException(final String message){
		super(message);
	}
	
}
