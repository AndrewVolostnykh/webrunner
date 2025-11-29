package andrew_volostnykh.webrunner.graphics.controller;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.graphics.RequestEditorUI;
import andrew_volostnykh.webrunner.graphics.components.LogArea;
import andrew_volostnykh.webrunner.graphics.components.js_editor.JsCodeEditor;
import andrew_volostnykh.webrunner.graphics.components.json_editor.JsonCodeArea;
import andrew_volostnykh.webrunner.service.TextFormatterService;
import andrew_volostnykh.webrunner.service.grpc.GrpcMethodDefinition;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.persistence.NavigationTreePersistenceService;
import andrew_volostnykh.webrunner.service.persistence.RequestDefinition;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicator;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class GrpcRequestUIController implements RequestEditorUI {

	@FXML
	private TextField hostField;
	@FXML
	private ComboBox<String> serviceCombo;
	@FXML
	private ComboBox<String> methodCombo;
	@FXML
	private Button loadServicesButton;

	@FXML
	private Button sendButton;
	@FXML
	private Button cancelButton;

	@FXML
	private VBox bodyContainer;
	private JsonCodeArea bodyArea;

	@FXML
	private VBox beforeRequestContainer;
	private JsCodeEditor beforeRequestCodeArea;

	@FXML
	private VBox afterResponseContainer;
	private JsCodeEditor afterResponseCodeArea;

	@FXML
	private TextArea responseArea;
	@FXML
	private Label statusLabel;
	@FXML
	private LogArea logsArea;

	private Map<String, Map<String, GrpcMethodDefinition>> servicesMethodsDefinitions;
	private CompletableFuture<?> requestRunner;

	private ChangeListener<String> methodListener,
		serviceListener,
		hostListener,
		bodyListener,
		beforeRequestAreaListener,
		afterResponseAreaListener;

	private final NavigationTreePersistenceService persistenceService =
		DependenciesContainer.collectionPersistenceService();
	private final JsExecutorService jsExecutorService =
		DependenciesContainer.jsExecutorService();
	private final VarsApplicator varsApplicator =
		DependenciesContainer.varsApplicator();

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

		Button beautifyBtn = new Button("Beautify JSON");
		beautifyBtn.setOnAction(e -> bodyArea.beautifyBody());
		bodyContainer.getChildren().add(beautifyBtn);
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
				servicesMethodsDefinitions = map;
				serviceCombo.setItems(FXCollections.observableArrayList(servicesMethodsDefinitions.keySet()));
				serviceCombo.setDisable(false);
				statusLabel.setText("✔ Services loaded");

				serviceCombo.setCellFactory(listView -> new ListCell<>() {
					@Override
					protected void updateItem(
						String item,
						boolean empty
					) {
						super.updateItem(item, empty);
						setText(empty || item == null ? null : item);
					}
				});
				serviceCombo.setButtonCell(new ListCell<>() {
					@Override
					protected void updateItem(
						String item,
						boolean empty
					) {
						super.updateItem(item, empty);
						setText(
							empty || item == null ? null : TextFormatterService.shortName(item));
					}
				});

				serviceCombo.getSelectionModel().selectedItemProperty().addListener((obs2, old, selectedService) -> {

					methodCombo.setItems(
						FXCollections.observableArrayList(
							map.get(selectedService).keySet()
						)
					);
					methodCombo.setDisable(false);

					methodCombo.setCellFactory(listView -> new ListCell<>() {
						@Override
						protected void updateItem(
							String item,
							boolean empty
						) {
							super.updateItem(item, empty);
							setText(empty || item == null ? null : item);
						}
					});
					methodCombo.setButtonCell(new ListCell<>() {
						@Override
						protected void updateItem(
							String item,
							boolean empty
						) {
							super.updateItem(item, empty);
							setText(empty || item == null ? null :
										TextFormatterService.shortName(item));
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
				return null;
			});
	}

	@FXML
	public void uploadLogs(Event e) {
		if (((Tab) e.getSource()).isSelected()) {
			Platform.runLater(() -> logsArea.setLogs());
		}
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
		if (bodyListener != null) {
			bodyArea.textProperty().removeListener(bodyListener);
		}

		bodyArea.replaceText(request.getBody());
		bodyListener = (obs, old, val) -> {
			request.setBody(val);
			persistenceService.save();
		};
		bodyArea.textProperty().addListener(bodyListener);

		if (hostListener != null) {
			hostField.textProperty().removeListener(hostListener);
		}
		hostField.setText(request.getUrl());
		hostListener = (obs, old, val) -> {
			request.setUrl(val);
			persistenceService.save();
		};
		hostField.textProperty().addListener(hostListener);

		if (beforeRequestAreaListener != null) {
			beforeRequestCodeArea.textProperty()
				.removeListener(beforeRequestAreaListener);
		}
		beforeRequestCodeArea.replaceText(request.getVarsDefinition());
		beforeRequestAreaListener = (obs, old, val) -> {
			request.setVarsDefinition(val);
			persistenceService.save();
		};
		beforeRequestCodeArea.textProperty().addListener(beforeRequestAreaListener);


		if (afterResponseAreaListener != null) {
			afterResponseCodeArea.textProperty()
				.removeListener(afterResponseAreaListener);
		}
		afterResponseCodeArea.replaceText(request.getOnResponse());
		afterResponseAreaListener = (obs, old, val) -> {
			request.setOnResponse(val);
			persistenceService.save();
		};
		afterResponseCodeArea.textProperty().addListener(afterResponseAreaListener);

		if (methodListener != null) {
			methodCombo.valueProperty().removeListener(methodListener);
		}
		methodCombo.setValue(request.getSelectedMethod());
		methodListener = (obs, old, val) -> {
			request.setSelectedMethod(val);
			persistenceService.save();
		};
		methodCombo.valueProperty().addListener(methodListener);

		if (serviceListener != null) {
			serviceCombo.valueProperty().removeListener(serviceListener);
		}
		serviceCombo.setValue(request.getSelectedService());
		serviceListener = (obs, old, val) -> {
			request.setSelectedService(val);
			persistenceService.save();
		};
		serviceCombo.valueProperty().addListener(serviceListener);
	}

	@Override
	public void sendRequest() {
		try {
			if (requestRunner != null && !requestRunner.isDone()) {
				statusLabel.setText("Another gRPC request running...");
				return;
			}

			AtomicReference<Map<String, Object>> vars = new AtomicReference<>();

			statusLabel.setText("Sending gRPC...");
			responseArea.setText("");

			requestRunner = CompletableFuture.runAsync(() -> {
				try {
					String host = hostField.getText();
					String serviceName = serviceCombo.getValue();
					String methodName = methodCombo.getValue();

					ManagedChannel channel = ManagedChannelBuilder
						.forTarget(host)
						.usePlaintext()
						.build();

					GrpcMethodDefinition grpcMethodDefinition =
						servicesMethodsDefinitions.get(serviceName).get(methodName);

					Descriptors.Descriptor inputDesc = grpcMethodDefinition.getInputType();
					Descriptors.Descriptor outputDesc = grpcMethodDefinition.getOutputType();

					String preparedBody = bodyArea.getText();
					if (beforeRequestCodeArea.getText() != null && !beforeRequestCodeArea.getText().isBlank()) {

						Map<String, Object> bodyVars = jsExecutorService
							.executeJsVariables(beforeRequestCodeArea.getText());

						vars.set(bodyVars);

						preparedBody = varsApplicator.applyVariables(bodyArea.getText(), bodyVars);
					}

					DynamicMessage requestMsg = jsonToDynamicMessage(preparedBody, inputDesc);

					io.grpc.MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethod =
						io.grpc.MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
							.setFullMethodName(
								io.grpc.MethodDescriptor.generateFullMethodName(serviceName, methodName)
							)
							.setType(io.grpc.MethodDescriptor.MethodType.UNARY)
							.setRequestMarshaller(ProtoUtils.marshaller(
								DynamicMessage.getDefaultInstance(inputDesc)))
							.setResponseMarshaller(ProtoUtils.marshaller(
								DynamicMessage.getDefaultInstance(outputDesc)))
							.build();

					DynamicMessage response = ClientCalls.blockingUnaryCall(
						channel, grpcMethod, CallOptions.DEFAULT, requestMsg
					);

					String jsonResponse = JsonFormat.printer().print(response);

					try {
						jsExecutorService
							.executeJsAfterRequest(
								afterResponseCodeArea.getText(),
								vars.get(),
								jsonResponse
//								result.headers()
							);
					} catch (Exception e) {
						DependenciesContainer.logger().logMessage("ERROR: " + e.getMessage());
					}

					Platform.runLater(() -> {
						responseArea.setText(jsonResponse);
						statusLabel.setText("✔ gRPC OK");
					});

					channel.shutdown();
				} catch (Exception e) {
					Platform.runLater(() -> {
						responseArea.setText("ERROR: " + e.getMessage());
						statusLabel.setText("❌ Failed");
					});
					DependenciesContainer.logger().logMessage("ERROR: " + e.getMessage());
				}
			});

		} catch (Exception ex) {
			statusLabel.setText("❌ " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private DynamicMessage jsonToDynamicMessage(String json, Descriptors.Descriptor descriptor) throws Exception {
		DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
		JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
		return builder.build();
	}

	@Override
	public void cancelRequest() {

	}

	@Override
	public String fxmlTemplatePath() {
		return "/ui/grpc_request_editor.fxml";
	}

	@Override
	public void saveChanges() {

	}
}
