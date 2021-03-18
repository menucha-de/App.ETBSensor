package havis.app.etb.sensor;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

	private final static Logger log = Logger.getLogger(Environment.class.getName());
	private final static Properties properties = new Properties();

	static {
		try (InputStream stream = Environment.class.getClassLoader().getResourceAsStream("havis.app.etb.sensor.properties")) {
			properties.load(stream);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to load environment properties", e);
		}
	}

	public static final String LOCK = properties.getProperty("havis.app.etb.sensor.lock", "conf/havis/app/etb/sensor/lock");
	public static final String SPEC = properties.getProperty("havis.app.etb.sensor.spec", "conf/havis/app/etb/sensor/spec");

	public static final String JDBC_URL = properties.getProperty("havis.app.etb.sensor.jdbcUrl",
			"jdbc:h2:./etb-sensor;INIT=RUNSCRIPT FROM 'conf/havis/app/etb/sensor/history.sql'");
	public static final String JDBC_DRIVER = properties.getProperty("havis.app.etb.sensor.jdbcDriver", "org.h2.Driver");
	public static final String JDBC_USERNAME = properties.getProperty("havis.app.etb.sensor.jdbcUsername", "sa");
	public static final String JDBC_PASSWORD = properties.getProperty("havis.app.etb.sensor.jdbcPassword", "");
	public static final int MAX_RECORD_AGE = Integer.valueOf(properties.getProperty("havis.app.etb.sensor.maxRecordAge", "300000"));
	public static final String CUSTOM_CONFIG_FILE = properties.getProperty("havis.app.etb.sensor.customConfigFile", "conf/havis/app/etb/sensor/config.json");
	public static final String DEFAULT_CONFIG_FILE = properties.getProperty("havis.app.etb.sensor.defaultConfigFile", "havis/app/etb/sensor/config/default.json");
	public static final String DEFAULT_CONVERSION = properties.getProperty("havis.app.etb.sensor.defaultConversion", "value");
	public static final int HTTP_TIMEOUT = Integer.valueOf(properties.getProperty("havis.app.etb.sensor.httpTimeout", "0"));
	public static final int MQTT_PORT = Integer.valueOf(properties.getProperty("havis.app.etb.sensor.mqttPort", "1883"));
	public static final int MQTT_TIMEOUT = Integer.valueOf(properties.getProperty("havis.app.etb.sensor.mqttTimeout", "10"));
	public static final String MQTT_CLIENT_ID = properties.getProperty("havis.app.etb.sensor.mqttClientId", "clientid");
	public static final String MQTT_QOS = properties.getProperty("havis.app.etb.sensor.mqttQoS", "qos");
	 
}