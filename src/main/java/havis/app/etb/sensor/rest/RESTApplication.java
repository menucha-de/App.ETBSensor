package havis.app.etb.sensor.rest;

import havis.app.etb.sensor.App;
import havis.app.etb.sensor.ConfigurationManager;
import havis.app.etb.sensor.ConfigurationManagerException;
import havis.app.etb.sensor.HistoryManager;
import havis.app.etb.sensor.rest.provider.HistoryManagerExceptionMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.ws.rs.core.Application;

public class RESTApplication extends Application {

	private final static String PROVIDERS = "javax.ws.rs.ext.Providers";

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> empty = new HashSet<Class<?>>();
	private Map<String, Object> properties = new HashMap<>();

	public RESTApplication(HistoryManager historyManager, ConfigurationManager configurationManager, ScriptEngine engine, App app) {
		singletons.add(new ETBSensorService(historyManager, configurationManager, engine, app));
		properties.put(PROVIDERS, new Class<?>[] { HistoryManagerExceptionMapper.class, ConfigurationManagerException.class });
	}

	@Override
	public Set<Class<?>> getClasses() {
		return empty;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
}