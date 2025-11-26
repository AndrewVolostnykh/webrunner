package andrew_volostnykh.webrunner.service.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
// will be replaced with Abstract and implementations: Http, Grpc, Chain, etc
public class RequestDefinition {
	private String id;
	private String name;
	private String method;
	private String url;
	private Map<String, String> headers;
	private String body;
	private String varsDefinition;
	private String onResponse;
	private RequestType type;

	public RequestDefinition deepCopy() {
		return new RequestDefinition(
			this.id,
			this.name,
			this.method,
			this.url,
			new HashMap<>(this.headers),
			this.body,
			this.varsDefinition,
			this.onResponse,
			this.type
		);
	}
}
