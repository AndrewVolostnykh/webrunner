package andrew_volostnykh.webrunner.graphics;

import andrew_volostnykh.webrunner.service.persistence.definition.AbstractRequestDefinition;

public interface RequestEditorUI {

	void loadRequest(AbstractRequestDefinition request);

	void sendRequest();

	void cancelRequest();

	String fxmlTemplatePath();
}
