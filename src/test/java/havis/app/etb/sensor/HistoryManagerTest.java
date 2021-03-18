package havis.app.etb.sensor;

import static org.junit.Assert.*;
import static mockit.Deencapsulation.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class HistoryManagerTest {

	HistoryManager historyManager;
	long timestamp1 = 0, timestamp2 = 0;
	
	@Before
	public void setup() throws HistoryManagerException {		
		this.historyManager = new HistoryManager();
		
		historyManager.clear();
		/* Generate 100 records */
		for (int i = 0 ; i < 100; i++) {
			HistoryEntry historyEntry = new HistoryEntry();
			historyEntry.setEpc(UUID.randomUUID().toString().replaceAll("-",""));
			historyEntry.setTimestamp(new Date().getTime());
			historyEntry.setValue((float)(Math.random() * 100.0));
			historyEntry.setUnit("°C");
			historyManager.add(historyEntry);
			
			if (i == 20) timestamp1 = historyEntry.getTimestamp();
			else if (i == 49) timestamp2 = historyEntry.getTimestamp();
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testHistoryManager() throws SQLException {
		Connection connection = getField(this.historyManager, "connection");
		assertNotNull(connection);
		assertTrue(!connection.isClosed());
	}

	@Test
	public void testClear() throws HistoryManagerException {
		assertEquals(100, historyManager.size());
		historyManager.clear();		
		assertEquals(0, historyManager.size());
	}

	@Test
	public void testSize() throws HistoryManagerException {
		assertEquals(100, historyManager.size());
	}

	@Test
	public void testGetEntries() throws HistoryManagerException {
		List<HistoryEntry> historyEntries = historyManager.getEntries(30, 20);
		assertEquals(30, historyEntries.size());
		assertEquals(timestamp1, historyEntries.get(0).getTimestamp());
		assertEquals(timestamp2, historyEntries.get(historyEntries.size()-1).getTimestamp());
		
		List<HistoryEntry> entriesSince = historyManager.getEntries(timestamp2);		
		List<HistoryEntry> allEntries = historyManager.getEntries(-1, 0);
		assertEquals(100,  allEntries.size());		
		assertEquals(timestamp2, entriesSince.get(0).getTimestamp());
		assertEquals(allEntries.get(allEntries.size()-1).getTimestamp(), entriesSince.get(entriesSince.size()-1).getTimestamp());		
	}

	@Test
	public void testAdd() throws HistoryManagerException {
		assertEquals(100, historyManager.size());
		setField(Environment.class, "MAX_RECORD_AGE", 100);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/* Generate 50 records */
		for (int i = 0 ; i < 50; i++) {
			HistoryEntry historyEntry = new HistoryEntry();
			historyEntry.setEpc(UUID.randomUUID().toString().replaceAll("-",""));
			historyEntry.setTimestamp(new Date().getTime());
			historyEntry.setValue((float)(Math.random() * 100.0));			
			historyManager.add(historyEntry);
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		
		long now = new Date().getTime();
		long oldestRecordAge = now-historyManager.getEntries(1, 0).get(0).timestamp;
		assertTrue(oldestRecordAge - /*tolerance*/ 10 < 100);
		
		setField(Environment.class, "MAX_RECORD_AGE", 1000);
	}

	@Test
	public void testClose() throws HistoryManagerException, SQLException {
		Connection connection = getField(this.historyManager, "connection");
		this.historyManager.close();
		assertTrue(connection.isClosed());
	}
	
	@Test
	public void testGetTimestamp() {
		long ts1 = new Date().getTime();
		long ts2 = this.historyManager.getTimestamp();
		assertTrue(ts2 - ts1 >= 0 && ts2 - ts1 <= 10);
	}
	
	@Test
	public void testCreate() {
		
		long ts = new Date().getTime();
		
		HistoryEntry he = this.historyManager.create("aabbccdd", 42.23f, "F");
		assertEquals("aabbccdd", he.getEpc());
		assertEquals((Float)42.23f, (Float)he.getValue());
		assertEquals("F", he.getUnit());
		assertTrue(he.getTimestamp()-ts >= 0 && he.getTimestamp()-ts <= 10);
		
	}
}
