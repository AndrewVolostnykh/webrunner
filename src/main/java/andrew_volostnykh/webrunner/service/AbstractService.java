package andrew_volostnykh.webrunner.service;

import andrew_volostnykh.webrunner.DependenciesContainer;

public class AbstractService {

	protected Logger logger;

	protected AbstractService() {
		this.logger = DependenciesContainer.logger();
	}
}
