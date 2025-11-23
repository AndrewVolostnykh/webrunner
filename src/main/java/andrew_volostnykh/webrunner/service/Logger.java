package andrew_volostnykh.webrunner.service;

public class Logger {
	// TODO:
	//  implement logger container. in container should be stored all needed loggers which can be selected by
	//  request id
	private final StringBuilder logs = new StringBuilder();

	public void logMessage(String message) {
		System.err.println("Message appended " + message);
		logs
			.append(message);
	}

	public String getLogs() {
		return logs.toString();
	}
}
