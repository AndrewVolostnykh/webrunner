package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.collections.CollectionNode;
import andrew_volostnykh.webrunner.collections.RequestDefinition;
import andrew_volostnykh.webrunner.collections.persistence.CollectionPersistenceService;
import andrew_volostnykh.webrunner.components.JsonCodeArea;
import andrew_volostnykh.webrunner.components.LogArea;
import andrew_volostnykh.webrunner.components.editor.JsCodeEditor;
import andrew_volostnykh.webrunner.service.http.HttpRequestService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class MainController {

	private final CollectionPersistenceService persistenceService = new CollectionPersistenceService();

	@FXML
	private ComboBox<String> methodCombo;
	@FXML
	private TextField urlField;
	@FXML
	private VBox bodyContainer;
	private JsonCodeArea bodyArea;

	@FXML
	private VBox varsContainer;
	private JsCodeEditor beforeRequestCodeArea;

	@FXML
	private VBox afterResponseContainer;
	private JsCodeEditor afterResponseCodeArea;

	@FXML
	private TextArea logsArea;

	@FXML
	private TextField headerKeyField;
	@FXML
	private TextField headerValueField;
	@FXML
	private VBox headersList;

	private final ObservableList<Map.Entry<String, String>> headers = FXCollections.observableArrayList();

	@FXML
	private TextArea responseArea;
	@FXML
	private Label statusLabel;
	@FXML
	private TreeView<CollectionNode> collectionTree;

	private ChangeListener<String> methodListener, urlListener, bodyListener;
	private ListChangeListener<Map.Entry<String, String>> headersListener;

	private final HttpRequestService httpService = new HttpRequestService();

	@FXML
	public void initialize() {
		beforeRequestCodeArea = new JsCodeEditor();
		beforeRequestCodeArea.replaceText("""
													var vars = {
													generatedName: "rnadom",
													userId: 123
													}
													""");
		beforeRequestCodeArea.attachTo(varsContainer);

		afterResponseCodeArea = new JsCodeEditor();
		afterResponseCodeArea.replaceText("""
													// Приклад:
													//assert(response.json.name, vars.generatedName, "Unexpected response name")
													//""");
		afterResponseCodeArea.attachTo(afterResponseContainer);

		bodyArea = new JsonCodeArea();
		bodyArea.replaceText("""
									{
										"name": "{{generatedName}}",
										"userId": "{{userId}}""
									}
									""");

		bodyContainer.getChildren().add(bodyArea);

		logsArea = new LogArea();

		Button beautifyBtn = new Button("Beautify JSON");
		beautifyBtn.setOnAction(e -> bodyArea.beautifyBody());
		bodyContainer.getChildren().add(beautifyBtn);

		methodCombo.setItems(FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE"));
		methodCombo.getSelectionModel().select("GET");

		// 🟢 Завантаження дерева
		CollectionNode saved = persistenceService.load();
		TreeItem<CollectionNode> rootItem;

		if (saved != null) {
			rootItem = buildTreeItem(saved);
			rootItem.setExpanded(true);
			persistenceService.setRootNode(saved);
		} else {
			CollectionNode rootNode = new CollectionNode("Requests", true, null, null);
			rootItem = new TreeItem<>(rootNode);
			rootItem.setExpanded(true);
			persistenceService.setRootNode(rootNode);
		}

		collectionTree.setRoot(rootItem);
		collectionTree.setShowRoot(true);

		collectionTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.getValue().isFolder()) {
				loadRequest(newVal.getValue().getRequest());
			}
		});

		collectionTree.setCellFactory(treeView -> new TreeCell<>() {
			private final FontIcon folderIcon = new FontIcon("mdi-folder:16");
			private final FontIcon requestIcon = new FontIcon("mdi-file-document:16");

			@Override
			protected void updateItem(
				CollectionNode item,
				boolean empty
			) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					setText(item.getName());
					setGraphic(item.isFolder() ? folderIcon : requestIcon);
				}
			}
		});
	}

	@FXML
	public void sendRequest() {
		statusLabel.setText("Sending...");
		responseArea.setText("");

		AtomicReference<Map<String, Object>> vars = new AtomicReference<>();

		CompletableFuture
			.supplyAsync(() -> {
				Map<String, String> headersMap = new HashMap<>();
				headers.forEach(entry -> headersMap.put(entry.getKey(), entry.getValue()));

				String preparedBody = bodyArea.getText();
				if (beforeRequestCodeArea.getText() != null && !beforeRequestCodeArea.getText().isBlank()) {

					Map<String, Object> bodyVars = DependenciesContainer.getJsExecutorService()
						.executeJsVariables(beforeRequestCodeArea.getText());

					vars.set(bodyVars);

					preparedBody = DependenciesContainer.getVarsApplicator()
						.applyVariables(bodyArea.getText(), bodyVars);
				}

				try {
					return httpService.sendRequest(
						methodCombo.getValue(),
						urlField.getText(),
						preparedBody,
						headersMap
					);
				} catch (Exception e) {
					System.err.println("Exception occurred: " + e.getMessage());
					return null;
				}
			})
			.exceptionally(ex -> {
				Platform.runLater(() -> {
					statusLabel.setText("Error");
					responseArea.setText(ex.getMessage());
				});
				return null;
			})
			.thenAccept(result -> Platform.runLater(() -> {
				if (result == null) {
					return;
				}

				String formattedBody = httpService.formatJson(result.body());
				statusLabel.setText("Done");
				responseArea.setText("Status " + result.statusCode() + "\n\n" + formattedBody);

				try {
					DependenciesContainer.getJsExecutorService()
						.handleAfterResponse(
							afterResponseCodeArea.getText(),
							result.body(),
							vars.get()
						);
				} catch (Exception e) {
					System.err.println("❌ Error in afterResponse JS: " + e.getMessage());
				}
			}));
	}

	private void loadRequest(RequestDefinition req) {
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
		beforeRequestCodeArea.textProperty().addListener((obs, old, val) -> {
			req.setVarsDefinition(val);
			persistenceService.save();
		});
	}

	// =======================================
	// 🆕 Створення нового запиту
	// =======================================
	@FXML
	public void createNewRequestOrFolder(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog("New Request");
		dialog.setTitle("Create Request");
		dialog.setHeaderText("Create new HTTP Request");
		dialog.setContentText("Enter request name:");

		dialog.showAndWait().ifPresent(name -> {
			if (!name.isBlank()) {

				RequestDefinition request = new RequestDefinition(
					name,
					"GET",
					"",
					new HashMap<>(),
					"",
					""
				);

				CollectionNode newRequestNode = new CollectionNode(name, false, null, request);

				persistenceService.getRootNode().addChild(newRequestNode);

				TreeItem<CollectionNode> newNode = new TreeItem<>(newRequestNode);
				collectionTree.getRoot().getChildren().add(newNode);

				persistenceService.save();

				collectionTree.getSelectionModel().select(newNode);
				loadRequest(request);
			}
		});
	}

	// =======================================
	// ➕ Додавання header
	// =======================================
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

	// =======================================
	// 📁 Відновлення дерева
	// =======================================
	private TreeItem<CollectionNode> buildTreeItem(CollectionNode node) {
		TreeItem<CollectionNode> item = new TreeItem<>(node);
		if (node.getChildren() != null) {
			node.getChildren().forEach(child -> item.getChildren().add(buildTreeItem(child)));
		}
		return item;
	}

	public void clearLogs() {
		logsArea.clear();
	}
}
