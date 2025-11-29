package andrew_volostnykh.webrunner.service.js;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.service.AbstractService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsExecutorService extends AbstractService {

	public Map<String, Object> executeJsAfterRequest(
		String jsCode,
		Map<String, Object> vars,
		String response
	) {
		return
			executeJsAfterRequest(
				jsCode,
				vars,
				response,
				Map.of(),
				0
			);
	}

	public Map<String, Object> executeJsAfterRequest(
		String jsCode,
		Map<String, Object> vars,
		String responseBody,
		Map<String, List<String>> headers,
		int statusCode
	) {
		return executeJsAfterRequest(
			jsCode, Map.of(), vars, responseBody, headers, statusCode
		);
	}

	public Map<String, Object> executeJsAfterRequest(
		String jsCode,
		Map<String, Object> globalContext,
		Map<String, Object> vars,
		String responseBody,
		Map<String, List<String>> headers,
		int statusCode
	) {
		LocalExecutionContext localExecutionContext = new LocalExecutionContext(
			vars, responseBody, headers, statusCode
		);

		Map<String, Object> jsonLocalExecutionContext = DependenciesContainer.getObjectMapper()
			.convertValue(localExecutionContext, new TypeReference<>() {
			});

		Context context = Context.newBuilder("js")
			.build();

		PredefinedFunctions.registerHelperFunctions(context);
		PredefinedFunctions.registerCommonFunctions(context);

		Value bindings = context.getBindings("js");
		bindings.putMember("local", ProxyObject.fromMap(jsonLocalExecutionContext));
		bindings.putMember("globalContext", ProxyObject.fromMap(globalContext));

		context.eval("js", jsCode);

		Value localContext = context.getBindings("js").getMember("local");

		return convertValueToMap(localContext);
	}

	public Map<String, Object> executeJsVariables(String jsCode) {
		return
			this.executeJsVariables(jsCode, Map.of());
	}

	public Map<String, Object> executeJsVariables(
		String jsCode,
		Map<String, Object> chainGlobalVars
	) {
		Context context = Context.newBuilder("js")
			.build();

		PredefinedFunctions.registerHelperFunctions(context);
		PredefinedFunctions.registerCommonFunctions(context);

		Value bindings = context.getBindings("js");
		bindings.putMember("chainGlobalVars", chainGlobalVars);

		context.eval("js", jsCode);

		Value vars = context.getBindings("js").getMember("vars");

		if (vars == null || vars.isNull()) {
			return Map.of();
		}

		return convertValueToMap(vars);
	}

	private Map<String, Object> convertValueToMap(
		Value value
	) {
		Map<String, Object> map = new HashMap<>();
		for (String key : value.getMemberKeys()) {
			map.put(key, value.getMember(key).as(Object.class));
		}

		return map;
	}
}
