package andrew_volostnykh.webrunner.graphics.components.js_editor;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SyntaxHighlighter {

	private static final Pattern PATTERN = Pattern.compile(
		"(?<KEYWORD>\\b(var|let|const|function|return|if|else)\\b)"
			+ "|(?<STRING>\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*')"
			+ "|(?<NUMBER>\\b\\d+(\\.\\d+)?\\b)"
			+ "|(?<VARIABLE>\\{\\{[^}]+}})"
			+ "|(?<COMMENT>//[^\n]*|/\\*(.|\\R)*?\\*/)"
			+ "|(?<BRACE>[(){}\\[\\]])"
	);

	static StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text);
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		int last = 0;

		while (matcher.find()) {
			String style = matcher.group("KEYWORD")   != null ? "keyword" :
				matcher.group("STRING")    != null ? "string"  :
					matcher.group("NUMBER")    != null ? "number"  :
						matcher.group("VARIABLE")  != null ? "variable":
							matcher.group("COMMENT")   != null ? "comment" :
								matcher.group("BRACE")     != null ? "brace"   :
									"plain-text";

			// 🔥 Тепер все між match – теж біле
			spansBuilder.add(Collections.singleton("plain-text"), matcher.start() - last);
			spansBuilder.add(Collections.singleton(style), matcher.end() - matcher.start());
			last = matcher.end();
		}

		// 🔥 Завершуємо кінець тексту звичайним стилем
		spansBuilder.add(Collections.singleton("plain-text"), text.length() - last);
		return spansBuilder.create();
	}
}
