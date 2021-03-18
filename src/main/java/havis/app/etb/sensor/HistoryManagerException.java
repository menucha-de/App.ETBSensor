package havis.app.etb.sensor;

public class HistoryManagerException extends Exception {

	private static final long serialVersionUID = 1L;

	public HistoryManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public HistoryManagerException(Throwable cause) {
		super(cause);
	}
}