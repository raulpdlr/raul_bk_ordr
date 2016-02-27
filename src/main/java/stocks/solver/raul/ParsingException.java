package stocks.solver.raul;

/**
 * Exception to be used if parsing error occurs on input, should be logged.
 * @author Raul
 *
 */
public class ParsingException extends Exception {

	public ParsingException(final String message){
		super(message);
	}
}
