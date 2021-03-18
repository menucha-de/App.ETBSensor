package havis.app.etb.sensor;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class Validator {
	
	private static final ResourceBundle res = 
			ResourceBundle.getBundle("havis.app.etb.sensor.ui.resourcebundle.ConstantsResource");
	
	public static void validateTerm(ScriptEngine engine, String term) throws ValidationException {
		try {
			Converter.getInstance().eval(engine, term, 0.0f);
		} catch (ConverterException e) {
			throw new ValidationException(e.getCause() != null ? e.getCause() : e);			
		}
	}
	
	private static boolean isNullOrEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}
	
	public static havis.app.etb.sensor.URI createUri(String scheme, String host, String portStr, String username, String password, String path,
			String query, String topic, String clientId, String qosStr) {
		
		havis.app.etb.sensor.URI uri = new URI();		
		if (isNullOrEmpty(host)) uri.getValidationErrors().put("host", res.getString("validationEmptyHost"));
		
		int port = -1;
		if (!isNullOrEmpty(portStr)) {
			try {
				port = Integer.parseInt(portStr);
				if (port < 0) throw new NumberFormatException();
			} catch (NumberFormatException e) {
				uri.getValidationErrors().put("port", res.getString("validationBadPort"));
			}
		}
		
		if (isNullOrEmpty(scheme)) uri.getValidationErrors().put("type", res.getString("validationEmptyProtocol"));
		else {		
			
			String userInfo = null;
			if (!isNullOrEmpty(username)) {
				userInfo = username;
				if (!isNullOrEmpty(password)) userInfo += ":" + password;
			}
			
			if (scheme.matches("https?")) {
				if (uri.getValidationErrors().isEmpty()) {
					try {						
						if (!isNullOrEmpty(path) && !path.startsWith("/")) path = "/" + path;							
						uri.setUriString(new java.net.URI(scheme, userInfo, host, port, path, !isNullOrEmpty(query) ? query : null, null).toString());
					} catch (URISyntaxException e) {
						uri.getValidationErrors().put("uri", e.getMessage());
					}
				}
			}
			else if (scheme.matches("mqtts?")) {
				
				Integer qos = null;
				
				if (isNullOrEmpty(topic)) uri.getValidationErrors().put("topic", res.getString("validationEmptyTopic"));
				if (isNullOrEmpty(clientId)) uri.getValidationErrors().put("clientId", res.getString("validationEmptyClientId"));
				if (!isNullOrEmpty(qosStr)) {
					try {
						qos = Integer.parseInt(qosStr);
						if (qos > 2 || qos < 0) throw new NumberFormatException();
					} catch (NumberFormatException e) {
						uri.getValidationErrors().put("qos", res.getString("validationBadQoS"));
					}
				}
				
				if (uri.getValidationErrors().isEmpty()) {
					
					String queryStr = Environment.MQTT_CLIENT_ID + "=" + clientId;
					if (qos != null) queryStr += "&" + Environment.MQTT_QOS + "=" + qos;					
					try {
						if (!topic.startsWith("/")) topic = "/" + topic;												
						uri.setUriString(decodeWildcard(new java.net.URI(scheme, userInfo, host, port, topic, queryStr, null).toString()));
					} catch (URISyntaxException e) {
						uri.getValidationErrors().put("uri", e.getMessage());
					}
				}				
			}
			else uri.getValidationErrors().put("type", res.getString("validationBadProtocol") + ": " + scheme);			
		}
		
		return uri;
	}
	
	public static havis.app.etb.sensor.URI parseUri(String uriStr) {		
		URI res = new URI();
		
		try {
			java.net.URI uri = new java.net.URI(encodeWildcard(uriStr));
			
			res.setScheme(uri.getScheme());
			
			if (uri.getScheme() == null || !uri.getScheme().matches("(https?)|(mqtts?)"))
				throw new URISyntaxException(uri.getScheme() == null ? "null" : uri.getScheme(), "Unsupported protocol");
			
			if (uri.getHost() == null)
				throw new URISyntaxException(uriStr, "No host specified");
			
			res.setHost(uri.getHost());
			res.setPort(uri.getPort());
			
			if (uri.getUserInfo() != null) {
				String[] userInfo = uri.getUserInfo().split(":");
				res.setUsername(userInfo[0]);
				if (userInfo.length > 1)
					res.setPassword(userInfo[1]);
			}
			
			res.setQos(-1);
			
			if (uri.getScheme().matches("(https?)")) {
				res.setPath(uri.getPath());
				res.setQuery(uri.getQuery());
			} else {
				if (uri.getPath().length() == 0)
					throw new URISyntaxException(uriStr, "No MQTT topic specified");
				
				res.setTopic(uri.getPath());
				
				if (uri.getQuery() != null) {
					String[] queryParts = uri.getQuery().split("&");
					for (String queryPart : queryParts) {
						String[] kvPairs = queryPart.split("=");
						if (kvPairs.length > 1) { 
							if (kvPairs[0].equals(Environment.MQTT_CLIENT_ID))
								res.setClientId(kvPairs[1]);
							else if (kvPairs[0].equals(Environment.MQTT_QOS)) {
								try {
									res.setQos(Integer.parseInt(kvPairs[1]));
								} catch (NumberFormatException e) { }
							}
						}
					}
				}
				
				if (res.getClientId() == null)
					throw new URISyntaxException(uriStr, "No client ID specified");
			}
					
		} catch (URISyntaxException ex) {
			res.getValidationErrors().put("uri", ex.getMessage());
		}
		
		return res;
	}
	
	private static String decodeWildcard(String s) {
		if (s == null) return s;		
		try {
			return s.replaceAll(URLEncoder.encode("{epc}", "UTF-8"), "{epc}");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}
	
	private static String encodeWildcard(String s) {
		if (s == null) return s;		
		try {
			return s.replaceAll("\\{epc\\}", URLEncoder.encode("{epc}", "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

	public static Map<String, String> validateConfig(ScriptEngine engine, String conversion, String ceiling, String floor) {
		
		Map<String, String> result = new HashMap<>();
		
		if (isNullOrEmpty(conversion)) result.put("conversion", res.getString("validationEmptyConversion"));
		else {
			try { validateTerm(engine, conversion); }
			catch (ValidationException e) {				
				if (e.getCause() instanceof ConverterException)
					result.put("conversion", e.getCause().getMessage());
				else if (e.getCause() instanceof ScriptException && e.getCause().getCause() != null)
					result.put("conversion", e.getCause().getCause().getMessage());
				else
					result.put("conversion", e.getMessage()); 
			}
		}
		
		Float ceilVal = null, floorVal = null; 
		
		if (ceiling != null) {		
			if (isNullOrEmpty(ceiling)) {
				result.put("ceiling", res.getString("validationEmptyCeiling"));
			} else {
				try {
					ceilVal = Float.parseFloat(ceiling);
				} catch (NumberFormatException e) {
					result.put("ceiling", res.getString("validationBadCeiling"));
				}
			}
		}
		
		if (floor != null) {		
			if (isNullOrEmpty(floor)) {
				result.put("floor", res.getString("validationEmptyFloor"));
			} else {
				try {
					floorVal = Float.parseFloat(floor);
				} catch (NumberFormatException e) {
					result.put("floor", res.getString("validationBadFloor"));
				}
			}
		}
		
		if (ceilVal != null && floorVal != null) {
			if (ceilVal <= floorVal) {
				result.put("ceiling", res.getString("validationCeilingTooSmall"));
				result.put("floor", res.getString("validationFloorTooBig"));
			}
		}
		
		return result;
	}
}
