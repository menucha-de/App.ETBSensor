package havis.custom.harting.etb.sensor;

import java.util.List;

/**
 * Abstract connector factory
 */
public abstract class Connector {

	private static Connector instance;

	/**
	 * @return the current factory
	 */
	public static Connector getFactory() {
		if (instance == null)
			throw new IllegalStateException("Connector factory has not been initialized");
		return instance;
	}

	/**
	 * @param connector
	 *            the factory to set
	 */
	public static void createFactory(Connector connector) {
		if (connector == null)
			throw new NullPointerException("connnector must not be null");
		instance = connector;
	}

	/**
	 * Clear the current factory, connector instantiation will not be possible
	 */
	public static void clearFactory() {
		instance = null;
	}

	/**
	 * Get all types
	 * 
	 * @return all types
	 */
	public abstract List<String> getTypes();
}
