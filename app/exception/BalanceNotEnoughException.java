package exception;

public class BalanceNotEnoughException extends BusinessException {
	
	private static final long serialVersionUID = -5831629339956286854L;

	public BalanceNotEnoughException(String message) {
		super(message);
	}
}
