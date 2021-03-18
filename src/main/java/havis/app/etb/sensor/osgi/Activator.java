package havis.app.etb.sensor.osgi;

import havis.app.etb.sensor.App;
import havis.app.etb.sensor.ConfigurationManager;
import havis.app.etb.sensor.Connector;
import havis.app.etb.sensor.Converter;
import havis.app.etb.sensor.Environment;
import havis.app.etb.sensor.HistoryManager;
import havis.app.etb.sensor.ScriptEngineFactory;
import havis.app.etb.sensor.rest.RESTApplication;
import havis.middleware.ale.base.exception.ALEException;
import havis.middleware.ale.config.service.mc.Path;
import havis.middleware.ale.service.mc.MC;
import havis.middleware.ale.service.mc.MCCommandCycleSpec;
import havis.middleware.ale.service.mc.MCPortCycleSpec;
import havis.middleware.ale.service.mc.MCSubscriberSpec;
import havis.transport.Transporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.ws.rs.core.Application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Activator implements BundleActivator {

	private final static Logger log = Logger.getLogger(Activator.class.getName());
	private final static String QUEUE_NAME = "name";
	private final static String CC_QUEUE_VALUE = "etb-cc";

	private ServiceTracker<MC, MC> tracker;
	private ServiceRegistration<?> ccQueue;
	private ServiceRegistration<Application> restService;
	private App app;

	@SuppressWarnings("serial")
	@Override
	public void start(final BundleContext context) throws Exception {
		if (new File(Environment.LOCK).createNewFile())
			create(context);

		initializeScriptEngineFactory();

		HistoryManager historyManager;
		ConfigurationManager configurationManager;

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(Activator.class.getClassLoader());
			configurationManager = new ConfigurationManager();
			historyManager = new HistoryManager();
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}

		app = new App(historyManager, configurationManager);
		app.start();
		restService = context.registerService(Application.class, new RESTApplication(historyManager, configurationManager, ScriptEngineFactory.getFactory()
				.getEngine(), app), null);
		Connector.createFactory(new Connector() {
			@Override
			public List<String> getTypes() {
				List<String> types = new ArrayList<>();
				try {
					for (@SuppressWarnings("rawtypes")
					ServiceReference<Transporter> reference : context.getServiceReferences(Transporter.class, null)) {
						Object type = reference.getProperty("name");
						if (type instanceof String) {
							types.add((String) type);
						}
					}
				} catch (InvalidSyntaxException e) {
					log.log(Level.SEVERE, "Error getting transport type", e);
				}
				return types;
			}
		});
		ccQueue = context.registerService(Queue.class, app.getCCQueue(), new Hashtable<String, String>() {
			{
				put(QUEUE_NAME, CC_QUEUE_VALUE);
			}
		});
	}

	private void initializeScriptEngineFactory() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
			final ScriptEngine engine = new ScriptEngineManager().getEngineByName(Converter.ENGINE_NAME);
			ScriptEngineFactory.createFactory(new ScriptEngineFactory() {
				@Override
				public ScriptEngine getEngine() {
					return engine;
				}
			});
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (ccQueue != null) {
			ccQueue.unregister();
			ccQueue = null;
		}

		if (restService != null) {
			restService.unregister();
			restService = null;
		}

		if (app != null) {
			app.stop();
			app = null;
		}

		ScriptEngineFactory.clearFactory();
	}

	private void create(BundleContext context) throws IOException {
		tracker = new ServiceTracker<MC, MC>(context, MC.class, null) {
			@SuppressWarnings("unchecked")
			@Override
			public MC addingService(ServiceReference<MC> reference) {
				try {
					MC mc = super.addingService(reference);
					ObjectMapper mapper = new ObjectMapper();
					try {
						for (java.nio.file.Path path : Files.newDirectoryStream(Paths.get(Environment.SPEC, "cc"))) {
							Map<String, Object> map = mapper.readValue(path.toFile(), Map.class);
							String id = mc.add(Path.Service.CC.CommandCycle, mapper.convertValue(map.get("commandCycle"), MCCommandCycleSpec.class));
							for (MCSubscriberSpec spec : mapper.convertValue(map.get("subscribers"), MCSubscriberSpec[].class))
								mc.add(Path.Service.CC.Subscriber, spec, id);
						}
						for (java.nio.file.Path path : Files.newDirectoryStream(Paths.get(Environment.SPEC, "pc"))) {
							Map<String, Object> map = mapper.readValue(path.toFile(), Map.class);
							mc.add(Path.Service.PC.PortCycle, mapper.convertValue(map.get("portCycle"), MCPortCycleSpec.class));
						}
					} catch (IOException | ALEException e) {
						log.log(Level.WARNING, "Failed to import spec", e);
					}
					return mc;
				} finally {
					tracker.close();
				}
			}
		};
		tracker.open();
	}

}