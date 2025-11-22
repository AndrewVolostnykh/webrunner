module andrew_volostnykh.webrunner {
	requires javafx.controls;
	requires javafx.fxml;

	requires org.controlsfx.controls;

	requires org.kordamp.ikonli.core;
	requires org.kordamp.ikonli.javafx;
//	requires org.kordamp.bootstrapfx.core;
	requires org.kordamp.ikonli.materialdesign;

	requires java.net.http;
	requires com.fasterxml.jackson.databind;
	requires static lombok;

	opens andrew_volostnykh.webrunner to javafx.fxml;
	exports andrew_volostnykh.webrunner;
}