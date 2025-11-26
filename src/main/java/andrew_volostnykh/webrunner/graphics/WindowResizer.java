package andrew_volostnykh.webrunner.graphics;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowResizer {
	private static final int BORDER = 5;

	public static void apply(Node root, Stage stage) {
		root.setOnMouseMoved(event -> {
			double x = event.getSceneX();
			double y = event.getSceneY();
			double w = stage.getWidth();
			double h = stage.getHeight();

			Cursor cursor = Cursor.DEFAULT;

			boolean left = x < BORDER;
			boolean right = x > w - BORDER;
			boolean top = y < BORDER;
			boolean bottom = y > h - BORDER;

			if (left && top) cursor = Cursor.NW_RESIZE;
			else if (left && bottom) cursor = Cursor.SW_RESIZE;
			else if (right && top) cursor = Cursor.NE_RESIZE;
			else if (right && bottom) cursor = Cursor.SE_RESIZE;
			else if (left) cursor = Cursor.W_RESIZE;
			else if (right) cursor = Cursor.E_RESIZE;
			else if (top) cursor = Cursor.N_RESIZE;
			else if (bottom) cursor = Cursor.S_RESIZE;

			root.setCursor(cursor);
		});

		root.setOnMouseDragged(event -> {
			Cursor c = root.getCursor();
			if (c == Cursor.DEFAULT) return;

			double x = event.getScreenX();
			double y = event.getScreenY();

			if (c == Cursor.W_RESIZE || c == Cursor.NW_RESIZE || c == Cursor.SW_RESIZE) {
				double newWidth = stage.getX() + stage.getWidth() - x;
				if (newWidth > 300) {
					stage.setWidth(newWidth);
					stage.setX(x);
				}
			}
			if (c == Cursor.E_RESIZE || c == Cursor.NE_RESIZE || c == Cursor.SE_RESIZE) {
				double newWidth = x - stage.getX();
				if (newWidth > 300) stage.setWidth(newWidth);
			}
			if (c == Cursor.N_RESIZE || c == Cursor.NW_RESIZE || c == Cursor.NE_RESIZE) {
				double newHeight = stage.getY() + stage.getHeight() - y;
				if (newHeight > 200) {
					stage.setHeight(newHeight);
					stage.setY(y);
				}
			}
			if (c == Cursor.S_RESIZE || c == Cursor.SW_RESIZE || c == Cursor.SE_RESIZE) {
				double newHeight = y - stage.getY();
				if (newHeight > 200) stage.setHeight(newHeight);
			}
		});
	}
}
