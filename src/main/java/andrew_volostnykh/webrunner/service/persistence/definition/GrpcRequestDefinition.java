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
public class GrpcRequestDefinition
	extends AbstractRequestDefinition {

	private String selectedService;
	private String selectedMethod;

	public GrpcRequestDefinition(
		String name
	) {
		super(
			name,
			"",
			"",
			"afterRequest",
			new HashMap<>(),
			""
		);

		this.selectedService = "";
		this.selectedMethod = "";
	}

	public GrpcRequestDefinition(
		String name,
		String url,
		String beforeRequest,
		String afterRequest,
		Map<String, List<String>> headers,
		String body,
		String selectedService,
		String selectedMethod
	) {
		super(
			name,
			url,
			beforeRequest,
			afterRequest,
			headers,
			body
		);

		this.selectedService = selectedService;
		this.selectedMethod = selectedMethod;
	}

	@Override
	public AbstractRequestDefinition deepCopy() {
		return
			new GrpcRequestDefinition(
				name,
				url,
				beforeRequest,
				afterRequest,
				new HashMap<>(headers),
				body,
				selectedService,
				selectedMethod
			);
	}

	@Override
	public RequestType getType() {
		return RequestType.GRPC_REQUEST;
	}
}
