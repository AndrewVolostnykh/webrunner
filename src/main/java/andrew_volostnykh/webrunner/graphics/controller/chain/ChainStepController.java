package andrew_volostnykh.webrunner.graphics.controller.chain;

import andrew_volostnykh.webrunner.service.persistence.definition.AbstractRequestDefinition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ChainStepController {
	@FXML
	private Label requestNameLabel;

	@FXML
	private Label stepIndexLabel;

	@FXML
	private Label statusLabel;

	@FXML
	private Button removeButton;

	private AbstractRequestDefinition request;
	private ChainBuilderUIController parent;

	public void init(AbstractRequestDefinition request, ChainBuilderUIController parent) {
		this.request = request;
		this.parent = parent;

		requestNameLabel.setText(request.getName());
		statusLabel.setText("pending");

		removeButton.setOnAction(e -> parent.removeStep(request));
	}

	public void removeStep() {
		System.err.println("step removing called");
	}
}
