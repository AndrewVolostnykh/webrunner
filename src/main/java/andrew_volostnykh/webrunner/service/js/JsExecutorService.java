package andrew_volostnykh.webrunner.service.js;

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
}
