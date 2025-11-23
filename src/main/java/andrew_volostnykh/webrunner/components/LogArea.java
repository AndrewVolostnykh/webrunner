package andrew_volostnykh.webrunner.components;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class LogArea
	extends TextArea {

	public void logMessage(String message) {
		Platform.runLater(() -> {
			this.appendText(message + "\n");

			// автоскрол до кінця
			this.setScrollTop(Double.MAX_VALUE);
		});
	}
}
