package andrew_volostnykh.webrunner.service.http;

import java.util.List;
import java.util.Map;

public record HttpResponseData(
	int statusCode,
	Map<String, List<String>> headers,
	String body
) {
}
