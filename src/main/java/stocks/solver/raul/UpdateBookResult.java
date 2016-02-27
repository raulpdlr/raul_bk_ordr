package stocks.solver.raul;

/**
 * Class used to maintain the results of whether or not a profit or cost
 * calculation is needed after an update of the book order structures.
 * @author Raul
 *
 */
public class UpdateBookResult {
	private boolean recalculateProfit = false;
	private boolean recalculateCost = false;
	
	boolean recalculateProfit() {
		return recalculateProfit;
	}

	void setRecalculateProfit(boolean recalculateProfit) {
		this.recalculateProfit = recalculateProfit;
	}

	boolean recalculateCost() {
		return recalculateCost;
	}

	void setRecalculateCost(boolean recalculateCost) {
		this.recalculateCost = recalculateCost;
	}
}
