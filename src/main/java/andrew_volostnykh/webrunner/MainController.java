package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.collections.CollectionNode;
import andrew_volostnykh.webrunner.collections.RequestDefinition;
import andrew_volostnykh.webrunner.collections.persistence.CollectionPersistenceService;
import andrew_volostnykh.webrunner.grphics.components.json_editor.JsonCodeArea;
import andrew_volostnykh.webrunner.grphics.components.LogArea;
import andrew_volostnykh.webrunner.grphics.components.js_editor.JsCodeEditor;
import andrew_volostnykh.webrunner.service.TextBeautifierService;
import andrew_volostnykh.webrunner.service.http.HttpRequestService;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicator;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class MainController {

	private final FontIcon folderIcon = new FontIcon("mdi-folder:16");
	private final FontIcon requestIcon  = new FontIcon("mdi-web");

	private double xOffset = 0;
	private double yOffset = 0;

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
	private LogArea logsArea;

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
	private TextArea responseHeaders;
	@FXML
	private Label statusLabel;
	@FXML
	private TreeView<CollectionNode> collectionTree;

	private ChangeListener<String> methodListener, urlListener, bodyListener;
	private ListChangeListener<Map.Entry<String, String>> headersListener;

	// FIXME: move to DependenciesContainer
	private final CollectionPersistenceService persistenceService = new CollectionPersistenceService();
	// FIXME: move to DependenciesContainer
	private final HttpRequestService httpService = new HttpRequestService();
	private final JsExecutorService jsExecutorService = DependenciesContainer.jsExecutorService();
	private final VarsApplicator varsApplicator = DependenciesContainer.varsApplicator();

	@FXML
	public void initialize() {
		beforeRequestCodeArea = new JsCodeEditor();
		beforeRequestCodeArea.attachTo(varsContainer);

		afterResponseCodeArea = new JsCodeEditor();
		afterResponseCodeArea.attachTo(afterResponseContainer);

		bodyArea = new JsonCodeArea();

		bodyContainer.getChildren().add(bodyArea);

		Button beautifyBtn = new Button("Beautify JSON");
		beautifyBtn.setOnAction(e -> bodyArea.beautifyBody());
		bodyContainer.getChildren().add(beautifyBtn);

		methodCombo.setItems(FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE"));
		methodCombo.getSelectionModel().select("GET");

		// TODO: incapsulate tree functionality
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
			private final FontIcon folderIcon  = new FontIcon("mdi-folder:16");
			private final FontIcon requestIcon = new FontIcon("mdi-web:16");

			private final ContextMenu folderMenu = new ContextMenu();
			private final MenuItem addRequestItem = new MenuItem("Add Request");
			private final MenuItem addFolderItem  = new MenuItem("Add Folder");
			private final MenuItem deleteFolderItem = new MenuItem("Delete Folder");

			private final ContextMenu requestMenu = new ContextMenu();
			private final MenuItem deleteRequestItem = new MenuItem("Delete Request");

			{
				// 📎 Папка
				addRequestItem.setOnAction(e -> createRequestInsideNode(getItem(), getTreeItem()));
				addFolderItem.setOnAction(e -> createFolderInsideNode(getItem(), getTreeItem()));
				deleteFolderItem.setOnAction(e -> deleteNode(getItem(), getTreeItem()));
				folderMenu.getItems().addAll(addRequestItem, addFolderItem, deleteFolderItem);

				// 🧾 Запит
				deleteRequestItem.setOnAction(e -> deleteNode(getItem(), getTreeItem()));
				requestMenu.getItems().add(deleteRequestItem);
			}

			@Override
			protected void updateItem(CollectionNode item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || item == null) {
					setText(null);
					setGraphic(null);
					setContextMenu(null);
				} else {
					setText(item.getName());
					setGraphic(item.isFolder() ? folderIcon : requestIcon);

					setContextMenu(item.isFolder() ? folderMenu : requestMenu);
				}
			}
		});
	}

	private void deleteNode(CollectionNode node, TreeItem<CollectionNode> treeItem) {
		TreeItem<CollectionNode> parent = treeItem.getParent();
		if (parent != null) {
			parent.getChildren().remove(treeItem);
			parent.getValue().getChildren().remove(node);
			persistenceService.save();
		}
	}

	private void createFolderInsideNode(CollectionNode folderNode, TreeItem<CollectionNode> folderItem) {
		TextInputDialog dialog = new TextInputDialog("New Folder");
		dialog.setTitle("Create Folder");
		dialog.setHeaderText("Enter folder name:");
		dialog.showAndWait().ifPresent(name -> {
			if (!name.isBlank()) {
				CollectionNode newFolder = new CollectionNode(name, true, new ArrayList<>(), null);
				folderNode.addChild(newFolder);

				TreeItem<CollectionNode> newItem = new TreeItem<>(newFolder);
				folderItem.getChildren().add(newItem);

				persistenceService.save();
			}
		});
	}

	private void createRequestInsideNode(CollectionNode folderNode, TreeItem<CollectionNode> folderItem) {
		TextInputDialog dialog = new TextInputDialog("New Request");
		dialog.setTitle("Create Request");
		dialog.setHeaderText("Create new HTTP Request");
		dialog.setContentText("Enter request name:");

		dialog.showAndWait().ifPresent(name -> {
			if (!name.isBlank()) {
				RequestDefinition request = new RequestDefinition(name, "GET", "", new HashMap<>(), "", "");

				CollectionNode newNode = new CollectionNode(
					name,
					false,
					null,
					request
				);

				folderNode.addChild(newNode);
				TreeItem<CollectionNode> newItem = new TreeItem<>(newNode);
				folderItem.getChildren().add(newItem);

				persistenceService.save();
				collectionTree.getSelectionModel().select(newItem);
				loadRequest(request);
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

					Map<String, Object> bodyVars = jsExecutorService
						.executeJsVariables(beforeRequestCodeArea.getText());

					vars.set(bodyVars);

					preparedBody = varsApplicator.applyVariables(bodyArea.getText(), bodyVars);
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
				responseHeaders.setText(TextBeautifierService.beautyString(result.headers()));

				try {
					jsExecutorService
						.handleAfterResponse(
							afterResponseCodeArea.getText(),
							result.body(),
							vars.get()
						);
				} catch (Exception e) {
					DependenciesContainer.logger().logMessage("ERROR: " + e.getMessage());
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

	private TreeItem<CollectionNode> buildTreeItem(CollectionNode node) {
		TreeItem<CollectionNode> item = new TreeItem<>(node);
		if (node.getChildren() != null) {
			node.getChildren().forEach(child -> item.getChildren().add(buildTreeItem(child)));
		}
		return item;
	}

	public void clearLogs() {
		logsArea.clear();
		DependenciesContainer.logger().clearLogs();
	}

	public void onTitleBarMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	public void onTitleBarMouseDragged(MouseEvent event) {
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		stage.setX(event.getScreenX() - xOffset);
		stage.setY(event.getScreenY() - yOffset);
	}

	public void minimizeStage() {
		getStage().setIconified(true);
	}

	public void maximizeRestoreStage() {
		Stage stage = getStage();
		stage.setMaximized(!stage.isMaximized());
	}

	public void closeStage() {
		getStage().close();
	}

	private Stage getStage() {
		return (Stage) methodCombo.getScene().getWindow();
	}

	@FXML
	public void uploadLogs(Event event) {
		Tab tab = (Tab) event.getSource();
		if (tab.isSelected()) {
			Platform.runLater(() -> logsArea.setLogs());
		}
	}
}
