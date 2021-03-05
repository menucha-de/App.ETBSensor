package havis.custom.harting.etb.sensor.osgi;

import havis.custom.harting.etb.sensor.App;
import havis.custom.harting.etb.sensor.ConfigurationManager;
import havis.custom.harting.etb.sensor.HistoryManager;
import havis.custom.harting.etb.sensor.rest.RESTApplication;
import havis.middleware.ale.service.ec.ECReports;
import havis.middleware.ale.service.pc.PCReports;

import java.util.Hashtable;
import java.util.Queue;

import javax.script.ScriptEngine;
import javax.ws.rs.core.Application;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ActivatorTest {

	@Tested
	Activator activator;
	@Mocked
	BundleContext context;
	@Mocked
	HistoryManager historyManager;
	@Mocked
	ConfigurationManager configurationManager;
	@Mocked
	RESTApplication restApplication;
	@Mocked
	PCReports pcReports;
	@Mocked
	ECReports ecReports;

	@Test
	public void testStart(@Mocked final Queue<?> ecQueue, @Mocked final Queue<?> pcQueue) throws Exception {

		new MockUp<App>() {

			@Mock
			void start() {
			}

			@Mock
			Queue<?> getECQueue() {
				return ecQueue;
			}

			@Mock
			Queue<?> getPCQueue() {
				return pcQueue;
			}
		};
		activator.start(context);

		final App main = Deencapsulation.getField(activator, "main");
		final HistoryManager historyManager = Deencapsulation.getField(main, "historyManager");
		final ConfigurationManager configurationManager = Deencapsulation.getField(main, "configurationManager");

		new Verifications() {
			{

				final String QUEUE_NAME = "name";
				// subscriber URI: queue://etb-ec
				final String EC_QUEUE_VALUE = "etb-ec";
				// subscriber URI: queue://etb-pc
				final String PC_QUEUE_VALUE = "etb-pc";

				Hashtable<String, String> pcTable = new Hashtable<String, String>();
				Hashtable<String, String> ecTable = new Hashtable<String, String>();
				pcTable.put(QUEUE_NAME, PC_QUEUE_VALUE);
				ecTable.put(QUEUE_NAME, EC_QUEUE_VALUE);

				context.registerService(
						Application.class,
						new RESTApplication(withSameInstance(historyManager), withSameInstance(configurationManager), this.<ScriptEngine> withNotNull(), this
								.<App> withNotNull()), null);
				times = 1;

				context.registerService(Queue.class, ecQueue, ecTable);
				times = 1;

				context.registerService(Queue.class, pcQueue, pcTable);
				times = 1;
			}
		};
	}

	@Test
	public void testStop_variablesAreNull() {

		try {
			activator.stop(context);
		} catch (Exception e) {
			throw new NullPointerException("If a reference variable is already null, it must not be unregistered or stopped.");
		}
	}

	@Test
	public void testStop(@Mocked final App main, @Mocked final ServiceRegistration<?> serviceRegistration) throws Exception {

		new Expectations() {
			{
				new App(withAny(historyManager), withAny(configurationManager));
				result = main;
			}
		};

		activator.start(context);
		activator.stop(context);

		new Verifications() {
			{
				serviceRegistration.unregister();
				times = 3;

				main.stop();
				times = 1;
			}
		};
	}
}
