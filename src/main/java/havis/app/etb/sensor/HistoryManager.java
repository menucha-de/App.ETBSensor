package havis.app.etb.sensor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvResultSetWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

public class HistoryManager {

	private final static int TIMESTAMP = 1, EPC = 2, VALUE = 3, UNIT = 4;
	private final static String CLEAR = "DELETE FROM history";
	private final static String SIZE = "SELECT COUNT(id) FROM history";
	private final static String SELECT = "SELECT timestamp, epc, value, unit FROM history";
	private final static String ORDER = "ORDER BY timestamp";
	private final static String INSERT = "INSERT INTO history (timestamp, epc, value, unit) VALUES (?, ?, ?, ?)";
	private final static String CLEANUP = "DELETE FROM history WHERE timestamp <= ";

	private Connection connection;

	private final static CellProcessor processor = new CellProcessor() {
		@SuppressWarnings("unchecked")
		@Override
		public String execute(Object value, CsvContext context) {
			if (value instanceof Clob) {
				Clob clob = (Clob) value;
				try {
					try (InputStream stream = clob.getAsciiStream()) {
    					byte[] bytes = new byte[stream.available()];
    					stream.read(bytes);
    					return new String(bytes, StandardCharsets.UTF_8);
					}
				} catch (Exception e) {
					// log.log(Level.FINE, "Failed to read column data", e);
				}
			}
			return null;
		}
	};	
	
	public HistoryManager() throws HistoryManagerException {
		try {
			connection = DriverManager.getConnection(Environment.JDBC_URL, Environment.JDBC_USERNAME, Environment.JDBC_PASSWORD);
		} catch (SQLException e) {
			throw new HistoryManagerException("Failed to get connection", e);
		}
	}

	public synchronized int clear() throws HistoryManagerException {
		try (Statement stmt = connection.createStatement()) {
			return stmt.executeUpdate(CLEAR);
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized int size() throws HistoryManagerException {
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SIZE)) {
			if (rs.next())
				return rs.getInt(1);
			return 0;
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized List<HistoryEntry> getEntries(int limit, int offset) throws HistoryManagerException {
		List<HistoryEntry> result = new ArrayList<>();

		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SELECT + " " + ORDER + " limit " + limit + " offset " + offset)) {
			while (rs.next()) {
				HistoryEntry historyEntry = new HistoryEntry();
				historyEntry.setTimestamp(rs.getLong(TIMESTAMP));
				historyEntry.setEpc(rs.getString(EPC));								
				historyEntry.setValue(rs.getFloat(VALUE));
				historyEntry.setUnit(rs.getString(UNIT));
								
				result.add(historyEntry);
			}
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
		return result;
	}
	
	public synchronized List<HistoryEntry> getEntries(long since) throws HistoryManagerException {
		List<HistoryEntry> result = new ArrayList<>();

		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SELECT + " WHERE timestamp >= " + since + " " + ORDER)) {
			while (rs.next()) {
				HistoryEntry historyEntry = new HistoryEntry();
				historyEntry.setTimestamp(rs.getLong(TIMESTAMP));
				historyEntry.setEpc(rs.getString(EPC));								
				historyEntry.setValue(rs.getFloat(VALUE));
				historyEntry.setUnit(rs.getString(UNIT));
								
				result.add(historyEntry);
			}
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
		return result;
	}

	public long getTimestamp() {
		return new Date().getTime();
	}
	
	private void deleteRecordsBefore(long timestamp) throws SQLException {
		if (timestamp > 0)
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate(CLEANUP + timestamp);
			}
	}

	public synchronized void add(HistoryEntry entry) throws HistoryManagerException {
		try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
			stmt.setLong(TIMESTAMP, entry.getTimestamp());
			stmt.setString(EPC, entry.getEpc());
			stmt.setFloat(VALUE, entry.getValue());
			stmt.setString(UNIT, entry.getUnit());
			stmt.execute();			
			deleteRecordsBefore(new Date().getTime() - Environment.MAX_RECORD_AGE);
			
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}
	
	public HistoryEntry create(String epc, float value, String unit) {
		HistoryEntry he = new HistoryEntry();
		he.setEpc(epc);
		he.setTimestamp(new Date().getTime());
		he.setUnit(unit);
		he.setValue(value);
		return he;
	}
	

	public synchronized void close() throws HistoryManagerException {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}
	
	
	public synchronized void marshal(Writer writer) throws SQLException, IOException, HistoryManagerException {
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(SELECT + " limit " + size() + " offset " + 0)) {
			ResultSetMetaData data = rs.getMetaData();
			CellProcessor[] processors = new CellProcessor[data.getColumnCount()];
			for (int i = 0; i < data.getColumnCount(); i++)
				if (data.getColumnType(i + 1) == Types.CLOB)
					processors[i] = processor;
			try (CsvResultSetWriter csv = new CsvResultSetWriter(writer, CsvPreference.EXCEL_PREFERENCE)) {
				csv.write(rs, processors);
				csv.flush();
			}
		}
	}
	
	
}