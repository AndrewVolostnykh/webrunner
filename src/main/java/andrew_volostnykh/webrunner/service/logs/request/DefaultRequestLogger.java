package andrew_volostnykh.webrunner.service.logs.request;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DefaultRequestLogger implements RequestLogger {
	private final StringBuilder logs = new StringBuilder();

	public void logMessage(String message) {
		System.err.println("Message appended " + message);
		logs
			.append(message);
	}

	public String getLogs() {
		return logs.toString();
	}

	public void clearLogs() {
		logs.delete(0, logs.length());
	}
}
