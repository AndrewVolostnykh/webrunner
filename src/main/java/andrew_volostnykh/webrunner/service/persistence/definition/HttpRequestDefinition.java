package andrew_volostnykh.webrunner.service.persistence.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HttpRequestDefinition
	extends AbstractRequestDefinition {

	private String method;

	public HttpRequestDefinition(
		String name
	) {
		super(
			name,
			"",
			"",
			"",
			new HashMap<>(),
			""
		);
		this.method = "GET";
	}

	public HttpRequestDefinition(
		String name,
		String url,
		String beforeRequest,
		String afterRequest,
		Map<String, List<String>> headers,
		String body,
		String method
	) {
		super(
			name,
			url,
			beforeRequest,
			afterRequest,
			headers,
			body
		);
		this.method = method;
	}

	@Override
	public AbstractRequestDefinition deepCopy() {
		return
			new HttpRequestDefinition(
				name,
				url,
				beforeRequest,
				afterRequest,
				new HashMap<>(headers),
				body,
				method
			);
	}

	@Override
	public RequestType getType() {
		return RequestType.HTTP_REQUEST;
	}
}
