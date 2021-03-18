package havis.app.etb.sensor;

public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException(String message) {
		super(message);
	}
	
}
