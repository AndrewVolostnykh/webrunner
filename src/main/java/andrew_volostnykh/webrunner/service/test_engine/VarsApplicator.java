package andrew_volostnykh.webrunner.service.test_engine;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.service.AbstractService;

import java.util.Map;

public class VarsApplicator extends AbstractService {

	public String applyVariables(
		String jsonBody,
		Map<String, Object> vars
	) {
		try {
			String result = jsonBody;

			for (Map.Entry<String, Object> v : vars.entrySet()) {
				String placeholder = "{{" + v.getKey() + "}}";

				if (v.getValue() instanceof String ||
					v.getValue() instanceof Number ||
					v.getValue() instanceof Boolean) {
					result = result.replace(
						placeholder,
						"\"" + v.getValue() + "\""
					);
				} else {
					String jsonValue = DependenciesContainer.getObjectMapper().writeValueAsString(v.getValue());
					result = result.replace(placeholder, jsonValue);
				}
			}

			return DependenciesContainer.getObjectMapper()
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(
					DependenciesContainer.getObjectMapper().readTree(result)
				);

		} catch (Exception ex) {
			requestsLogger.logMessage("ERROR: " + ex.getMessage());
			return jsonBody;
		}
	}
}
