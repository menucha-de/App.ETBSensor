package havis.app.etb.sensor;

import java.nio.ByteBuffer;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class Converter {

	public final static String ENGINE_NAME = "javascript";

	private final static String REGEX_INVALID_CHARS = ".*[{}=;].*";
	private final static Converter instance = new Converter(); 

	public static Converter getInstance() {
		return instance;
	}

	private Converter() {
		super();
	}

	public float eval(ScriptEngine engine, String term, float value) throws ConverterException {
		if (engine == null)
			throw new ConverterException("Script engine '" + ENGINE_NAME + "' was not found.");			
		
		try {			
			if (term.matches(REGEX_INVALID_CHARS))
				throw new ConverterException("Term contains one or more invalid characters ('{', '}', '=' or ';').");
			
			return new Float((double)engine.eval("var value="+value+";" + term)).floatValue();			
		} catch (ScriptException e) {
			throw new ConverterException(e);
		} 
	}

	public float eval(String term, float value) throws ConverterException {
		return eval(ScriptEngineFactory.getFactory().getEngine(), term, value);
	}

	public float parse(String hexString) {		
		String[] parts = hexString.split(":");
		String hexStr = parts[1].substring(1);		
		byte[] data = hexToBytes(hexStr);
		if (data.length <= 4)
			return ByteBuffer.wrap(resize(data, 4)).getInt();
		else if (data.length > 4 && data.length <= 8)
			return (float) ByteBuffer.wrap(resize(data, 8)).getDouble();
		throw new IllegalArgumentException("Invalid data format");
	}

	private byte[] resize(byte[] data, int size) {
		byte[] result = new byte[size];
		for (int i = 0; i < data.length; i++)
			result[i + size - data.length] = data[i];
		return result;
	}

	private byte[] hexToBytes(String hexStr) throws IllegalArgumentException {
		hexStr = hexStr.replaceAll("\\s|_", "");
		if (hexStr.length() % 2 != 0)
			throw new IllegalArgumentException(
					"Hex string must have an even number of characters.");

		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length(); i += 2)
			result[i / 2] = Integer.decode(
					"0x" + hexStr.charAt(i) + hexStr.charAt(i + 1)).byteValue();

		return result;
	}
}
