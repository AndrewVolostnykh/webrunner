package andrew_volostnykh.webrunner.service;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.service.logs.request.RequestLogger;

public class AbstractService {

	protected RequestLogger requestsLogger;

	protected AbstractService() {
		this.requestsLogger = DependenciesContainer.logger();
	}
}
