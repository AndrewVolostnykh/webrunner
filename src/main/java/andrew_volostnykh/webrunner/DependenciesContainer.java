package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.service.grpc.GrpcReflectionService;
import andrew_volostnykh.webrunner.service.http.HttpRequestService;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.logs.request.LoggersContext;
import andrew_volostnykh.webrunner.service.logs.request.RequestLogger;
import andrew_volostnykh.webrunner.service.logs.request.RequestLoggerProvider;
import andrew_volostnykh.webrunner.service.persistence.NavigationTreePersistenceService;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;

public class DependenciesContainer {

	@Getter
	public static final ObjectMapper objectMapper = new ObjectMapper()
		.enable(SerializationFeature.INDENT_OUTPUT)
		.findAndRegisterModules();

	private static final RequestLoggerProvider requestLoggerProvider = new RequestLoggerProvider();

	private static final NavigationTreePersistenceService
		NAVIGATION_TREE_PERSISTANCE_SERVICE = new NavigationTreePersistenceService();
	private static final GrpcReflectionService GRPC_REFLECTION_SERVICE = new GrpcReflectionService();

	public static NavigationTreePersistenceService collectionPersistenceService() {
		return NAVIGATION_TREE_PERSISTANCE_SERVICE;
	}

	public static LoggersContext loggersContext() {
		return requestLoggerProvider;
	}

	public static RequestLogger logger() {
		return requestLoggerProvider;
	}

	public static GrpcReflectionService grpcReflectionService() {
		return GRPC_REFLECTION_SERVICE;
	}

	public static VarsApplicatorService varsApplicator(
	) {
		return
			new VarsApplicatorService();
	}

	public static HttpRequestService httpRequestService() {
		return
			new HttpRequestService();
	}

	public static JsExecutorService jsExecutorService(
	) {
		return new JsExecutorService();
	}
}
