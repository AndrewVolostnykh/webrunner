package andrew_volostnykh.webrunner.service;

import andrew_volostnykh.webrunner.DependenciesContainer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextBeautifierService {

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

	public static String beautyString(Map<String, List<String>> body) {
		return body.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(e -> "%s: %s".formatted(e.getKey(), String.join(" ", e.getValue())))
			.collect(Collectors.joining(System.lineSeparator()));
	}
}
