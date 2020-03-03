package exception;

public class ApiStatusUnknownException extends BusinessException {
	private static final long serialVersionUID = -3269325096786337362L;

	public ApiStatusUnknownException(String message) {
        super(message);
    }
}
