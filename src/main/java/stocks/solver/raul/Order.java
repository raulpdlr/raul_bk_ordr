package stocks.solver.raul;

/**
 * Represents a market order
 * @author Raul
 *
 */
public class Order {

	private long timestamp;
	private OrderAction orderAction;
	private String orderId;
	private OrderSide side;
	private double price;
	private Integer size;
	
	/**
	 * Constructor for Order
	 * @param parts Input needed to construct Order
	 * @throws ParsingException Thrown on bad input
	 */
	public Order(String[] parts) throws ParsingException{
		
		String partsString;
		
		if(parts.length != 6 && parts.length != 4){
			partsString = buildString(parts);
			throw new ParsingException("Bad market order: " + partsString);
		} else if(parts.length == 6 && !parts[1].equals("A")){
			partsString = buildString(parts);
			throw new ParsingException("Bad market order: " + partsString);
		} else if(parts.length == 4 && !parts[1].equals("R")){
			partsString = buildString(parts);
			throw new ParsingException("Bad market order: " + partsString);
		}
		
		try{
			this.setTimestamp(Long.valueOf(parts[0]));
			
			if(parts[1].equals("A")){
				this.setOrderAction(OrderAction.ADD);
				this.setSide(OrderSide.getOrderSide(parts[3]));
				this.setPrice(Double.valueOf(parts[4]));
				this.setSize(Integer.valueOf(parts[5]));
			} else if(parts[1].equals("R")){
				this.setOrderAction(OrderAction.REDUCE);
				this.setSize(Integer.valueOf(parts[3]));
			}  

			this.setOrderId(parts[2]);			
		} catch(Exception e){
			partsString = buildString(parts);
			throw new ParsingException("Bad market order: " + partsString);
		}
	}
	
	@Override
	public String toString(){
		String stringOrder;
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append("\n")
					 .append("timestamp: ")
					 .append(timestamp)
					 .append("\n")
					 .append("orderAction: ")
					 .append(orderAction)
					 .append("\n")
					 .append("orderId: ")
					 .append(orderId)
					 .append("\n")
					 .append("side: ")
					 .append(side);
		
		if(this.orderAction.equals(OrderAction.ADD)){
			 stringBuilder.append("\n")
			 			  .append("price: ")
						  .append(price)
						  .append("\n")
						  .append("size: ")
						  .append(size);
		}
					 
		
		
		return stringBuilder.toString();
	}
	
	/**
	 * Helper function for exceptions
	 * @param parts String array of the order
	 * @return
	 */
	private String buildString(String[] parts){
		StringBuilder sb = new StringBuilder();
		for(String s : parts){
			if(!s.isEmpty()){
				sb.append(s);
				sb.append(" ");
			}
		}	
		return sb.toString();
	}
	
	public void reduceOrder(Integer amount){
		setSize(getSize() - amount);
	}

	long getTimestamp() {
		return timestamp;
	}

	void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	OrderAction getOrderAction() {
		return orderAction;
	}

	void setOrderAction(OrderAction orderAction) {
		this.orderAction = orderAction;
	}

	String getOrderId() {
		return orderId;
	}

	void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	OrderSide getSide() {
		return side;
	}

	void setSide(OrderSide side) {
		this.side = side;
	}

	double getPrice() {
		return price;
	}

	void setPrice(double price) {
		this.price = price;
	}

	Integer getSize() {
		return size;
	}

	void setSize(Integer size) {
		this.size = size;
	}
}
