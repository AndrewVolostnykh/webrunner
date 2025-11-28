package andrew_volostnykh.webrunner.graphics.controller;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.graphics.RequestEditorUI;
import andrew_volostnykh.webrunner.graphics.components.LogArea;
import andrew_volostnykh.webrunner.graphics.components.js_editor.JsCodeEditor;
import andrew_volostnykh.webrunner.graphics.components.json_editor.JsonCodeArea;
import andrew_volostnykh.webrunner.service.TextFormatterService;
import andrew_volostnykh.webrunner.service.persistence.RequestDefinition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class GrpcRequestUIController implements RequestEditorUI {

	@FXML private TextField hostField;
	@FXML private ComboBox<String> serviceCombo;
	@FXML private ComboBox<String> methodCombo;
	@FXML private Button loadServicesButton;

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

		serviceCombo.setDisable(true);
		methodCombo.setDisable(true);

		serviceCombo.setPrefWidth(350);
		methodCombo.setPrefWidth(280);

		serviceCombo.setMaxWidth(Region.USE_PREF_SIZE);
		methodCombo.setMaxWidth(Region.USE_PREF_SIZE);
	}

	@FXML
	public void loadServices() {
		String host = hostField.getText();
		if (host == null || host.isBlank()) {
			statusLabel.setText("⚠ Please enter host first");
			return;
		}

		serviceCombo.setDisable(true);
		methodCombo.setDisable(true);
		statusLabel.setText("⏳ Loading services...");

		DependenciesContainer.grpcReflectionService().getAllServicesAndMethodsAsync(host)
			.thenAccept(map -> Platform.runLater(() -> {
				serviceCombo.setItems(FXCollections.observableArrayList(map.keySet()));
				serviceCombo.setDisable(false);
				statusLabel.setText("✔ Services loaded");

				serviceCombo.setCellFactory(listView -> new ListCell<>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? null : item); // показати повне ім'я
					}
				});
				serviceCombo.setButtonCell(new ListCell<>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? null : TextFormatterService.shortName(item)); // коротке ім'я в полі
					}
				});

				serviceCombo.getSelectionModel().selectedItemProperty().addListener((obs2, old, selectedService) -> {
					methodCombo.setItems(FXCollections.observableArrayList(map.get(selectedService)));
					methodCombo.setDisable(false);

					methodCombo.setCellFactory(listView -> new ListCell<>() {
						@Override
						protected void updateItem(String item, boolean empty) {
							super.updateItem(item, empty);
							setText(empty || item == null ? null : item); // повне ім'я в списку
						}
					});
					methodCombo.setButtonCell(new ListCell<>() {
						@Override
						protected void updateItem(String item, boolean empty) {
							super.updateItem(item, empty);
							setText(empty || item == null ? null : TextFormatterService.shortName(item)); // коротке ім'я в полі
						}
					});

					methodCombo.setOnShowing(e ->
												 Optional.ofNullable(methodCombo.lookup(".combo-box-popup"))
													 .ifPresent(p -> p.setStyle("-fx-pref-width: 600px;"))
					);
				});

				serviceCombo.setOnShowing(e ->
											  Optional.ofNullable(serviceCombo.lookup(".combo-box-popup"))
												  .ifPresent(p -> p.setStyle("-fx-pref-width: 650px;"))
				);
			}))
			.exceptionally(ex -> {
				Platform.runLater(() -> statusLabel.setText("❌ " + ex.getMessage()));
				ex.printStackTrace(); // TODO: remove
				return null;
			});
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
