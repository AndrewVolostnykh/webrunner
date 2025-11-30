package andrew_volostnykh.webrunner.graphics.controller;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.graphics.RequestEditorUI;
import andrew_volostnykh.webrunner.graphics.components.LogArea;
import andrew_volostnykh.webrunner.graphics.components.js_editor.JsCodeEditor;
import andrew_volostnykh.webrunner.graphics.components.json_editor.JsonCodeArea;
import andrew_volostnykh.webrunner.service.TextFormatterService;
import andrew_volostnykh.webrunner.service.http.HttpRequestService;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.js.RequestJsExecutor;
import andrew_volostnykh.webrunner.service.persistence.NavigationTreePersistenceService;
import andrew_volostnykh.webrunner.service.persistence.RequestDefinition;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.NoArgsConstructor;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor
public class HttpRequestUIController implements RequestEditorUI {

	@FXML
	private ComboBox<String> methodCombo;
	@FXML
	private TextField urlField;

	@FXML
	private Button sendButton;
	@FXML
	private Button cancelButton;

	@FXML
	private VBox bodyContainer;
	private JsonCodeArea bodyArea;

	@FXML
	private VBox headersList;
	@FXML
	private TextField headerKeyField;
	@FXML
	private TextField headerValueField;

	@FXML
	private VBox beforeRequestContainer;
	private JsCodeEditor beforeRequestCodeArea;

	@FXML
	private VBox afterResponseContainer;
	private JsCodeEditor afterResponseCodeArea;

	@FXML
	private TextArea responseArea;
	@FXML
	private TextArea responseHeaders;
	@FXML
	private Label statusLabel;
	@FXML
	private LogArea logsArea;

	private final ObservableList<Entry<String, String>> headers = FXCollections.observableArrayList();
	private ListChangeListener<Entry<String, String>> headersListener;
	private ChangeListener<String> methodListener,
		urlListener,
		bodyListener,
		beforeRequestAreaListener,
		afterResponseAreaListener;

	private CompletableFuture<?> requestRunner; // TODO: move to request service

	private final NavigationTreePersistenceService persistenceService =
		DependenciesContainer.collectionPersistenceService();

	private RequestDefinition request;

	private final HttpRequestService httpService = DependenciesContainer.httpRequestService();
	private final JsExecutorService jsExecutorService = DependenciesContainer.jsExecutorService();
	private final VarsApplicator varsApplicator = DependenciesContainer.varsApplicator();

