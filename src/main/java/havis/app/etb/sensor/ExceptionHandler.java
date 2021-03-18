package havis.app.etb.sensor;

import java.util.HashSet;
import java.util.Set;

public class ExceptionHandler {
	private Set<String> reportedExceptions = new HashSet<>();
	
	public synchronized boolean handle(String errorMessage) {		
		if (!reportedExceptions.contains(errorMessage)) {
			reportedExceptions.add(errorMessage);
			return true;						
		}		
		return false;		
	}
	
	public synchronized void reset() {
		this.reportedExceptions.clear();
	}

	public void reset(String errorMessage) {
		this.reportedExceptions.remove(errorMessage);
		
	}
	
}
