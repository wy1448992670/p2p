package exception;

public class BalanceIsEnoughException extends BusinessException {
	
	private static final long serialVersionUID = -3306632349331788746L;
	
	public BalanceIsEnoughException(String message) {
		super(message);
	}
}
