package andrew_volostnykh.webrunner.service.js;

import andrew_volostnykh.webrunner.service.AbstractService;
import org.graalvm.polyglot.Context;

public class JsExecutorService extends AbstractService {

	public static RequestJsExecutor requestExecutor() {
		Context context = Context.newBuilder("js")
			.build();

		PredefinedFunctions.registerHelperFunctions(context);
		PredefinedFunctions.registerCommonFunctions(context);

		return
			new RequestJsExecutor(context);
	}
}