	@FXML
	public void initialize() {
		bodyArea = new JsonCodeArea();
		bodyContainer.getChildren().add(bodyArea);

		beforeRequestCodeArea = new JsCodeEditor();
		beforeRequestCodeArea.attachTo(beforeRequestContainer);

		afterResponseCodeArea = new JsCodeEditor();
		afterResponseCodeArea.attachTo(afterResponseContainer);

		methodCombo.setItems(FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE"));
		methodCombo.getSelectionModel().select("GET");
		sendButton.setOnAction(e -> sendRequest());
		cancelButton.setOnAction(e -> cancelRequest());

		Button beautifyBtn = new Button("Beautify JSON");
		beautifyBtn.setOnAction(e -> bodyArea.beautifyBody());
		bodyContainer.getChildren().add(beautifyBtn);
	}

	@Override
	public Node getRoot() {
		return methodCombo.getScene() != null
			? methodCombo.getScene().getRoot()
			: null;
	}

	@Override
	public void loadRequest(RequestDefinition req) {
		this.request = req;

		if (methodListener != null) {
			methodCombo.valueProperty().removeListener(methodListener);
		}
		if (urlListener != null) {
			urlField.textProperty().removeListener(urlListener);
		}
		if (bodyListener != null) {
			bodyArea.textProperty().removeListener(bodyListener);
		}
		if (headersListener != null) {
			headers.removeListener(headersListener);
		}
		if (beforeRequestAreaListener != null) {
			beforeRequestCodeArea.textProperty()
				.removeListener(beforeRequestAreaListener);
		}
		if (afterResponseAreaListener != null) {
			afterResponseCodeArea.textProperty()
				.removeListener(afterResponseAreaListener);
		}

		methodCombo.setValue(req.getMethod());
		urlField.setText(req.getUrl());
		bodyArea.replaceText(req.getBody());

		headers.clear();
		headersList.getChildren().clear();
		req.getHeaders().forEach((k, v) -> {
			var entry = new AbstractMap.SimpleEntry<>(k, v);
			headers.add(entry);
			addHeaderRowToUi(entry);
		});

		methodListener = (obs, old, val) -> {
			req.setMethod(val);
			persistenceService.save();
		};
		urlListener = (obs, old, val) -> {
			req.setUrl(val);
			persistenceService.save();
		};
		bodyListener = (obs, old, val) -> {
			req.setBody(val);
			persistenceService.save();
		};
		methodCombo.valueProperty().addListener(methodListener);
		urlField.textProperty().addListener(urlListener);
		bodyArea.textProperty().addListener(bodyListener);

		headersListener = change -> {
			req.getHeaders().clear();
			headers.forEach(entry -> req.getHeaders().put(entry.getKey(), entry.getValue()));
			persistenceService.save();
		};
		headers.addListener(headersListener);

		beforeRequestCodeArea.replaceText(req.getVarsDefinition());
		beforeRequestAreaListener = (obs, old, val) -> {
			req.setVarsDefinition(val);
			persistenceService.save();
		};
		beforeRequestCodeArea.textProperty().addListener(beforeRequestAreaListener);

		afterResponseCodeArea.replaceText(req.getOnResponse());
		afterResponseAreaListener = (obs, old, val) -> {
			req.setOnResponse(val);
			persistenceService.save();
		};
		afterResponseCodeArea.textProperty().addListener(afterResponseAreaListener);
	}

	@Override
	public void saveChanges() {

	}

	@Override
	public void sendRequest() {
		if (requestRunner != null && !requestRunner.isDone()) {
			statusLabel.setText("Another request running...");
			return;
		}

		statusLabel.setText("Sending...");
		responseArea.setText("");

		AtomicReference<Map<String, Object>> vars = new AtomicReference<>();
		RequestJsExecutor requestJsExecutor = JsExecutorService.requestExecutor();

		requestRunner = CompletableFuture
			.supplyAsync(() -> {
				if (Thread.currentThread().isInterrupted()) {
					// TODO: need custom exception
					throw new RuntimeException("Cancelled");
				}

				Map<String, String> headersMap = new HashMap<>();
				headers.forEach(entry -> headersMap.put(entry.getKey(), entry.getValue()));

				String preparedBody = bodyArea.getText();
				if (beforeRequestCodeArea.getText() != null && !beforeRequestCodeArea.getText().isBlank()) {

					requestJsExecutor.executeBeforeRequest(
						beforeRequestCodeArea.getText(),
						headersMap,
						preparedBody
					);

					vars.set(requestJsExecutor.getVars());

					preparedBody = varsApplicator.applyVariables(
						bodyArea.getText(),
						requestJsExecutor.getVars()
					);
				}

				try {
					return httpService.sendRequest(
						methodCombo.getValue(),
						urlField.getText(),
						preparedBody,
						headersMap
					);
				} catch (Exception e) {
					DependenciesContainer.logger().logMessage("ERROR: " + e.getMessage());
					return null;
				}
			})
			.exceptionally(ex -> {
				Platform.runLater(() -> {
					statusLabel.setText("Error");
					responseArea.setText(ex.getMessage());
				});
				DependenciesContainer.logger().logMessage("ERROR: " + ex.getMessage());
				return null;
			})
			.thenAccept(result -> Platform.runLater(() -> {
				if (result == null) {
					return;
				}

				String formattedBody = httpService.formatJson(result.body());
				statusLabel.setText("Response code: " + result.statusCode());
				responseArea.setText(formattedBody);
				responseHeaders.setText(TextFormatterService.beautyString(result.headers()));

				try {
					requestJsExecutor.executeAfterRequest(
						afterResponseCodeArea.getText(),
						result.body(),
						result.headers(),
						result.statusCode()
					);
				} catch (Exception e) {
					DependenciesContainer.logger().logMessage("ERROR: " + e.getMessage());
				}
			}));
	}

	@Override
	public void cancelRequest() {
		if (requestRunner != null && !requestRunner.isDone()) {
			requestRunner.cancel(true);
			Platform.runLater(() -> {
				statusLabel.setText("Canceled");
				responseArea.setText("Canceled");
			});
			DependenciesContainer.logger().logMessage("Request cancelled\n");
		}
	}

	@FXML
	public void uploadLogs(Event event) {
		Tab tab = (Tab) event.getSource();
		if (tab.isSelected()) {
			Platform.runLater(() -> logsArea.setLogs());
		}
	}

	@FXML
	public void addHeader() {
		String key = headerKeyField.getText();
		String value = headerValueField.getText();
		if (key == null || key.isBlank()) {
			return;
		}

		var entry = new AbstractMap.SimpleEntry<>(key, value);
		headers.add(entry);
		addHeaderRowToUi(entry);

		headerKeyField.clear();
		headerValueField.clear();
		persistenceService.save();
	}


	public void clearLogs() {
		logsArea.clear();
		DependenciesContainer.logger().clearLogs();
	}

	private void addHeaderRowToUi(Map.Entry<String, String> entry) {
		HBox row = new HBox(10);
		Label label = new Label(entry.getKey() + ": " + entry.getValue());

		Button removeBtn = new Button("✖");
		removeBtn.setOnAction(e -> {
			headers.remove(entry);
			headersList.getChildren().remove(row);
			persistenceService.save();
		});

		row.getChildren().addAll(label, removeBtn);
		row.getStyleClass().add("headers-row");
		headersList.getChildren().add(row);
	}

	@Override
	public String fxmlTemplatePath() {
		return "/ui/http_request_editor.fxml";
	}
}