package stocks.solver.raul;

/**
 * Class to hold output from calculation done in {@link Calculator}
 * @author Raul
 *
 */
public class CalculatorResult {
	
	private String outputResultNoTimestamp;
	private String completeOutputResult;
	
	String getOutputResultNoTimestamp() {
		return outputResultNoTimestamp;
	}
	void setOutputResultNoTimestamp(String outputResultNoTimestamp) {
		this.outputResultNoTimestamp = outputResultNoTimestamp;
	}
	String getCompleteOutputResult() {
		return completeOutputResult;
	}
	void setCompleteOutputResult(String completeOutputResult) {
		this.completeOutputResult = completeOutputResult;
	}	
}
