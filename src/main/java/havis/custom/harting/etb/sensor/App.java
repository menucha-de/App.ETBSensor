package havis.custom.harting.etb.sensor;

import havis.middleware.ale.service.cc.CCCmdReport;
import havis.middleware.ale.service.cc.CCOpReport;
import havis.middleware.ale.service.cc.CCReports;
import havis.middleware.ale.service.cc.CCTagReport;
import havis.transport.Subscriber;
import havis.transport.SubscriberManager;
import havis.transport.ValidationException;
import havis.transport.common.CommonSubscriberManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class App {

	private final static Logger log = Logger.getLogger(App.class.getName());
	private HistoryManager historyManager;
	private ConfigurationManager configurationManager;

	private CCThread ccThread;
	private final BlockingQueue<CCReports> ccQueue = new LinkedBlockingQueue<>();

	private SubscriberManager reportSubscribers;
	private SubscriberManager floorNotificationSubscribers;
	private SubscriberManager ceilingNotificationSubscribers;

	public App(HistoryManager historyManager, ConfigurationManager configurationManager) throws ConverterException, ValidationException,
			ConfigurationManagerException {
		this.historyManager = historyManager;
		this.configurationManager = configurationManager;
		Map<String, String> props = new HashMap<String, String>();
		props.put("MimeType", "application/json");
		reportSubscribers = new CommonSubscriberManager(HistoryEntry.class, configurationManager.get().getSubscribers(), props);
		floorNotificationSubscribers = new CommonSubscriberManager(Notification.class, configurationManager.get().getFloor().getNotifications(), props);
		ceilingNotificationSubscribers = new CommonSubscriberManager(Notification.class, configurationManager.get().getCeiling().getNotifications(), props);
		this.configurationManager.set(this.configurationManager.get());
	}

	public Queue<CCReports> getCCQueue() {
		return ccQueue;
	}

	public synchronized void start() {
		if (ccThread == null) {
			ccThread = new CCThread();
			ccThread.start();
		}
	}

	public synchronized void stop() {
		if (ccThread != null) {
			ccThread.running = false;
			try {
				ccThread.join();
			} catch (InterruptedException e) {
			} finally {
				ccThread = null;
			}
		}
		if (historyManager != null)
			try {
				historyManager.close();
			} catch (Throwable e) {
				log.log(Level.FINE, "Failed to close manager", e);
			}

		configurationManager.getAppExceptionHandler().reset();
	}

	public synchronized void updateConfig(Configuration config) throws ConfigurationManagerException, ValidationException {
		updateSubscribers(reportSubscribers, config.getSubscribers());
		updateSubscribers(ceilingNotificationSubscribers, config.getCeiling().getNotifications());
		updateSubscribers(floorNotificationSubscribers, config.getFloor().getNotifications());
		configurationManager.set(config);
	}

	public void updateSubscriber(Subscriber subscriber) throws ConfigurationManagerException, ValidationException {
		Configuration configuration = configurationManager.get();
		int index = configuration.getSubscribers().indexOf(subscriber);
		if (index >= 0) {
			configuration.getSubscribers().set(index, subscriber);
			reportSubscribers.update(subscriber);

		}
		index = configuration.getFloor().getNotifications().indexOf(subscriber);
		if (index >= 0) {
			configuration.getFloor().getNotifications().set(index, subscriber);
			floorNotificationSubscribers.update(subscriber);
		}
		index = configuration.getCeiling().getNotifications().indexOf(subscriber);
		if (index >= 0) {
			configuration.getCeiling().getNotifications().set(index, subscriber);
			ceilingNotificationSubscribers.update(subscriber);
		}
		configurationManager.set(configuration);
	}

	public String addSubscriber(Subscriber subscriber, String type) throws ValidationException {
		switch (type) {
		case "SUBSCRIBER":
			return reportSubscribers.add(subscriber);
		case "FLOOR":
			return floorNotificationSubscribers.add(subscriber);
		case "CEILING":
			return ceilingNotificationSubscribers.add(subscriber);
		default:
			return null;
		}
	}

	private void updateSubscribers(SubscriberManager in, List<Subscriber> config) throws ValidationException {
		// Add/updateSubscribers
		for (Subscriber subscriber : config) {
			if (subscriber.getId() == null) {
				subscriber.setId(in.add(subscriber));
			} else {
				in.update(subscriber);
			}
		}
		// Remove missing subscribers
		List<Subscriber> deletion = new ArrayList<Subscriber>();
		for (Subscriber subscriber : in.getSubscribers()) {
			if (!config.contains(subscriber)) {
				deletion.add(subscriber);
			}
		}
		for (Subscriber subscriber : deletion) {
			in.remove(subscriber);
		}
	}

	private class CCThread extends Thread {

		private boolean running = true;
		private ExceptionHandler exceptionHandler;

		CCThread() {
			super("CC-Thread");
			this.exceptionHandler = configurationManager.getAppExceptionHandler();
		}

		private void evaluate(CCReports reports) {
			if (reports.getCmdReports() == null)
				return;

			Configuration config = configurationManager.get();
			String epc, data;

			for (CCCmdReport ccReport : reports.getCmdReports().getCmdReport()) {
				if (ccReport.getTagReports() == null)
					continue;
				for (CCTagReport tagReport : ccReport.getTagReports().getTagReport()) {
					epc = tagReport.getId();
					if (tagReport.getOpReports() == null)
						continue;
					for (CCOpReport opReports : tagReport.getOpReports().getOpReport()) {
						if ("SUCCESS".equals(opReports.getOpStatus())) {
							data = opReports.getData();

							try {
								float value = Converter.getInstance().eval(config.getConversion(), Converter.getInstance().parse(data));
								HistoryEntry he = historyManager.create(epc, value, config.getUnit());
								historyManager.add(he);

								log.log(Level.FINER, "Added history entry: timestamp={0}, epc={1}, value={2}, unit={3}",
										new Object[] { he.getTimestamp(), he.getEpc(), he.getValue(), he.getUnit() });

								reportSubscribers.send(he);

								Notification notification = new Notification();
								notification.setEpc(he.getEpc());
								notification.setTimestamp(he.getTimestamp());
								notification.setUnit(he.getUnit());
								notification.setValue(he.getValue());

								if (value < config.getFloor().getValue()) {
									notification.setType("FLOOR");
									notification.setLimit(config.getFloor().getValue());
									floorNotificationSubscribers.send(notification);
								}

								if (value > config.getCeiling().getValue()) {
									notification.setType("CEILING");
									notification.setLimit(config.getCeiling().getValue());
									ceilingNotificationSubscribers.send(notification);
								}
								this.exceptionHandler.reset();
							}

							catch (ConverterException ce) {
								String errMsg = String.format("Value conversion failed: %s", ce.getCause() == null ? ce.getMessage() : ce.getCause()
										.getMessage());
								LogRecord logRec = new LogRecord(Level.OFF, errMsg);
								logRec.setLoggerName(log.getName());
								logRec.setThrown(ce);

								if (log.isLoggable(Level.WARNING) && exceptionHandler.handle(errMsg))
									logRec.setLevel(Level.WARNING);
								else
									logRec.setLevel(Level.FINE);

								log.log(logRec);

							} catch (HistoryManagerException hme) {
								String errMsg = String.format("Value conversion failed: %s", hme.getMessage());
								LogRecord logRec = new LogRecord(Level.OFF, errMsg);
								logRec.setLoggerName(log.getName());
								logRec.setThrown(hme);

								if (log.isLoggable(Level.WARNING) && exceptionHandler.handle(errMsg))
									logRec.setLevel(Level.WARNING);
								else
									logRec.setLevel(Level.FINE);

								log.log(logRec);
							}
						}
					}
				}
			}
		}

		@Override
		public void run() {
			try {
				while (running) {
					CCReports reports = ccQueue.poll(100, TimeUnit.MILLISECONDS);

					if (reports != null) {
						try {
							evaluate(reports);
						} catch (Throwable e) {
							LogRecord logRec = new LogRecord(Level.WARNING, "Failed to evaluate CC report: {0}");
							logRec.setLoggerName(log.getName());
							logRec.setThrown(e);
							logRec.setParameters(new Object[] { e });
							log.log(logRec);
						}
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}
}