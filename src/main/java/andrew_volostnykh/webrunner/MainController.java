package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.service.HttpRequestService;
import andrew_volostnykh.webrunner.service.HttpResponseData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

public class MainController {
	@FXML private ComboBox<String> methodCombo;
	@FXML private TextField urlField;
	@FXML private TextArea bodyArea;
	@FXML private TableView<Entry<String, String>> headersTable;
	@FXML private TableColumn<Entry<String, String>, String> colKey;
	@FXML private TableColumn<Map.Entry<String, String>, String> colValue;
	@FXML private TextArea responseArea;
	@FXML private Label statusLabel;

	private final HttpRequestService httpService = new HttpRequestService();
	private final ObservableList<Entry<String, String>> headers = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		methodCombo.setItems(FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE"));
		methodCombo.getSelectionModel().select("GET");

		colKey.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getKey()));
		colKey.setCellFactory(TextFieldTableCell.forTableColumn());
		colKey.setOnEditCommit(e -> {
			headers.set(e.getTablePosition().getRow(),
						new java.util.AbstractMap.SimpleEntry<>(e.getNewValue(), e.getRowValue().getValue()));
		});

		colValue.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue().getValue()));
		colValue.setCellFactory(TextFieldTableCell.forTableColumn());
		colValue.setOnEditCommit(e -> {
			headers.set(e.getTablePosition().getRow(),
						new java.util.AbstractMap.SimpleEntry<>(e.getRowValue().getKey(), e.getNewValue()));
		});

		headersTable.setItems(headers);
		headersTable.setEditable(true);
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
}
