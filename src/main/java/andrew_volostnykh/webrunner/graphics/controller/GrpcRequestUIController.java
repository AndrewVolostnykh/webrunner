package andrew_volostnykh.webrunner.graphics.controller;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.graphics.RequestEditorUI;
import andrew_volostnykh.webrunner.graphics.components.LogArea;
import andrew_volostnykh.webrunner.graphics.components.js_editor.JsCodeEditor;
import andrew_volostnykh.webrunner.graphics.components.json_editor.JsonCodeArea;
import andrew_volostnykh.webrunner.service.TextFormatterService;
import andrew_volostnykh.webrunner.service.grpc.GrpcMethodDefinition;
import andrew_volostnykh.webrunner.service.js.JsExecutorService;
import andrew_volostnykh.webrunner.service.js.RequestJsExecutor;
import andrew_volostnykh.webrunner.service.persistence.NavigationTreePersistenceService;
import andrew_volostnykh.webrunner.service.persistence.definition.AbstractRequestDefinition;
import andrew_volostnykh.webrunner.service.persistence.definition.GrpcRequestDefinition;
import andrew_volostnykh.webrunner.service.test_engine.VarsApplicatorService;
import andrew_volostnykh.webrunner.utils.Maps;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.MetadataUtils;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
	private VBox headersList;
	@FXML
	private TextField headerKeyField;
	@FXML
	private TextField headerValueField;
	private final ObservableList<Entry<String, String>> headers = FXCollections.observableArrayList();
	private ListChangeListener<Entry<String, String>> headersListener;

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
	private final VarsApplicatorService varsApplicator =
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

	public void clearLogs() {
		logsArea.clear();
	}

	@Override
	public Node getRoot() {
		return null;
	}

	@Override
	public void loadRequest(AbstractRequestDefinition abstractRequest) {
		GrpcRequestDefinition request = (GrpcRequestDefinition) abstractRequest;

		if (bodyListener != null) {
			bodyArea.textProperty().removeListener(bodyListener);
		}

		bodyArea.replaceText(request.getBody());
		bodyListener = (obs, old, val) -> {
			request.setBody(val);
			persistenceService.save();
		};
		bodyArea.textProperty().addListener(bodyListener);

		if (headersListener != null) {
			headers.removeListener(headersListener);
		}
		headersListener = change -> {
			request.getHeaders().clear();
			headers.forEach(
				entry -> Maps.singularPut(request.getHeaders(), entry.getKey(), entry.getValue())
			);
			persistenceService.save();
		};
		headers.addListener(headersListener);

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
		beforeRequestCodeArea.replaceText(request.getBeforeRequest());
		beforeRequestAreaListener = (obs, old, val) -> {
			request.setBeforeRequest(val);
			persistenceService.save();
		};
		beforeRequestCodeArea.textProperty().addListener(beforeRequestAreaListener);


		if (afterResponseAreaListener != null) {
			afterResponseCodeArea.textProperty()
				.removeListener(afterResponseAreaListener);
		}
		afterResponseCodeArea.replaceText(request.getAfterRequest());
		afterResponseAreaListener = (obs, old, val) -> {
			request.setAfterRequest(val);
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
			RequestJsExecutor requestJsExecutor = JsExecutorService.requestExecutor();

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

					Metadata metadata = new Metadata();
					headers.forEach(entry -> {
						Metadata.Key<String> key = Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER);
						metadata.put(key, entry.getValue());
					});

					Channel channelWithHeaders = ClientInterceptors.intercept(
						channel, MetadataUtils.newAttachHeadersInterceptor(metadata)
					);

					DynamicMessage response = ClientCalls.blockingUnaryCall(
						channelWithHeaders, grpcMethod, CallOptions.DEFAULT, requestMsg
					);

					String jsonResponse = JsonFormat.printer().print(response);

					// FIXME: thenApply in completable future
					try {
						requestJsExecutor.executeAfterRequest(
							afterResponseCodeArea.getText(),
							jsonResponse,
							new HashMap<>(),
							0
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
		if (requestRunner != null && !requestRunner.isDone()) {
			requestRunner.cancel(true);
			Platform.runLater(() -> {
				statusLabel.setText("Canceled");
				responseArea.setText("Canceled");
			});
			DependenciesContainer.logger().logMessage("Request cancelled\n");
		}
	}

	@Override
	public String fxmlTemplatePath() {
		return "/ui/grpc_request_editor.fxml";
	}

	@Override
	public void saveChanges() {

	}
}
