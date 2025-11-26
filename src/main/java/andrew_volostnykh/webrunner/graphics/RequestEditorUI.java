package andrew_volostnykh.webrunner.graphics;

import andrew_volostnykh.webrunner.service.persistence.RequestDefinition;
import javafx.scene.Node;

public interface RequestEditorUI {
	Node getRoot();
	void loadRequest(RequestDefinition request);
	void saveChanges();
	void sendRequest();
	void cancelRequest();
}
