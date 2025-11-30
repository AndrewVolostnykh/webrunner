package andrew_volostnykh.webrunner.graphics;

import andrew_volostnykh.webrunner.graphics.controller.GrpcRequestUIController;
import andrew_volostnykh.webrunner.graphics.controller.HttpRequestUIController;
import andrew_volostnykh.webrunner.service.persistence.definition.AbstractRequestDefinition;

public class RequestUIFactory {

	public static RequestEditorUI create(
		AbstractRequestDefinition requestDefinition
	) {
		return switch (requestDefinition.getType()) {
			case HTTP_REQUEST -> new HttpRequestUIController();
			case GRPC_REQUEST -> new GrpcRequestUIController();
//			case CHAIN -> new ChainRequestEditorUI();
			default -> throw new UnsupportedOperationException("Unknown request type");
		};
	}

}
