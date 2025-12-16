package andrew_volostnykh.webrunner.graphics.components.json_editor;

import andrew_volostnykh.webrunner.service.TextFormatterService;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.Objects;

public class JsonCodeArea
	extends CodeArea {

	public JsonCodeArea() {
		super();
		this.setParagraphGraphicFactory(LineNumberFactory.get(this));
		this.setPrefHeight(300);
		VBox.setVgrow(this, Priority.ALWAYS);

		this.getStylesheets().add(
			Objects.requireNonNull(getClass().getResource("/ui/styles/json-style.css")).toExternalForm()
		);
		this.setStyle("-fx-caret-color: white;");

		setParagraphGraphicFactory(line -> {
			Node lineNumber = LineNumberFactory.get(this).apply(line);
			lineNumber.getStyleClass().add("line-number");
			return lineNumber;
		});

		textProperty().addListener((obs, oldText, newText) ->
									   setStyleSpans(0, SyntaxHighlighter.computeHighlighting(newText))
		);

		this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.L) {
				beautifyBody();
			}
		});

		this.setOnKeyTyped(this::handleAutoBrackets);
	}

	private void handleAutoBrackets(KeyEvent event) {
		String ch = event.getCharacter();
		int pos = getCaretPosition();

		switch (ch) {
			case "{":
				event.consume();
				replaceText(pos - 1, pos, "{}");
				moveTo(pos);
				break;

			case "[":
				event.consume();
				replaceText(pos - 1, pos, "[]");
				moveTo(pos);
				break;

			case "\"":
				if (ch.equals(getText(pos, pos + 1))) {
					event.consume();
					moveTo(pos + 1);
					replaceText(pos, pos + 1, "");
				} else {
					event.consume();
					replaceText(pos - 1, pos, "\"\"");
					moveTo(pos);
				}
				break;

			case "}":
			case "]":
				if (pos < getLength() && getText(pos, pos + 1).equals(ch)) {
					event.consume();
					moveTo(pos + 1);
					replaceText(pos, pos + 1, "");
				}
				break;
		}
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		this.setStyleSpans(0, SyntaxHighlighter.computeHighlighting(getText()));
	}

	public void beautifyBody() {
		try {
			String pretty = TextFormatterService.formatJsonWithPlaceholders(this.getText());
			this.replaceText(pretty);
			this.setStyleSpans(0, SyntaxHighlighter.computeHighlighting(pretty));
		} catch (Exception ignored) {
		}
	}
}
