package stocks.solver.raul;

public enum OrderSide {
	B,
	S,
	ORDER_SIDE_ERROR;

	public static OrderSide getOppositeSide(OrderSide orderSide){
		if(orderSide.equals(OrderSide.B)){
			return OrderSide.S;
		} else if(orderSide.equals(OrderSide.S)){
			return OrderSide.B;
		}
		return OrderSide.ORDER_SIDE_ERROR;
	}
	
	public static OrderSide getOrderSide(String side){
		if(side.equals("B")){
			return OrderSide.B;
		} else if(side.equals("S")){
			return OrderSide.S;
		}
		
		return OrderSide.ORDER_SIDE_ERROR;
	}
	
}
