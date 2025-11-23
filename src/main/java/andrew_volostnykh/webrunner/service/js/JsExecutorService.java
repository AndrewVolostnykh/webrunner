package andrew_volostnykh.webrunner.service.js;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.service.AbstractService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

// TODO: add global scope variables
public class JsExecutorService extends AbstractService {

	public Map<String, Object> executeJsVariables(String jsCode) {
		Context context = Context.newBuilder("js")
			.build();

		Value result = context.eval("js", jsCode);

		Value vars = context.getBindings("js").getMember("vars");

		if (vars == null || vars.isNull()) {
			throw new IllegalArgumentException("JS must define a `vars` object!");
		}

		Map<String, Object> map = new HashMap<>();
		for (String key : vars.getMemberKeys()) {
			map.put(key, vars.getMember(key).as(Object.class));
		}

		return map;
	}

	public void handleAfterResponse(
		String jsCode,
		String responseBody,
		Map<String, Object> vars
	) {

		try (
			Context context = Context.newBuilder("js")
				.allowAllAccess(true)
				.option("js.ecmascript-version", "2023")
				.build()
		) {

			context.getBindings("js").putMember("response", parseResponse(responseBody));
			// TODO: add headers

			context.getBindings("js").putMember("vars", vars);

			PredefinedFunctions.registerHelperFunctions(context);
			PredefinedFunctions.registerCommonFunctions(context);

			Value result = context.eval("js", jsCode);

			if (result.isNull()) {
				return;
			}

			if (result.hasMembers()) {
				logger.logMessage(
					DependenciesContainer.getObjectMapper()
						.valueToTree(
							result.as(Map.class)
						)
						.toString()
				);
			} else {
				logger.logMessage(result.asString());
			}

		} catch (Exception ex) {
			logger.logMessage("ERROR: JS AfterResponse error: " + ex.getMessage());
		}
	}

	private Map<String, Object> parseResponse(String responseBody) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(responseBody, new TypeReference<>() {
		});
	}
}
