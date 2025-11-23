package andrew_volostnykh.webrunner.grphics.components.js_editor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Formatter {

	private static final ObjectMapper mapper = new ObjectMapper();

	public static String formatJson(String text) throws Exception {
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(text));
	}
}
