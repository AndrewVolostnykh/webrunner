package andrew_volostnykh.webrunner.service;

import andrew_volostnykh.webrunner.DependenciesContainer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextFormatterService {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^{}]+)}}");

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

	public static String shortName(String fullName) {
		if (fullName == null) return null;
		return fullName.substring(fullName.lastIndexOf('.') + 1);
	}

	public static String formatJsonWithPlaceholders(String json) {
		Map<String, String> placeholderMap = new LinkedHashMap<>();

		Matcher matcher = PLACEHOLDER_PATTERN.matcher(json);
		StringBuilder tempJson = new StringBuilder();
		int i = 0;
		while (matcher.find()) {
			String key = matcher.group(1).trim();
			String replacement = "\"__PLACEHOLDER_" + (i++) + "__\"";
			placeholderMap.put(replacement, "{{" + key + "}}");
			matcher.appendReplacement(tempJson, replacement.replace("$", "\\$"));
		}
		matcher.appendTail(tempJson);

		String jsonSafe = tempJson.toString();

		String formatted;
		try {
			Object obj = DependenciesContainer.getObjectMapper()
				.readValue(jsonSafe, Object.class);
			formatted = DependenciesContainer.getObjectMapper()
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(obj);
		} catch (Exception e) {
			return json;
		}

		for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
			formatted = formatted.replace(entry.getKey(), entry.getValue());
		}

		return formatted;
	}

	public static String beautyString(Map<String, List<String>> body) {
		return body.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(e -> "%s: %s".formatted(e.getKey(), String.join(" ", e.getValue())))
			.collect(Collectors.joining(System.lineSeparator()));
	}
}
