module andrew_volostnykh.webrunner {
	requires javafx.controls;
	requires javafx.fxml;

	requires org.controlsfx.controls;

	requires org.kordamp.ikonli.core;
	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.materialdesign;

	requires java.net.http;
	requires com.fasterxml.jackson.databind;
	requires static lombok;
	requires java.desktop;
	requires org.fxmisc.richtext;

	opens andrew_volostnykh.webrunner to javafx.fxml;
	opens andrew_volostnykh.webrunner.collections to com.fasterxml.jackson.databind;
	opens andrew_volostnykh.webrunner.collections.persistence to com.fasterxml.jackson.databind;
	exports andrew_volostnykh.webrunner;
}