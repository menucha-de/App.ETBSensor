package havis.app.etb.sensor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.DecimalFormatSymbols;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Test;

public class ConverterTest {

	final static char COMMA = DecimalFormatSymbols.getInstance().getDecimalSeparator();

	@Test
	public void testEval() throws ConverterException {

		ScriptEngine engine = new ScriptEngineManager().getEngineByName(Converter.ENGINE_NAME);
		Converter conv = Converter.getInstance();

		float value = new Float(Math.PI);
		String term = "Math.round(Math.cos(value) * Math.sin(value))";
		float result = conv.eval(engine, term, value);
		assertEquals((Float) 0.0f, (Float) result);

		value = -1.0f;
		term = "Math.sqrt(value)";
		result = conv.eval(engine, term, value);
		assertEquals((Float) Float.NaN, (Float) result);

		value = 0.0f;
		term = "Math.sqrt(value";
		try {
			conv.eval(engine, term, value);
			fail("Exception expected");
		} catch (ConverterException ce) {
		}

		term = "";
		try {
			conv.eval(engine, "=", value);
			fail("Exception expected");
		} catch (ConverterException ce) {
		}

		try {
			conv.eval(engine, "{", value);
			fail("Exception expected");
		} catch (ConverterException ce) {
		}

		try {
			conv.eval(engine, "}", value);
			fail("Exception expected");
		} catch (ConverterException ce) {
		}

		try {
			conv.eval(engine, ";", value);
			fail("Exception expected");
		} catch (ConverterException ce) {
		}

		try {
			conv.eval(engine, "function funny(x){ alert('Hello ' + x); }", value);
			fail("Exception expected");
		} catch (ConverterException ce) {
		}
	}

	@Test
	public void testParse() throws ConverterException {
		Converter converter = Converter.getInstance();

		assertEquals(new Float(0xAAAA), new Float(converter.parse("16:xAAAA")));
		assertEquals(new Float(0xAAAABBBB), new Float(converter.parse("32:xAAAABBBB")));
		try {
			converter.parse("64:xAAAABBBBCCCCDDDD");
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
		}
	}

}
