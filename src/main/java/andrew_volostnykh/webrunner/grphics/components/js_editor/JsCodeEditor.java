package andrew_volostnykh.webrunner.grphics.components.js_editor;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.Objects;

public class JsCodeEditor
	extends CodeArea {

	public JsCodeEditor() {
		this.getStylesheets().add(
			Objects.requireNonNull(getClass().getResource("/ui/styles/code-style.css")).toExternalForm()
		);
		this.setStyle("-fx-caret-color: white;");
		this.getStyleClass().add("code-area");

		setupEditor();
	}

	private void setupEditor() {
		setParagraphGraphicFactory(line -> {
			Node lineNumber = LineNumberFactory.get(this).apply(line);
			lineNumber.getStyleClass().add("line-number");
			return lineNumber;
		});

		setOnKeyTyped(this::handleAutoBrackets);
		addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcuts);

		textProperty().addListener((obs, oldText, newText) ->
									   setStyleSpans(0, SyntaxHighlighter.computeHighlighting(newText))
		);
	}

	private void handleAutoBrackets(KeyEvent event) {
		String c = event.getCharacter();

		String match = switch (c) {
			case "{" -> "}";
			case "[" -> "]";
			case "(" -> ")";
			case "\"" -> "\"";
			default -> null;
		};
		if (match != null) {
			int pos = getCaretPosition();
			replaceText(pos, pos, match);
			moveTo(pos); // 🟢 курсор між дужками
		}
	}

	private void handleShortcuts(KeyEvent event) {
		if (event.getCode() == KeyCode.L && event.isControlDown() && event.isAltDown()) {
			replaceText(
				Formatter.beautifyJs(getText())
			);
			event.consume();
		}
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		this.setStyleSpans(0, SyntaxHighlighter.computeHighlighting(getText()));
	}

	// TODO: maybe attach it directly in MainController?
	public void attachTo(VBox container) {
		VBox.setVgrow(this, Priority.ALWAYS);
		container.getChildren().add(this);
	}

}
