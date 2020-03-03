package exception;

public class BusinessException extends Exception{

	private static final long serialVersionUID = 2201800548165425255L;
	
	public BusinessException(String message) {
        super(message);
    }
}
