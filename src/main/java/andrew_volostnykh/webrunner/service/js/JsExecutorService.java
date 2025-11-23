package andrew_volostnykh.webrunner.service.js;

import andrew_volostnykh.webrunner.DependenciesContainer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

public class JsExecutorService {

	public Map<String, Object> executeJsVariables(String jsCode) {
		Context context = Context.newBuilder("js")
//			.allowIO(true)
//			.allowHostAccess(HostAccess.ALL)
//			.allowHostClassLookup(className -> true)
//			.option("engine.WarnInterpreterOnly", "false")
			.build();

			// Виконуємо JS
			Value result = context.eval("js", jsCode);

			// Очікуємо, що повернеться об'єкт `vars`
			Value vars = context.getBindings("js").getMember("vars");

			if (vars == null || vars.isNull()) {
				throw new IllegalArgumentException("JS must define a `vars` object!");
			}

			// Конвертуємо у Map<String, Object>
			Map<String, Object> map = new HashMap<>();
			for (String key : vars.getMemberKeys()) {
				map.put(key, vars.getMember(key).as(Object.class));
			}

			return map;
	}

	public void handleAfterResponse(String jsCode, String responseBody, Map<String, Object> vars) {

		try (Context context = Context.newBuilder("js")
			.allowAllAccess(true)
			.option("js.ecmascript-version", "2023")
			.build()) {


			// Передаємо response як об'єкт
			context.getBindings("js").putMember("response", parseResponse(responseBody));

			// Передаємо vars
			context.getBindings("js").putMember("vars", vars);

			// 🔹 Реєструємо predefined функції
			PredefinedFunctions.registerHelperFunctions(context);

			Value result = context.eval("js", jsCode);

			JsonNode node = null;
			String message = null;
			if (result.hasMembers()) {
				// JSON-like result
				node = DependenciesContainer.getObjectMapper().valueToTree(result.as(Map.class));
			} else {
				// fallback: просто текст
				message = result.asString();
			}

			System.err.println("RESULT!!!::: " + node + " !!!OR!!! " + message);

		} catch (Exception ex) {
			System.err.println("❌ JS AfterResponse error: " + ex.getMessage());
		}
	}

	private Map<String, Object> parseResponse(String responseBody) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(responseBody, new TypeReference<>() {});
	}
}
