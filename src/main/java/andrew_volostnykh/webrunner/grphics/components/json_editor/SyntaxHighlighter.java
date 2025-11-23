package andrew_volostnykh.webrunner.grphics.components.json_editor;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SyntaxHighlighter {

	private static final Pattern PATTERN = Pattern.compile(
		"(?<KEY>\"(\\\\.|[^\"\\\\])*\"\\s*:)|" +              // "key":
			"(?<STRING>\"(\\\\.|[^\"\\\\])*\")|" +                // "string"
			"(?<NUMBER>\\b-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b)|" + // 123, -10.5, 1e10
			"(?<BOOLEAN>\\btrue\\b|\\bfalse\\b)|" +               // true/false
			"(?<NULL>\\bnull\\b)|" +                              // null
			"(?<VARIABLE>\\{\\{[^}]+}})|" +                       // {{variable}}
			"(?<BRACE>[{}\\[\\],:])"                              // { } [ ] , :
	);

	static StyleSpans<Collection<String>> computeHighlighting(String text) {
		Matcher matcher = PATTERN.matcher(text);
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		int last = 0;

		while (matcher.find()) {
			String style =
				matcher.group("KEY") != null ? "json-key" :
					matcher.group("STRING") != null ? "json-string" :
						matcher.group("NUMBER") != null ? "json-number" :
							matcher.group("BOOLEAN") != null ? "json-boolean" :
								matcher.group("NULL") != null ? "json-null" :
									matcher.group("VARIABLE") != null ? "json-variable" :
										matcher.group("BRACE") != null ? "json-brace" :
											"plain-text";

			spansBuilder.add(Collections.singleton("plain-text"), matcher.start() - last);
			spansBuilder.add(Collections.singleton(style), matcher.end() - matcher.start());
			last = matcher.end();
		}

		spansBuilder.add(Collections.singleton("plain-text"), text.length() - last);
		return spansBuilder.create();
	}
}
