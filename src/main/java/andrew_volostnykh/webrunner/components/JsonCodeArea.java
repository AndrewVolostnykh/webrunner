package andrew_volostnykh.webrunner.components;

import andrew_volostnykh.webrunner.service.JsonBeautifier;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class JsonCodeArea extends CodeArea {

	public JsonCodeArea() {
		super();
		this.setParagraphGraphicFactory(LineNumberFactory.get(this));
		this.setPrefHeight(300);
		VBox.setVgrow(this, Priority.ALWAYS);

		// 🔹 Beautify по Ctrl+Alt+L
		this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.L) {
				beautifyBody();
			}
		});

		// 🔹 Автодужки
		this.setOnKeyTyped(event -> {
			String ch = event.getCharacter();
			int pos = this.getCaretPosition();

			switch (ch) {
				case "{":
					this.insertText(pos, "}");
					this.moveTo(pos);
					break;
				case "[":
					this.insertText(pos, "]");
					this.moveTo(pos);
					break;
				case "\"":
					this.insertText(pos, "\"");
					this.moveTo(pos);
					break;
			}
		});
	}

	public void beautifyBody() {
		try {
			String pretty = JsonBeautifier.formatJson(this.getText());
			this.replaceText(pretty);
		} catch (Exception e) {
		}
	}
}
