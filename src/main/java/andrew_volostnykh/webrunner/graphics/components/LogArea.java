package andrew_volostnykh.webrunner.graphics.components;

import andrew_volostnykh.webrunner.DependenciesContainer;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

// TODO: need to be provided everywhere
public class LogArea
	extends TextArea {

	public void setLogs() {
		Platform.runLater(() -> {
			String logs = DependenciesContainer.logger().getLogs();
			this.clear();                // 💥 Затираємо старий текст
			this.appendText(logs);       // 🔥 appendText завжди працює
			this.requestLayout();
			this.requestFocus(); // якщо кастомний компонент не малює одразу
			this.setScrollTop(Double.MAX_VALUE);
		});
	}
}
