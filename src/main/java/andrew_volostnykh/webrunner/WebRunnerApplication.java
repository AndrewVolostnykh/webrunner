package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.grphics.WindowResizer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class WebRunnerApplication extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/main-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 1100, 700);
		JMetro jMetro = new JMetro(Style.DARK);
		jMetro.setScene(scene);

		scene.getStylesheets().add(
			getClass().getResource("/ui/styles/style.css").toExternalForm()
		);
		stage.getIcons().add(
			new Image(getClass().getResourceAsStream("/icons/app-icon.png"))
		);
		stage.setTitle("WebRunner");
		stage.setScene(scene);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.show();
		WindowResizer.apply(scene.getRoot(), stage);
	}

	public static void main(String[] args) {
		launch();
	}
}
