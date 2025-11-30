package andrew_volostnykh.webrunner.service.logs.request;

import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class RequestLoggerProvider
	implements RequestLogger, LoggersContext {

	private static final Map<String, DefaultRequestLogger> REGISTERED_LOGGERS = new HashMap<>();

	@Setter
	private String currentRequest;

	private DefaultRequestLogger getDefaultLogger() {
		return
			REGISTERED_LOGGERS.computeIfAbsent(
				currentRequest,
				k -> new DefaultRequestLogger()
			);
	}

	@Override
	public void logMessage(String message) {
		getDefaultLogger().logMessage(message);
	}

	@Override
	public String getLogs() {
		return getDefaultLogger().getLogs();
	}

	@Override
	public void clearLogs() {
		getDefaultLogger().clearLogs();
	}
}
