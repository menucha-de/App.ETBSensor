package havis.custom.harting.etb.sensor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class ValidatorTest {

	@Test
	public void testValidateTerm() {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName(Converter.ENGINE_NAME);

		try {
			Validator.validateTerm(engine, "{}");
			fail("Exception expected.");
		} catch (ValidationException e) {
			assertEquals(ConverterException.class, e.getCause().getClass());
		}
		
		try {
			Validator.validateTerm(engine, "((1+2)*3");
			fail("Exception expected.");
		} catch (ValidationException e) {
			assertEquals(ScriptException.class, e.getCause().getClass());
		}
		
		try {
			Validator.validateTerm(engine, "((1+2)*3.0)");
		} catch (ValidationException e) {
			fail("Unexpected exception.");
		}
		
	}
	
	@Test
	public void testParseUri() {
		
		URI uri = Validator.parseUri("peraMIC.io");
		assertEquals(1, uri.getValidationErrors().size());
		assertEquals("Unsupported protocol: null", uri.getValidationErrors().get("uri"));
		
		uri = Validator.parseUri("ftp://peraMIC.io");
		assertEquals(1, uri.getValidationErrors().size());
		assertEquals("Unsupported protocol: ftp", uri.getValidationErrors().get("uri"));
		
		uri = Validator.parseUri("http://peraMIC.io:abc");
		assertEquals(1, uri.getValidationErrors().size());
		assertEquals("No host specified: http://peraMIC.io:abc", uri.getValidationErrors().get("uri"));
	
		uri = Validator.parseUri("http://admin:s3cret@peraMIC.io:8080/path?foo=bar");
		assertEquals("http", uri.getScheme());
		assertEquals("admin", uri.getUsername());
		assertEquals("s3cret", uri.getPassword());
		assertEquals("peraMIC.io", uri.getHost());
		assertEquals(8080, uri.getPort());
		assertEquals("/path", uri.getPath());
		assertEquals("foo=bar", uri.getQuery());
		
		uri = Validator.parseUri("https://admin:s3cret@peraMIC.io:8080/path?foo=bar");
		assertEquals("https", uri.getScheme());
		assertEquals("admin", uri.getUsername());
		assertEquals("s3cret", uri.getPassword());
		assertEquals("peraMIC.io", uri.getHost());
		assertEquals(8080, uri.getPort());
		assertEquals("/path", uri.getPath());
		assertEquals("foo=bar", uri.getQuery());
		
		uri = Validator.parseUri("mqtt://peraMIC.io");
		assertEquals(1, uri.getValidationErrors().size());
		assertEquals("No MQTT topic specified: mqtt://peraMIC.io", uri.getValidationErrors().get("uri"));
		
		uri = Validator.parseUri("mqtt://peraMIC.io/path");
		assertEquals(1, uri.getValidationErrors().size());
		assertEquals("No client ID specified: mqtt://peraMIC.io/path", uri.getValidationErrors().get("uri"));
		
		uri = Validator.parseUri("mqtt://peraMIC.io/path?clientid=foo");
		assertEquals("mqtt", uri.getScheme());
		assertEquals("peraMIC.io", uri.getHost());
		assertEquals("/path", uri.getTopic());
		assertEquals("foo", uri.getClientId());
		
		uri = Validator.parseUri("mqtts://admin:s3cret@peraMIC.io:8080/path?clientid=foo&qos=2");
		assertEquals("mqtts", uri.getScheme());
		assertEquals("admin", uri.getUsername());
		assertEquals("s3cret", uri.getPassword());
		assertEquals("peraMIC.io", uri.getHost());
		assertEquals(8080, uri.getPort());		
		assertEquals("/path", uri.getTopic());
		assertEquals("foo", uri.getClientId());
		assertEquals(2, uri.getQos());
		
		uri = Validator.parseUri("mqtts://admin:s3cret@peraMIC.io:8080/path/{epc}?clientid=foo&qos=2");
		assertEquals("mqtts", uri.getScheme());
		assertEquals("admin", uri.getUsername());
		assertEquals("s3cret", uri.getPassword());
		assertEquals("peraMIC.io", uri.getHost());
		assertEquals(8080, uri.getPort());		
		assertEquals("/path/{epc}", uri.getTopic());
		assertEquals("foo", uri.getClientId());
		assertEquals(2, uri.getQos());
	}
	
	@Test
	public void testCreateUri() {
		URI uri = Validator.createUri(null, null, null, null, null, null, null, null, null, null);
		assertEquals("Please enter a host.", uri.getValidationErrors().get("host"));
		assertEquals("Please select a protocol.", uri.getValidationErrors().get("type"));
		
		uri = Validator.createUri("ftp", null, null, null, null, null, null, null, null, null);
		assertEquals("Please enter a host.", uri.getValidationErrors().get("host"));
		assertEquals("Unsupported protocol: ftp", uri.getValidationErrors().get("type"));
		
		uri = Validator.createUri("http", "peraMIC.io", null, null, null, null, null, null, null, null);
		assertEquals("http://peraMIC.io", uri.getUriString());
		
		uri = Validator.createUri("http", "peraMIC.io", "abcd", null, null, null, null, null, null, null);
		assertEquals("Port must be a positive number.", uri.getValidationErrors().get("port"));
		
		uri = Validator.createUri("http", "peraMIC.io", "-80", null, null, null, null, null, null, null);
		assertEquals("Port must be a positive number.", uri.getValidationErrors().get("port"));
		
		uri = Validator.createUri("http", "peraMIC.io", "8080", null, null, null, null, null, null, null);
		assertEquals("http://peraMIC.io:8080", uri.getUriString());
		
		uri = Validator.createUri("http", "peraMIC.io", "8080", "admin", null, null, null, null, null, null);
		assertEquals("http://admin@peraMIC.io:8080", uri.getUriString());
		
		uri = Validator.createUri("http", "peraMIC.io", "8080", null, "s3cret", null, null, null, null, null);
		assertEquals("http://peraMIC.io:8080", uri.getUriString());
		
		uri = Validator.createUri("http", "peraMIC.io", "8080", "admin", "s3cret", null, null, null, null, null);
		assertEquals("http://admin:s3cret@peraMIC.io:8080", uri.getUriString());
		
		uri = Validator.createUri("http", "peraMIC.io", "8080", "admin", "s3cret", "/stairway/to/heaven", null, null, null, null);
		assertEquals("http://admin:s3cret@peraMIC.io:8080/stairway/to/heaven", uri.getUriString());
		
		uri = Validator.createUri("http", "peraMIC.io", "8080", "admin", "s3cret", "/stairway/to/heaven", "foo=bar&awesome", null, null, null);
		assertEquals("http://admin:s3cret@peraMIC.io:8080/stairway/to/heaven?foo=bar&awesome", uri.getUriString());
		
		uri = Validator.createUri("http", "peraMIC.io", "8080", "admin", "s3cret", "stairway/to/heaven", "foo=bar&awesome", "will", "be", "ignored");
		assertEquals("http://admin:s3cret@peraMIC.io:8080/stairway/to/heaven?foo=bar&awesome", uri.getUriString());
		
		uri = Validator.createUri("https", "peraMIC.io", "8080", "admin", "s3cret", "/stairway/to/heaven", "foo=bar&awesome", "will", "be", "ignored");
		assertEquals("https://admin:s3cret@peraMIC.io:8080/stairway/to/heaven?foo=bar&awesome", uri.getUriString());
		
		uri = Validator.createUri("mqtt", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", null, null, null);
		assertEquals("Please enter an MQTT topic.", uri.getValidationErrors().get("topic"));
		
		uri = Validator.createUri("mqtt", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", "/stairway/to/heaven", null, null);
		assertEquals("Please enter a client ID.", uri.getValidationErrors().get("clientId"));
		
		uri = Validator.createUri("mqtt", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", "/stairway/to/heaven", "fourty-two", null);
		assertEquals("mqtt://admin:s3cret@peraMIC.io:8080/stairway/to/heaven?clientid=fourty-two", uri.getUriString());
		
		uri = Validator.createUri("mqtt", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", "/stairway/to/heaven", "fourty-two", "-1");
		assertEquals("QoS must be a number between 0 and 2.", uri.getValidationErrors().get("qos"));
		
		uri = Validator.createUri("mqtt", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", "/stairway/to/heaven", "fourty-two", "5");
		assertEquals("QoS must be a number between 0 and 2.", uri.getValidationErrors().get("qos"));
		
		uri = Validator.createUri("mqtt", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", "/stairway/to/heaven", "fourty-two", "2");
		assertEquals("mqtt://admin:s3cret@peraMIC.io:8080/stairway/to/heaven?clientid=fourty-two&qos=2", uri.getUriString());
		
		uri = Validator.createUri("mqtts", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", "stairway/to/heaven", "fourty-two", "2");
		assertEquals("mqtts://admin:s3cret@peraMIC.io:8080/stairway/to/heaven?clientid=fourty-two&qos=2", uri.getUriString());
		
		uri = Validator.createUri("mqtts", "peraMIC.io", "8080", "admin", "s3cret", "/will/be/ignored", "will&be&ignored", "stairway/to/heaven/{epc}", "fourty-two", "2");
		assertEquals("mqtts://admin:s3cret@peraMIC.io:8080/stairway/to/heaven/{epc}?clientid=fourty-two&qos=2", uri.getUriString());
		
	}

}
