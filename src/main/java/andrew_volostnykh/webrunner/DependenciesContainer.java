package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.grphics.components.LogArea;
import andrew_volostnykh.webrunner.service.Logger;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;

public class DependenciesContainer {

	@Getter
	private static final ObjectMapper objectMapper = new ObjectMapper()
		.enable(
			SerializationFeature.INDENT_OUTPUT
		);

	private static final Logger logger = new Logger();

	public static VarsApplicator varsApplicator(
	) {
		return
			new VarsApplicator();
	}

	public static JsExecutorService jsExecutorService(
	) {
		return new JsExecutorService();
	}

	public static Logger logger() {
		return logger;
	}
}
