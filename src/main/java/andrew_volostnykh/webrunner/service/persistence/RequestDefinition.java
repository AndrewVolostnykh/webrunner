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

	private String selectedService;
	private String selectedMethod;

	// HTTP constructor
	public RequestDefinition(
		String id,
		String name,
		String method,
		String url,
		Map<String, String> headers,
		String body,
		String varsDefinition,
		String onResponse,
		RequestType type
	) {
		this.id = id;
		this.name = name;
		this.method = method;
		this.url = url;
		this.headers = headers;
		this.body = body;
		this.varsDefinition = varsDefinition;
		this.onResponse = onResponse;
		this.type = type;
	}

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
			this.type,
			this.selectedService,
			this.selectedMethod
		);
	}
}
