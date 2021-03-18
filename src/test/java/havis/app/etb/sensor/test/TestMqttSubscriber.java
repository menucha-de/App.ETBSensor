package havis.app.etb.sensor.test;

import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class TestMqttSubscriber {
	private static String DEFAULT_URI = "tcp://localhost";
	private static String DEFAULT_CLIENT_ID = UUID.randomUUID().toString();	
	private static String[] DEFAULT_TOPICS = new String[] { "/sensor", "/ceiling", "/floor", "/urn:epc:raw:96.x0080B0403C000000120AB48E" };
	
	interface MqttSubscriberCallback {
		void messageReceived(String topic, String message);
	}

	private String uri;
	private String clientId;	
	private String[] topics;
	private MqttSubscriberThread thread;
	private MqttSubscriberCallback callback;	
	
	public TestMqttSubscriber(String uri, String clientId, String[] topics, MqttSubscriberCallback callback) {
		this.uri = uri;
		this.clientId = clientId;
		this.topics = topics;
		this.callback = callback;
	}
	
	private void start() throws MqttException {
		if (thread == null || thread.crashed) 
			thread = new MqttSubscriberThread(this.uri, this.clientId, this.topics, this.callback);		
		thread.start();
	}
	
	private void stop() throws MqttException, InterruptedException {
		thread.stopSubscriber();
		thread.join();
		thread = null;		
	}
	
	private boolean isRunning() {
		return thread != null && thread.running;
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		try {						
			String uri = DEFAULT_URI;
			String clientId = DEFAULT_CLIENT_ID;
			String[] topics = DEFAULT_TOPICS;
			
			if (args.length > 0) uri = args[0];
			if (args.length > 1) clientId = args[1];
			if (args.length > 2) topics = args[2].split("\\s*,\\s*");
			
			System.out.println("Using URI: " + uri);
			System.out.println("Using client ID: " + clientId);
			System.out.println("Subsribing topics: " + Arrays.toString(topics));			
			System.out.println("Type 'start' to start subscriber, 'stop' to stop and 'q' to quit. Type '?' for help.");
			
			TestMqttSubscriber subscriber = new TestMqttSubscriber(uri, clientId, topics, new MqttSubscriberCallback() {				
				@Override
				public void messageReceived(String topic, String message) {
					System.out.println("MQTT " + topic + ": " + message);
				}
			});
						
			boolean running = true;
			try (Scanner sc = new Scanner(System.in)) {
				while (running) {
					System.out.print("mqtt-sub>");
					String cmd = sc.nextLine();				
					cmd = cmd.replaceAll("\\s+", "");					
					if (0 == cmd.length()) continue;
					
					switch (cmd) {						
						case "start":
							if (!subscriber.isRunning()) {
								System.out.println("Starting subscriber...");
								subscriber.start();
								System.out.println("Subscriber started.");
							} else System.out.println("Subscriber already running.");
						break;
						
						case "stop": 							
							if (subscriber.isRunning()) {
								System.out.println("Stopping subscriber...");
								subscriber.stop();
								System.out.println("Subscriber stopped.");
							} else System.out.println("Subscriber already stopped.");			
						break;
						
						case "q": 
							if (subscriber.isRunning()) {
								System.out.println("Stopping subscriber...");
								subscriber.stop();
								System.out.println("Subscriber stopped.");
							}
							running = false;													
						break;					
					}
				}
			}
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}
	
	static class MqttSubscriberThread extends Thread {		
		private MqttClient client; 
		private boolean running;
		private boolean crashed;
		private String[] topics;
		
		public MqttSubscriberThread(String uri, String clientId, String[] topics, MqttSubscriberCallback callback) throws MqttException {					
			client = new MqttClient(uri, clientId, new MemoryPersistence());
			client.setCallback(new TestMqttHandler(this, callback));
			this.topics = topics;
		}

		@Override
		public void run() {
			if (running) return;
			try {
				client.connect();
				client.subscribe(topics);
				running = true;
			} catch (MqttException  e) {
				System.err.println(e);
				running = false;
			} 
		}
		
		public void stopSubscriber() throws MqttException {
			if (!running) return;
			client.disconnect();
			running = false;						
		}

		public boolean isRunning() {
			return running;
		}
		
		
	}
	
	static class TestMqttHandler implements MqttCallback {

		private MqttSubscriberThread subscr;
		private MqttSubscriberCallback callback;
		
		public TestMqttHandler(MqttSubscriberThread subscr, MqttSubscriberCallback callback) {
			this.subscr = subscr;
			this.callback = callback;
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			if (callback != null) 
				callback.messageReceived(topic, message.toString());					
		}
		
		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			
		}
		
		@Override
		public void connectionLost(Throwable t) {
			System.err.println(t);
			subscr.running = false;
			subscr.crashed = true;
		}
		
	}
}
