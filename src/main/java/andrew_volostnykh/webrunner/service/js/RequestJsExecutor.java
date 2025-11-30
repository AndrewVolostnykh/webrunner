package andrew_volostnykh.webrunner.service.js;

import andrew_volostnykh.webrunner.lang.Immutable;
import lombok.Getter;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestJsExecutor {

	@Immutable
	private final Map<String, Object> env;
	@Immutable
	private final Map<String, Object> chain;

	private final Context context;

	@Getter
	private Map<String, Object> localContext = new HashMap<>();

	private Map<String, String> requestHeaders;
	private String body;
	private String responseBody;
	private Map<String, List<String>> responseHeaders;
	private int statusCode;

	@Getter
	private Map<String, Object> shared;
	@Getter
	private Map<String, Object> vars;

	public RequestJsExecutor(
		Context context
	) {
		this.env = Map.of();
		this.chain = Map.of();
		this.shared = Map.of();
		this.context = context;
	}

	public void executeBeforeRequest(
		String jsCode,
		Map<String, String> requestHeaders,
		String body
	) {
		Value bindings = context.getBindings("js");

		bindings.putMember("env", ProxyObject.fromMap(env));
		bindings.putMember("chain", ProxyObject.fromMap(chain));
		bindings.putMember("shared", ProxyObject.fromMap(shared));

		localContext.put("body", body);
		localContext.put("requestHeaders", requestHeaders);

		bindings.putMember("local", ProxyObject.fromMap(localContext));

		context.eval("js", jsCode);

		Value vars = context.getBindings("js").getMember("vars");
		Value shared = context.getBindings("js").getMember("shared");

		if (shared != null && !shared.isNull()) {
			this.shared = convertValueToMap(shared);
		}

		if (vars != null && !vars.isNull()) {
			this.vars = convertValueToMap(vars);
		}
	}

	public void executeAfterRequest(
		String jsCode,
		String responseBody,
		Map<String, List<String>> responseHeaders,
		int responseCode
	) {
		Value bindings = context.getBindings("js");

		bindings.putMember("env", ProxyObject.fromMap(env));
		bindings.putMember("chain", ProxyObject.fromMap(chain));
		bindings.putMember("shared", ProxyObject.fromMap(shared));

		localContext.put("responseBody", responseBody);
		localContext.put("responseHeaders", responseHeaders);
		localContext.put("statusCode", responseCode);

		bindings.putMember("local", ProxyObject.fromMap(localContext));

		context.eval("js", jsCode);

		Value vars = context.getBindings("js").getMember("vars");
		Value shared = context.getBindings("js").getMember("shared");

		if (shared != null && !shared.isNull()) {
			this.shared = convertValueToMap(shared);
		}

		if (vars != null && !vars.isNull()) {
			this.vars = convertValueToMap(vars);
		}
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
