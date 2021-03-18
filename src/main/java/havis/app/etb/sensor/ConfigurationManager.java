package havis.app.etb.sensor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManager {

	private final static ObjectMapper mapper = new ObjectMapper();

	private Configuration configuration;
	private ExceptionHandler appExceptionHandler, subscriptionExceptionHandler, floorExceptionHandler, ceilingExceptionHandler;

	public ConfigurationManager() throws ConfigurationManagerException {

		this.appExceptionHandler = new ExceptionHandler();
		this.subscriptionExceptionHandler = new ExceptionHandler();
		this.floorExceptionHandler = new ExceptionHandler();
		this.ceilingExceptionHandler = new ExceptionHandler();

		try {
			File configFile = new File(Environment.CUSTOM_CONFIG_FILE);
			if (configFile.exists()) {
				this.configuration = mapper.readValue(new File(Environment.CUSTOM_CONFIG_FILE), Configuration.class);
			} else {
				this.configuration = mapper.readValue(ConfigurationManager.class.getClassLoader().getResourceAsStream(Environment.DEFAULT_CONFIG_FILE),
						Configuration.class);
			}
		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}

	public Configuration get() {
		return this.configuration;
	}

	public void set(Configuration configuration) throws ConfigurationManagerException {
		try {
			File configFile = new File(Environment.CUSTOM_CONFIG_FILE);
			Files.createDirectories(configFile.toPath().getParent(), new FileAttribute<?>[] {});
			mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, configuration);
			this.diff(this.configuration, configuration);
			this.configuration = configuration;

		} catch (Exception e) {
			throw new ConfigurationManagerException(e);
		}
	}

	private void diff(Configuration oldConf, Configuration newConf) {

		boolean subscribersChanged = !Arrays.equals(oldConf.getSubscribers().toArray(), newConf.getSubscribers().toArray());
		boolean floorNotificationsChanged = false;
		boolean ceilingNotificationsChanged = false;

		boolean termChanged = false;
		if (oldConf.getConversion() == null)
			termChanged = newConf.getConversion() != null;
		else if (newConf.getConversion() == null)
			termChanged = oldConf.getConversion() != null;
		else
			termChanged = !oldConf.getConversion().equals(newConf.getConversion());

		if (oldConf.getFloor() == null)
			floorNotificationsChanged = newConf.getFloor() != null;
		else if (newConf.getFloor() == null)
			floorNotificationsChanged = oldConf.getFloor() != null;
		else
			floorNotificationsChanged = oldConf.getFloor().isEnable() != newConf.getFloor().isEnable()
					|| !Arrays.equals(oldConf.getFloor().getNotifications().toArray(), newConf.getFloor().getNotifications().toArray());

		if (oldConf.getCeiling() == null)
			ceilingNotificationsChanged = newConf.getCeiling() != null;
		else if (newConf.getCeiling() == null)
			ceilingNotificationsChanged = oldConf.getCeiling() != null;
		else
			ceilingNotificationsChanged = oldConf.getCeiling().isEnable() != newConf.getCeiling().isEnable()
					|| !Arrays.equals(oldConf.getCeiling().getNotifications().toArray(), newConf.getCeiling().getNotifications().toArray());

		if (termChanged)
			appExceptionHandler.reset();

		if (subscribersChanged)
			subscriptionExceptionHandler.reset();

		if (floorNotificationsChanged)
			floorExceptionHandler.reset();

		if (ceilingNotificationsChanged)
			ceilingExceptionHandler.reset();

	}

	public void reset() {
		new File(Environment.CUSTOM_CONFIG_FILE).delete();
		this.configuration = new Configuration();
		this.configuration.setConversion(Environment.DEFAULT_CONVERSION);

		this.appExceptionHandler.reset();
		this.floorExceptionHandler.reset();
		this.ceilingExceptionHandler.reset();
		this.subscriptionExceptionHandler.reset();
	}

	public ExceptionHandler getAppExceptionHandler() {
		return appExceptionHandler;
	}

	public ExceptionHandler getSubscriptionExceptionHandler() {
		return subscriptionExceptionHandler;
	}

	public ExceptionHandler getFloorExceptionHandler() {
		return floorExceptionHandler;
	}

	public ExceptionHandler getCeilingExceptionHandler() {
		return ceilingExceptionHandler;
	}
}