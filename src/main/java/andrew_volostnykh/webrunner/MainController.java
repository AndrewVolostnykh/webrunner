package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.collections.CollectionNode;
import andrew_volostnykh.webrunner.collections.RequestDefinition;
import andrew_volostnykh.webrunner.service.HttpRequestService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTableCell;

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
	@FXML
	private TableView<Entry<String, String>> headersTable;
	@FXML
	private TableColumn<Entry<String, String>, String> colKey;
	@FXML
	private TableColumn<Map.Entry<String, String>, String> colValue;
	@FXML
	private TextArea responseArea;
	@FXML
	private Label statusLabel;
	@FXML
	private TreeView<CollectionNode> collectionTree;

	private ChangeListener<String> methodListener, urlListener, bodyListener;

	private final HttpRequestService httpService = new HttpRequestService();
	private final ObservableList<Entry<String, String>> headers = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		methodCombo.setItems(FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE"));
		methodCombo.getSelectionModel().select("GET");

		colKey.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getKey()));
		colKey.setCellFactory(TextFieldTableCell.forTableColumn());
		colKey.setOnEditCommit(e -> {
			headers.set(
				e.getTablePosition().getRow(),
				new java.util.AbstractMap.SimpleEntry<>(e.getNewValue(), e.getRowValue().getValue())
			);
		});

		colValue.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()
																								 .getValue()));
		colValue.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue.setOnEditCommit(e -> {
			headers.set(
				e.getTablePosition().getRow(),
				new java.util.AbstractMap.SimpleEntry<>(e.getRowValue().getKey(), e.getNewValue())
			);
		});

		headersTable.setItems(headers);
		headersTable.setEditable(true);

		// Tree init
		CollectionNode rootNode = new CollectionNode("Requests", true, null, null);
		TreeItem<CollectionNode> rootItem = new TreeItem<>(rootNode);
		rootItem.setExpanded(true);
		collectionTree.setRoot(rootItem);
		collectionTree.setShowRoot(true); // або false, якщо не хочеш показувати

		collectionTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && !newVal.getValue().isFolder()) {
				loadRequest(newVal.getValue().getRequest());
			}
		});
	}

	@FXML
	public void addHeader() {
		headers.add(new java.util.AbstractMap.SimpleEntry<>("", ""));
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

					// Pretty JSON
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
		// 🔹 Вимикаємо попередні listener'и
		if (methodListener != null) methodCombo.valueProperty().removeListener(methodListener);
		if (urlListener != null) urlField.textProperty().removeListener(urlListener);
		if (bodyListener != null) bodyArea.textProperty().removeListener(bodyListener);

		// 🔹 Завантажуємо значення
		methodCombo.setValue(req.getMethod());
		urlField.setText(req.getUrl());
		bodyArea.setText(req.getBody());
		headers.clear();
		req.getHeaders().forEach((k, v) -> headers.add(new java.util.AbstractMap.SimpleEntry<>(k, v)));

		// 🔹 Ставимо listener'и назад
		methodListener = (obs, old, val) -> req.setMethod(val);
		urlListener = (obs, old, val) -> req.setUrl(val);
		bodyListener = (obs, old, val) -> req.setBody(val);

		methodCombo.valueProperty().addListener(methodListener);
		urlField.textProperty().addListener(urlListener);
		bodyArea.textProperty().addListener(bodyListener);

		headers.addListener(
			(javafx.collections.ListChangeListener<Entry<String, String>>) change -> {
				req.getHeaders().clear();
				headers.forEach(entry -> req.getHeaders().put(entry.getKey(), entry.getValue()));
			}
		);
	}

	@FXML
	public void createNewRequestOrFolder(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog("New Request");
		dialog.setTitle("Create Request");
		dialog.setHeaderText("Create new HTTP Request");
		dialog.setContentText("Enter request name:");

		dialog.showAndWait().ifPresent(name -> {
			if (!name.isBlank()) {

				// створюємо новий requestDefinition
				RequestDefinition request = new RequestDefinition(
					name,
					"GET",
					"https://",
					new HashMap<>(),
					""
				);

				// створюємо вузол дерева
				CollectionNode newRequestNode = new CollectionNode(
					name,
					false, // це не папка
					null,
					request
				);

				// ВСІ НОВІ РЕКВЕСТИ ДОДАЄМО В КОРІНЬ
				TreeItem<CollectionNode> root = collectionTree.getRoot();
				TreeItem<CollectionNode> newNode = new TreeItem<>(newRequestNode);
				root.getChildren().add(newNode);
				root.setExpanded(true);

				// Відкриваємо цей реквест
				loadRequest(request);

				// Виділяємо його
				collectionTree.getSelectionModel().select(newNode);
			}
		});
	}

	private void clearRequestUI() {
		methodCombo.setValue("GET");
		urlField.setText("https://");
		bodyArea.setText("");
		headers.clear();
	}
}
