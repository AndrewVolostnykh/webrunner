package andrew_volostnykh.webrunner.graphics;

import andrew_volostnykh.webrunner.graphics.controller.HttpRequestController;
import andrew_volostnykh.webrunner.service.persistence.RequestDefinition;

public class RequestUIFactory {

	public static RequestEditorUI create(
		RequestDefinition requestDefinition
	) {
		return switch (requestDefinition.getType()) {
			case HTTP_REQUEST -> new HttpRequestController();
//			case GRPC_REQUEST -> new GrpcRequestEditorUI();
//			case CHAIN -> new ChainRequestEditorUI();
			default ->
				throw new UnsupportedOperationException("Unknown request type");
		};
	}

}
