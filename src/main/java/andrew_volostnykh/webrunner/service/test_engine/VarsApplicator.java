package andrew_volostnykh.webrunner.service.test_engine;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarsApplicator {
	private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{(.+?)}}");

	// FIXME: invalid arch
	public String applyVariables(String body, Map<String, String> values) {
		Matcher matcher = VAR_PATTERN.matcher(body);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String varName = matcher.group(1).trim();
			String replacement = values.getOrDefault(varName, "undefined");
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
