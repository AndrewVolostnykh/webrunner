package andrew_volostnykh.webrunner.service.grpc;

import com.google.protobuf.Descriptors.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrpcMethodDefinition {

	private String name;
	private Descriptor inputType;
	private Descriptor outputType;

}
