package andrew_volostnykh.webrunner.service.js;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

class PredefinedFunctions {

	static void commonFuncs(Context context) {
		context.getBindings("js").putMember("log", (Consumer<Object>) System.out::println);
		context.getBindings("js").putMember("uuid", (ProxyExecutable) (_args) -> UUID.randomUUID().toString());
		context.getBindings("js").putMember("now", (ProxyExecutable) (_args) -> Instant.now().toString());
		context.getBindings("js").putMember("log", (ProxyExecutable) args -> {
			if (args.length > 0) {
				System.out.println("[JS LOG] " + args[0].asString());
			}
			return null;
		});
	}

	static void registerHelperFunctions(Context context) {
		context.getBindings("js").putMember("assert", new ProxyExecutable() {
			@Override
			public Object execute(Value... args) {
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
			}
		});
	}
}
