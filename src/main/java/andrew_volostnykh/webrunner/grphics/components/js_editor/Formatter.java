package andrew_volostnykh.webrunner.grphics.components.js_editor;

import lombok.SneakyThrows;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.nio.file.Files;
import java.nio.file.Path;

public class Formatter {

	public static String beautifyJs(String code) {
		try (
			Context context = Context.newBuilder("js")
				.allowAllAccess(true)
				.option("js.ecmascript-version", "2023")
				.build()
		) {

			context.eval("js", loadResource("js/standalone.js"));
			context.eval("js", loadResource("js/parser-babel.js"));

			String safeCode = code.replace("`", "\\`");

			String script = """
prettier.format(`%s`, {
    parser: 'babel',
    plugins: [prettierPlugins.babel],
    semi: true,
    singleQuote: true
});
""".formatted(safeCode);

			Value result = context.eval("js", script);

			return result.asString();
		} catch (Exception e) {
			e.printStackTrace();
			return code;
		}
	}

	@SneakyThrows
	private static String loadResource(String path) {
		return Files.readString(Path.of(
			Formatter.class.getClassLoader()
				.getResource(path)
				.toURI()
		));
	}
}
