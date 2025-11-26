package andrew_volostnykh.webrunner.graphics.components;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class CreateTreeElementDialog {

	private double xOffset = 0;
	private double yOffset = 0;

	public Optional<String> show(String defaultValue) {
		AtomicReference<String> result = new AtomicReference<>(null);

		Stage dialogStage = new Stage(StageStyle.UNDECORATED);
		dialogStage.initModality(Modality.APPLICATION_MODAL);

		Label lbl = new Label("Enter name:");
		TextField input = new TextField(defaultValue);

		Button ok = new Button("OK");
		Button cancel = new Button("Cancel");

		ok.setOnAction(e -> {
			result.set(input.getText());
			dialogStage.close();
		});
		cancel.setOnAction(e -> dialogStage.close());

		HBox buttons = new HBox(10, ok, cancel);

		VBox content = new VBox(10, lbl, input, buttons);
		content.setPadding(new Insets(10));
		content.getStyleClass().add("custom-dialog");

		content.setOnMousePressed(this::onMousePressed);
		content.setOnMouseDragged(event -> onMouseDragged(event, dialogStage));

		Scene scene = new Scene(content);
		scene.getStylesheets().add(getClass().getResource("/ui/styles/dialog-style.css").toExternalForm());

		dialogStage.setScene(scene);

		input.requestFocus();
		input.selectAll();

		dialogStage.showAndWait();

		return Optional.ofNullable(result.get());
	}

	private void onMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	private void onMouseDragged(
		MouseEvent event,
		Stage stage
	) {
		stage.setX(event.getScreenX() - xOffset);
		stage.setY(event.getScreenY() - yOffset);
	}
}
