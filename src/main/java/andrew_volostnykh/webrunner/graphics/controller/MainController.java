package andrew_volostnykh.webrunner.graphics.controller;

import andrew_volostnykh.webrunner.DependenciesContainer;
import andrew_volostnykh.webrunner.graphics.RequestEditorUI;
import andrew_volostnykh.webrunner.graphics.RequestUIFactory;
import andrew_volostnykh.webrunner.service.persistence.CollectionNode;
import andrew_volostnykh.webrunner.service.persistence.NavigationTreeService;
import andrew_volostnykh.webrunner.service.persistence.definition.AbstractRequestDefinition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {

	private double xOffset = 0;
	private double yOffset = 0;

	@FXML
	private TreeView<CollectionNode> collectionTree;
	@FXML
	private VBox mainEditorContainer;
	private RequestEditorUI activeEditor;

	@FXML
	public void initialize() {
		TreeItem<CollectionNode> rootItem = NavigationTreeService.initRootItem();

		collectionTree.setRoot(rootItem);
		collectionTree.setShowRoot(true);

		NavigationTreeService.addContextMenus(
			collectionTree,
			this::loadRequest
		);
		NavigationTreeService.addListenerOnCreate(
			collectionTree,
			this::loadRequest
		);
	}

	@FXML
	public void sendRequest() {
		if (activeEditor != null) {
			activeEditor.sendRequest();
		}
	}

	private void loadRequest(AbstractRequestDefinition req) {
		mainEditorContainer.getChildren().clear();

		activeEditor = RequestUIFactory.create(req);

		try {
			FXMLLoader loader = new FXMLLoader(
				getClass().getResource(
					activeEditor.fxmlTemplatePath()
				)
			);
			loader.setController(activeEditor);
			mainEditorContainer.getChildren()
				.add(
					loader.load()
				);
		} catch (Exception e) {
			// TODO: custom exception
			throw new RuntimeException("Failed to load UI", e);
		}

		DependenciesContainer.loggersContext().setCurrentRequest(req.getId());
		activeEditor.loadRequest(req);
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
		return (Stage) mainEditorContainer.getScene().getWindow();
	}

	@FXML
	public void cancelRequest() {
		if (activeEditor != null) {
			activeEditor.cancelRequest();
		}
	}
}
