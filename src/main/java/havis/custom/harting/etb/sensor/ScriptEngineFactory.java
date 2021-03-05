package havis.custom.harting.etb.sensor;

import javax.script.ScriptEngine;

public abstract class ScriptEngineFactory {

	private static ScriptEngineFactory instance;

	/**
	 * @return the current factory
	 */
	public static ScriptEngineFactory getFactory() {
		if (instance == null)
			throw new IllegalStateException("ScriptEngineFactory has not been initialized");
		return instance;
	}

	/**
	 * @param factory
	 *            the factory to set
	 */
	public static void createFactory(ScriptEngineFactory factory) {
		if (factory == null)
			throw new NullPointerException("factory must not be null");
		instance = factory;
	}

	/**
	 * Clear the current factory, ScriptEngine instantiation will not be
	 * possible
	 */
	public static void clearFactory() {
		instance = null;
	}

	public abstract ScriptEngine getEngine();
}
