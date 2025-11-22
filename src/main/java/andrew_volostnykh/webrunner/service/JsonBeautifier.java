package andrew_volostnykh.webrunner.service;

import andrew_volostnykh.webrunner.DependenciesContainer;

public class JsonBeautifier {

	public static String formatJson(String json) {
		try {
			Object obj = DependenciesContainer.getObjectMapper()
				.readValue(json, Object.class);
			return DependenciesContainer.getObjectMapper()
				.writeValueAsString(obj);
		} catch (Exception e) {
			return json;
		}
	}
}
