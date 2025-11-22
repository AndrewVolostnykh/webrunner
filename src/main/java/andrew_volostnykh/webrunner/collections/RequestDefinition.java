package andrew_volostnykh.webrunner.collections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDefinition {
	private String name;
	private String method;
	private String url;
	private Map<String, String> headers;
	private String body;
}
