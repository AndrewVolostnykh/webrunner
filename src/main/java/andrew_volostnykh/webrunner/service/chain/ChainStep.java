package andrew_volostnykh.webrunner.service.chain;

import andrew_volostnykh.webrunner.service.persistence.RequestDefinition;
import lombok.Data;

@Data
public class ChainStep {

	private String name;
	private String requestId;
	private RequestDefinition request;
	private String beforeJs;
	private String afterJs;
}
