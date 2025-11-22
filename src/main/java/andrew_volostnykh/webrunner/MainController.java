package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.collections.CollectionNode;
import andrew_volostnykh.webrunner.collections.RequestDefinition;
import andrew_volostnykh.webrunner.service.HttpRequestService;
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
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

public class MainController {

	@FXML
	private ComboBox<String> methodCombo;
	@FXML
	private TextField urlField;
	@FXML
	private TextArea bodyArea;

	// Headers UI
	@FXML
	private TextField headerKeyField;
	@FXML
	private TextField headerValueField;
	@FXML
	private VBox headersList;

	private final ObservableList<Entry<String, String>> headers = FXCollections.observableArrayList();

	@FXML
	private TextArea responseArea;
	@FXML
	private Label statusLabel;
	@FXML
	private TreeView<CollectionNode> collectionTree;

	private ChangeListener<String> methodListener;
	private ChangeListener<String> urlListener;
	private ChangeListener<String> bodyListener;
	private ListChangeListener<Entry<String, String>> headersListener;

	private final HttpRequestService httpService = new HttpRequestService();

	@FXML
	public void initialize() {
		// HTTP methods
		methodCombo.setItems(FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE"));
		methodCombo.getSelectionModel().select("GET");

		// === Tree init ===
		CollectionNode rootNode = new CollectionNode("Requests", true, null, null);
		TreeItem<CollectionNode> rootItem = new TreeItem<>(rootNode);
		rootItem.setExpanded(true);
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
			protected void updateItem(CollectionNode item, boolean empty) {
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

		CompletableFuture
			.supplyAsync(() -> {
				try {
					Map<String, String> headersMap = new HashMap<>();
					headers.forEach(entry -> {
						if (!entry.getKey().isBlank()) {
							headersMap.put(entry.getKey(), entry.getValue());
						}
					});

					var response = httpService.sendRequest(
						methodCombo.getValue(),
						urlField.getText(),
						bodyArea.getText(),
						headersMap
					);

					String formattedBody = httpService.formatJson(response.body());
					return "Status: " + response.statusCode() + "\n\n" + formattedBody;

				} catch (Exception e) {
					return "Error: " + e.getMessage();
				}
			})
			.thenAccept(result -> Platform.runLater(() -> {
				statusLabel.setText("Done");
				responseArea.setText(result);
			}));
	}

	private void loadRequest(RequestDefinition req) {
		// 🔹 Вимикаємо попередні listener'и, щоб не плодити їх
		if (methodListener != null) methodCombo.valueProperty().removeListener(methodListener);
		if (urlListener != null) urlField.textProperty().removeListener(urlListener);
		if (bodyListener != null) bodyArea.textProperty().removeListener(bodyListener);
		if (headersListener != null) headers.removeListener(headersListener);

		// 🔹 Завантажуємо значення в поля
		methodCombo.setValue(req.getMethod());
		urlField.setText(req.getUrl());
		bodyArea.setText(req.getBody());

		// Хедери: і логіка, і UI
		headers.clear();
		headersList.getChildren().clear();
		req.getHeaders().forEach((k, v) -> {
			var entry = new AbstractMap.SimpleEntry<>(k, v);
			headers.add(entry);
			addHeaderRowToUi(entry);
		});

		// 🔹 Ставимо listener'и назад — тепер вони оновлюють саме цей req
		methodListener = (obs, old, val) -> req.setMethod(val);
		urlListener = (obs, old, val) -> req.setUrl(val);
		bodyListener = (obs, old, val) -> req.setBody(val);
		methodCombo.valueProperty().addListener(methodListener);
		urlField.textProperty().addListener(urlListener);
		bodyArea.textProperty().addListener(bodyListener);

		headersListener = change -> {
			req.getHeaders().clear();
			headers.forEach(entry -> req.getHeaders().put(entry.getKey(), entry.getValue()));
		};
		headers.addListener(headersListener);
	}

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
					"https://",
					new HashMap<>(),
					""
				);

				CollectionNode newRequestNode = new CollectionNode(
					name,
					false,
					null,
					request
				);

				TreeItem<CollectionNode> root = collectionTree.getRoot();
				TreeItem<CollectionNode> newNode = new TreeItem<>(newRequestNode);
				root.getChildren().add(newNode);
				root.setExpanded(true);

				loadRequest(request);
				collectionTree.getSelectionModel().select(newNode);
			}
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
		headers.add(entry); // логічний список хедерів

		addHeaderRowToUi(entry);

		headerKeyField.clear();
		headerValueField.clear();
	}

	private void addHeaderRowToUi(Entry<String, String> entry) {
		HBox row = new HBox(10);
		Label label = new Label(entry.getKey() + ": " + entry.getValue());

		Button removeBtn = new Button("✖");
		removeBtn.setOnAction(e -> {
			headers.remove(entry);          // прибираємо з логіки
			headersList.getChildren().remove(row); // прибираємо з UI
		});

		row.getChildren().addAll(label, removeBtn);
		row.getStyleClass().add("headers-row");
		headersList.getChildren().add(row);
	}
}
