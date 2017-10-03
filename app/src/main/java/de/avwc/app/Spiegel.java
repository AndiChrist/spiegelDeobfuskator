package de.avwc.app;
/**
 * Created by andichrist on 21.05.17.
 */

import de.avwc.decoder.SpiegelDecoder;

import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Spiegel extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER_LEFT);

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);

        TextField textField = new TextField();
        textField.setPrefWidth(750);

        Button button = new Button("Load");
        button.setMinWidth(50);
        hbox.getChildren().addAll(textField, button);
        vbox.getChildren().add(hbox);

        WebView browser = new WebView();
        WebEngine webEngine = browser.getEngine();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(browser);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        vbox.getChildren().add(browser);

        button.setOnAction(loadAction(textField, webEngine));
        textField.setOnAction(loadAction(textField, webEngine));

        Scene scene = new Scene(vbox);

        stage.setScene(scene);
        stage.show();
    }

    private EventHandler<ActionEvent> loadAction(TextField textField, WebEngine webEngine) {
        return (ActionEvent event) -> {
            String url = textField.getText();
            String plainText = SpiegelDecoder.decodeFromURL(url);
            System.out.println("Loading url: " + url);

            webEngine.loadContent(plainText);
        };
    }
}
