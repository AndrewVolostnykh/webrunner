package andrew_volostnykh.webrunner.service.hotkeys;

import javafx.scene.layout.VBox;

public class HotkeysListenersService {

	public static void registerSendRequestHotKeyListener(
		VBox mainEditorContainer,
		Runnable sendRequest
	) {
		mainEditorContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
			if (newScene != null) {
				newScene.addEventFilter(
					javafx.scene.input.KeyEvent.KEY_PRESSED,
					event -> {
						if (event.isControlDown()
							&& event.getCode() == javafx.scene.input.KeyCode.ENTER) {
							sendRequest.run();
							event.consume();
						}
					}
				);
			}
		});
	}
}
