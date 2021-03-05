package havis.custom.harting.etb.sensor;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ExceptionHandlerTest {

	@Test
	public void testHandle() {
		
		ExceptionHandler eh = new ExceptionHandler();
		assertEquals(true, eh.handle("E1"));
		assertEquals(false, eh.handle("E1"));
		assertEquals(true, eh.handle("E2"));
	}

	@Test
	public void testReset() {
		ExceptionHandler eh = new ExceptionHandler();
		assertEquals(true, eh.handle("E1"));
		assertEquals(true, eh.handle("E2"));
		eh.reset();
		assertEquals(true, eh.handle("E1"));
		assertEquals(true, eh.handle("E2"));
	}

	@Test
	public void testResetString() {
		ExceptionHandler eh = new ExceptionHandler();
		assertEquals(true, eh.handle("E1"));
		assertEquals(true, eh.handle("E2"));
		eh.reset("E1");
		assertEquals(true, eh.handle("E1"));
		assertEquals(false, eh.handle("E2"));
	}	
}
