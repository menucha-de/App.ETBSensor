package havis.custom.harting.etb.sensor;

import java.util.HashMap;
import java.util.Map;

public class URI {
	
	private String scheme;
	private String host;
	private int port;
	private String username;
	private String password;
	private String path;
	private String query;
	private String topic;
	private String clientId;
	private int qos;	
	private String uriString;
	
	Map<String, String> validationErrors = new HashMap<>();
	
	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public int getQos() {
		return qos;
	}
	public void setQos(int qos) {
		this.qos = qos;
	}
	public Map<String, String> getValidationErrors() {
		return validationErrors;
	}
	public String getUriString() {
		return uriString;
	}
	public void setUriString(String uriString) {
		this.uriString = uriString;
	}
	
	
}
