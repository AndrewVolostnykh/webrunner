package andrew_volostnykh.webrunner.service.js;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LocalExecutionContext {

	private final Map<String, Object> vars;
	private final String responseBody;
	private final Map<String, List<String>> headers;
	private final int statusCode;
}
