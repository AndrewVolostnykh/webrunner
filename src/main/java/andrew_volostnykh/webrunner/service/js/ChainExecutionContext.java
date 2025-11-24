package andrew_volostnykh.webrunner.service.js;

import lombok.Data;

import java.util.Map;

@Data
public class ChainExecutionContext {

	private final Map<String, Object> vars;
	private final Map<String, Object> executionContext;

}
