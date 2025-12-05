package andrew_volostnykh.webrunner.graphics.controller.chain;

import andrew_volostnykh.webrunner.graphics.RequestEditorUI;
import andrew_volostnykh.webrunner.service.persistence.definition.AbstractRequestDefinition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChainBuilderUIController implements RequestEditorUI {

	@FXML
	private VBox chainStepsContainer;

	@FXML
	private Button runChainButton;

	@FXML
	private Button clearChainButton;

	private final List<AbstractRequestDefinition> chain = new ArrayList<>();

	private CompletableFuture<?> chainRunner;

	@Override
	public void loadRequest(AbstractRequestDefinition request) {
		runChainButton.setOnAction(e -> sendRequest());
		clearChainButton.setOnAction(e -> clearChain());
	}

	public void addRequest(AbstractRequestDefinition request) {
		chain.add(request);
		addStepToUI(request);
	}

	private void addStepToUI(AbstractRequestDefinition request) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/chain_step.fxml"));
			HBox root = loader.load();

			ChainStepController controller = loader.getController();
			controller.init(request, this);

			chainStepsContainer.getChildren().add(root);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load chain step UI", ex);
		}

		updateStepIndexes();
	}

	private void updateStepIndexes() {
		for (int i = 0; i < chainStepsContainer.getChildren().size(); i++) {
			HBox box = (HBox) chainStepsContainer.getChildren().get(i);
			Label idxLabel = (Label) box.lookup("#stepIndexLabel");
			idxLabel.setText("#" + (i + 1));
		}
	}

	public void removeStep(AbstractRequestDefinition request) {
		int index = chain.indexOf(request);
		if (index >= 0) {
			chain.remove(index);
			chainStepsContainer.getChildren().remove(index);
			updateStepIndexes();
		}
	}

	public void clearChain() {
		chain.clear();
		chainStepsContainer.getChildren().clear();
	}

	private void updateStatus(int stepIndex, String status) {
		javafx.application.Platform.runLater(() -> {
			HBox root = (HBox) chainStepsContainer.getChildren().get(stepIndex);
			Label statusLabel = (Label) root.lookup("#statusLabel");
			statusLabel.setText(status);
		});
	}

	@Override
	public void sendRequest() {
		System.err.println("CHAIN REQUEST SENT");
	}

	@Override
	public void cancelRequest() {

	}

	@Override
	public String fxmlTemplatePath() {
		return "/ui/chain_builder.fxml";
	}
}
