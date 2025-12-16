package andrew_volostnykh.webrunner.graphics.controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HowToUseUIController {
	@FXML
	private VBox root;

	private double xOffset;
	private double yOffset;

	@FXML
	public void onTitleBarMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	@FXML
	public void onTitleBarMouseDragged(MouseEvent event) {
		Stage stage = getStage(event);
		stage.setX(event.getScreenX() - xOffset);
		stage.setY(event.getScreenY() - yOffset);
	}

	@FXML
	public void minimizeStage(ActionEvent event) {
		getStage(event).setIconified(true);
	}

	@FXML
	public void closeStage(ActionEvent event) {
		getStage(event).close();
	}

	private Stage getStage(Event event) {
		return (Stage) ((Node) event.getSource())
			.getScene()
			.getWindow();
	}
}
