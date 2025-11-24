package andrew_volostnykh.webrunner.service.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
// will be replaced with Abstract and implementations: Http, Grpc, Chain, etc
public class RequestDefinition {
	private String name;
	private String method;
	private String url;
	private Map<String, String> headers;
	private String body;
	private String varsDefinition;
	private RequestType type;
}
