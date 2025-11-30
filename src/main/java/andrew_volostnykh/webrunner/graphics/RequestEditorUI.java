package andrew_volostnykh.webrunner.graphics;

import andrew_volostnykh.webrunner.service.persistence.definition.AbstractRequestDefinition;
import javafx.scene.Node;

public interface RequestEditorUI {
	Node getRoot();
	void loadRequest(AbstractRequestDefinition request);
	void saveChanges();
	void sendRequest();
	void cancelRequest();

	String fxmlTemplatePath();
}
