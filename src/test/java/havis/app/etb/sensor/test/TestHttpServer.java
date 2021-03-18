package havis.app.etb.sensor.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class TestHttpServer {
	private static final int DEFAULT_PORT = 9000;
	private static final String DEFAULT_HOST = "0.0.0.0";
	private static final String[] DEFAULT_CONTEXTS = new String[] { "/sensor", "/floor", "/ceiling" };
	
	private int port;
	private String host;
	private String[] contexts;	
	private ServerThread thread;
	private HttpServerCallback callback; 
	
	interface HttpServerCallback {
		void requestReceived(String context, String body);
	}
	
	public TestHttpServer(String host, int port, String[] contexts, HttpServerCallback callback) {
		this.host = host;
		this.port = port;
		this.contexts = contexts;
		this.callback = callback;
	}
	
	private void start() throws IOException {
		if (thread == null) 
			thread = new ServerThread(this.host, this.port, this.contexts, this.callback);		
		thread.start();
	}
	
	private void stop() throws IOException, InterruptedException {
		thread.stopServer();
		thread.join();
		thread = null;		
	}
	
	private boolean isRunning() {
		return thread != null && thread.running;
	}
	
	public static void main(String[] args) throws InterruptedException {
		try {			
			
			String host = DEFAULT_HOST;
			int port = DEFAULT_PORT;
			String[] contexts = DEFAULT_CONTEXTS;
			
			if (args.length > 0) { 
				host = args[0].trim();			
			}

			if (args.length > 1) {
				try {
					port = Integer.valueOf(args[1].trim());				
				} catch (NumberFormatException e) {
					System.err.println("Unparsable port: " + args[1]);
				} 
			}
			
			if (args.length > 2)
				contexts = args[2].split("\\s*,\\s*");
			
			System.out.println("Using host: " + host + ":" + port);
			System.out.println("Handling contexts: " + Arrays.toString(contexts));
			System.out.println("Type 'start' to start server, 'stop' to stop and 'q' to quit. Type '?' for help.");
			
			TestHttpServer server = new TestHttpServer(host, port, contexts, new HttpServerCallback() {				
				@Override
				public void requestReceived(String context, String body) {
					System.out.println("HTTP " + context + ": " + body);					
				}
			});						
			
			boolean running = true;
			try (Scanner sc = new Scanner(System.in)) {
				while (running) {
					System.out.print("http-svr>");
					String cmd = sc.nextLine();				
					cmd = cmd.replaceAll("\\s+", "");					
					if (0 == cmd.length()) continue;
					
					switch (cmd) {						
						case "start": 									
							if (!server.isRunning()) {
								System.out.println("Starting server...");
								server.start();
								System.out.println("Server started.");
							} else System.out.println("Server already running.");
							
						break;
						case "stop":
							if (server.isRunning()) {
								System.out.println("Stopping server...");
								server.stop();
								System.out.println("Server stopped.");
							} else System.out.println("Server already stopped.");
							
						break;
						case "q": 
							if (server.isRunning()) {
								System.out.println("Stopping server...");
								server.stop();
								System.out.println("Server stopped.");
							}								
							running = false;													
						break;					
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static class ServerThread extends Thread {
		private HttpServer svr; 
		private boolean running;
		
		public ServerThread(String host, int port, String[] contexts, HttpServerCallback callback) throws IOException {
			svr = HttpServer.create(new InetSocketAddress(host, port), 0);
			for (String context : contexts)
				svr.createContext(context, new TestHttpHandler(context, callback));			
		}
		
		@Override
		public void run() {
			if (running) return;
			svr.start();
			running = true;
		}
		
		public void stopServer() throws IOException {
			if (!running) return;
			svr.stop(0);
			svr = null;
			running = false;						
		}

		public boolean isRunning() {
			return running;
		}
		
		
	}
	
	static class TestHttpHandler implements HttpHandler {

		private String context;
		private HttpServerCallback callback;
		public TestHttpHandler(String context, HttpServerCallback callback) {
			this.context = context;
			this.callback = callback;
		}
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			InputStream is = exchange.getRequestBody();
			byte[] buf = new byte[8];
			int len = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while((len = is.read(buf)) > 0)
				bos.write(buf, 0, len);			
			
			String reqBody = bos.toString("UTF-8");
			if (this.callback != null) 
				callback.requestReceived(context, reqBody);
			
			String response = "OK";
			exchange.sendResponseHeaders(200, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
		
	}
}
