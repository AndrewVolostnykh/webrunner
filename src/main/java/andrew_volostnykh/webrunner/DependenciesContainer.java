package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.service.Logger;
import andrew_volostnykh.webrunner.service.http.HttpRequestService;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.persistence.NavigationTreePersistanceService;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;

// Make it autowirable?
public class DependenciesContainer {

	@Getter
	private static final ObjectMapper objectMapper = new ObjectMapper()
		.enable(
			SerializationFeature.INDENT_OUTPUT
		);

	private static final Logger logger = new Logger();
	private static final NavigationTreePersistanceService
		NAVIGATION_TREE_PERSISTANCE_SERVICE = new NavigationTreePersistanceService();

	public static NavigationTreePersistanceService collectionPersistenceService() {
		return NAVIGATION_TREE_PERSISTANCE_SERVICE;
	}

	// FIXME: replace factory with single instance
	public static VarsApplicator varsApplicator(
	) {
		return
			new VarsApplicator();
	}

	public static HttpRequestService httpRequestService() {
		return
			new HttpRequestService();
	}

	public static JsExecutorService jsExecutorService(
	) {
		return new JsExecutorService();
	}

	public static Logger logger() {
		return logger;
	}
}
