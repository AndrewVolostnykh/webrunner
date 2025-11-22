package andrew_volostnykh.webrunner;

import andrew_volostnykh.webrunner.collections.persistence.CollectionPersistenceService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WebRunnerApplication extends Application {

	private static final CollectionPersistenceService collectionPersistenceService =
		new CollectionPersistenceService();

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/main-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 900, 700);
		scene.getStylesheets().add(
			getClass().getResource("/ui/styles/style.css").toExternalForm()
		);
		stage.setTitle("WebRunner");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}
