package andrew_volostnykh.webrunner.service.logs.request;

public interface RequestLogger {

	void logMessage(String message);

	String getLogs();

	void clearLogs();
}
