package havis.app.etb.sensor;

import static mockit.Deencapsulation.getField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import havis.transport.Subscriber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManagerTest {

	@After
	public void tearDown() {
		new File(Environment.CUSTOM_CONFIG_FILE).delete();
	}

	@Test
	public void testConfigurationManager() throws ConfigurationManagerException {
		assertFalse(new File(Environment.CUSTOM_CONFIG_FILE).exists());
		ConfigurationManager configurationManager = new ConfigurationManager();
		assertEquals(configurationManager.get().getConversion(), ((Configuration) getField(configurationManager, "configuration")).getConversion());
		assertFalse(new File(Environment.CUSTOM_CONFIG_FILE).exists());
	}

	@Test
	public void testGet() throws ConfigurationManagerException {
		ConfigurationManager configurationManager = new ConfigurationManager();
		assertEquals(getField(configurationManager, "configuration"), configurationManager.get());
	}

	@Test
	public void testSet() throws ConfigurationManagerException, FileNotFoundException, IOException {
		assertFalse(new File(Environment.CUSTOM_CONFIG_FILE).exists());
		ConfigurationManager configurationManager = new ConfigurationManager();

		Configuration configuration = generateTestConfig();
		configurationManager.set(configuration);

		assertTrue(new File(Environment.CUSTOM_CONFIG_FILE).exists());

		String configFileContent = readConfigFile();
		ObjectMapper om = new ObjectMapper();
		om.writerWithDefaultPrettyPrinter().writeValue(new File(Environment.CUSTOM_CONFIG_FILE), configuration);

		assertEquals(readConfigFile(), configFileContent);
	}

	private String readConfigFile() throws IOException, FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(Environment.CUSTOM_CONFIG_FILE))) {
			while (br.ready())
				sb.append(br.readLine());
		}
		return sb.toString().trim();
	}

	@Test
	public void testReset() throws ConfigurationManagerException {
		assertFalse(new File(Environment.CUSTOM_CONFIG_FILE).exists());
		ConfigurationManager configurationManager = new ConfigurationManager();
		Configuration configuration = generateTestConfig();
		configurationManager.set(configuration);
		assertTrue(new File(Environment.CUSTOM_CONFIG_FILE).exists());
		configurationManager.reset();
		assertFalse(new File(Environment.CUSTOM_CONFIG_FILE).exists());
		assertNotEquals(configuration, getField(configurationManager, "configuration"));
	}

	private Configuration generateTestConfig() {
		Configuration configuration = new Configuration();
		configuration.setConversion("2*value");
		configuration.setUnit("mW");

		Limit ceil = new Limit();
		ceil.setEnable(true);
		ceil.setValue((float) Math.PI);
		Subscriber transportObject = new Subscriber();
		transportObject.setUri("http://localhost/etb-notify/ceil");
		ceil.getNotifications().add(transportObject);

		Limit floor = new Limit();
		floor.setEnable(false);
		floor.setValue(-(float) Math.PI);

		transportObject = new Subscriber();
		transportObject.setUri("http://localhost/etb-notify/floor");
		floor.getNotifications().add(transportObject);

		configuration.setCeiling(ceil);
		configuration.setFloor(floor);

		transportObject = new Subscriber();
		transportObject.setUri("mqtt://localhost/{epc}?clientId=1&qos=5");
		configuration.getSubscribers().add(transportObject);
		transportObject = new Subscriber();
		transportObject.setUri("mqtt://localhost/{epc}?clientId=2&qos=10");
		configuration.getSubscribers().add(transportObject);
		return configuration;
	}

	@Test
	public void testDiff() {
		Configuration oldConf = generateTestConfig();
		Configuration newConf = generateTestConfig();

		boolean termChanged = false;

		if (oldConf.getConversion() == null)
			termChanged = newConf.getConversion() != null;
		else if (newConf.getConversion() == null)
			termChanged = oldConf.getConversion() != null;
		else
			termChanged = !oldConf.getConversion().equals(newConf.getConversion());

		boolean subscribersChanged = !Arrays.equals(oldConf.getSubscribers().toArray(), newConf.getSubscribers().toArray());
		boolean floorNotificationsChanged = !Arrays.equals(oldConf.getFloor().getNotifications().toArray(), newConf.getFloor().getNotifications().toArray());
		boolean ceilingNotificationsChanged = !Arrays.equals(oldConf.getCeiling().getNotifications().toArray(), newConf.getCeiling().getNotifications()
				.toArray());

		if (subscribersChanged)
			System.out.println("Subscribers diff.");

		if (floorNotificationsChanged)
			System.out.println("Floor diff.");

		if (ceilingNotificationsChanged)
			System.out.println("Ceil diff.");

		if (termChanged)
			System.out.println("Ceil diff.");

	}

}
