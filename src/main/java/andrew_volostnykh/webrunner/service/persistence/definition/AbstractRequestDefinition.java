package andrew_volostnykh.webrunner.service.persistence.definition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = As.EXISTING_PROPERTY,
	property = "type"
)
@JsonSubTypes({
	@Type(
		value = GrpcRequestDefinition.class,
		name = "GRPC_REQUEST"
	),
	@Type(
		value = HttpRequestDefinition.class,
		name = "HTTP_REQUEST"
	)})
public abstract class AbstractRequestDefinition {

	protected String id;
	protected String name;
	protected String url;
	protected String beforeRequest;
	protected String afterRequest;
	protected Map<String, List<String>> headers;
	protected String body;

	protected AbstractRequestDefinition(
		String name,
		String url,
		String beforeRequest,
		String afterRequest,
		Map<String, List<String>> headers,
		String body
	) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.url = url;
		this.beforeRequest = beforeRequest;
		this.afterRequest = afterRequest;
		this.headers = headers;
		this.body = body;
	}

	public abstract AbstractRequestDefinition deepCopy();

	public abstract RequestType getType();
}
