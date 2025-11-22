package andrew_volostnykh.webrunner;

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

	@Getter
	private static final VarsApplicator varsApplicator = new VarsApplicator();
}
