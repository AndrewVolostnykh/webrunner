package andrew_volostnykh.webrunner.service.js;

import andrew_volostnykh.webrunner.DependenciesContainer;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.time.Instant;
import java.util.UUID;

class PredefinedFunctions {

	static void registerCommonFunctions(Context context) {
		context.getBindings("js").putMember("log", (ProxyExecutable) args -> {
			if (args.length > 0) {
				// TODO: run through all args
				String msg = args[0].asString();
				DependenciesContainer.logger().logMessage(msg);
			}
			return null;
		});

		context.getBindings("js").putMember("uuid", (ProxyExecutable) (_args) -> UUID.randomUUID().toString());
		context.getBindings("js").putMember("now", (ProxyExecutable) (_args) -> Instant.now().toString());
	}

	static void registerHelperFunctions(Context context) {
		context.getBindings("js").putMember("assert", (ProxyExecutable) args -> {
			if (args.length < 2) {
				throw new RuntimeException("assert(actual, expected, message?) requires at least 2 arguments");
			}

			Object actual = args[0].as(Object.class);
			Object expected = args[1].as(Object.class);
			String message = args.length > 2 ? args[2].asString() : "Assertion failed";

			if (!actual.equals(expected)) {
				throw new RuntimeException("❌ Assert failed: " + message +
											   " (expected " + expected + ", got " + actual + ")");
			}

			return "✔ Assert OK";
		});
	}
}
