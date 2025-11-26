package andrew_volostnykh.webrunner.graphics.controller;

import andrew_volostnykh.webrunner.graphics.RequestEditorUI;
import andrew_volostnykh.webrunner.graphics.components.LogArea;
import andrew_volostnykh.webrunner.graphics.components.js_editor.JsCodeEditor;
import andrew_volostnykh.webrunner.graphics.components.json_editor.JsonCodeArea;
import andrew_volostnykh.webrunner.service.persistence.RequestDefinition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class GrpcRequestUIController implements RequestEditorUI {

	@FXML private TextField hostField;
	@FXML private ComboBox<String> serviceCombo;
	@FXML private ComboBox<String> methodCombo;
	@FXML private Button sendButton;
	@FXML private Button cancelButton;

	@FXML private VBox bodyContainer;
	private JsonCodeArea bodyArea;

	@FXML private VBox beforeRequestContainer;
	private JsCodeEditor beforeRequestCodeArea;

	@FXML private VBox afterResponseContainer;
	private JsCodeEditor afterResponseCodeArea;

	@FXML private TextArea responseArea;
	@FXML private Label statusLabel;
	@FXML private LogArea logsArea;

	@FXML
	public void initialize() {
		bodyArea = new JsonCodeArea();
		bodyContainer.getChildren().add(bodyArea);

		beforeRequestCodeArea = new JsCodeEditor();
		beforeRequestCodeArea.attachTo(beforeRequestContainer);

		afterResponseCodeArea = new JsCodeEditor();
		afterResponseCodeArea.attachTo(afterResponseContainer);

		// FIXME: should be button
		hostField.focusedProperty().addListener((obs, oldV, newV) -> {
			if (!newV) loadGrpcServices();
		});
	}

	private void loadGrpcServices() {

	}

	@FXML
	public void uploadLogs(Event e) {
		if (((Tab) e.getSource()).isSelected())
			Platform.runLater(() -> logsArea.setLogs());
	}

	public void clearLogs() {
		logsArea.clear();
	}

	@Override
	public Node getRoot() {
		return null;
	}

	@Override
	public void loadRequest(RequestDefinition request) {

	}

	@Override
	public void saveChanges() {

	}

	@Override
	public void sendRequest() {

	}

	@Override
	public void cancelRequest() {

	}

	@Override
	public String fxmlTemplatePath() {
		return "/ui/grpc_request_editor.fxml";
	}
}
