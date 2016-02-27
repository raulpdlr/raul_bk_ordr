package stocks.solver.raul;

import java.util.HashMap;
import java.util.Map;

public class PriceLevelShares {

	int totalShares = 0;
	int sharesUsedInLastCalc = 0;
	
	public void addShares(int shares){
		totalShares = totalShares + shares;
	}
	
	public void reduceShares(int shares){
		totalShares = totalShares - shares;
	}
}
