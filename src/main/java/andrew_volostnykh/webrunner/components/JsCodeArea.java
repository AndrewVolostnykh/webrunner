package andrew_volostnykh.webrunner.components;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class JsCodeArea
	extends CodeArea {

	public JsCodeArea() {
		this.setParagraphGraphicFactory(LineNumberFactory.get(this));
		this.setPrefHeight(200);
		VBox.setVgrow(this, Priority.ALWAYS);

// Beautify vars по Ctrl+Alt+L
//		this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
//			if (e.isControlDown() && e.isAltDown() && e.getCode() == KeyCode.L) {
//				try {
//					String formatted = httpService.formatJson(varsArea.getText());
//					this.replaceText(formatted);
//				} catch (Exception ignored) {}
//			}
//		});

// Авто закриття дужок
		this.setOnKeyTyped(event -> {
			switch (event.getCharacter()) {
				case "{":
					this.insertText(this.getCaretPosition(), "}");
					break;
				case "(":
					this.insertText(this.getCaretPosition(), ")");
					break;
				case "[":
					this.insertText(this.getCaretPosition(), "]");
					break;
				case "\"":
					this.insertText(this.getCaretPosition(), "\"");
					break;
			}
		});
	}
}
