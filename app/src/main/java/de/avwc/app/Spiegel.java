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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class Spiegel extends Application {

    private static final Logger LOG = Logger.getLogger(Spiegel.class.getName());

    private static SpiegelDecoder decoder;

    public static void main(String[] args) {
        decoder = SpiegelDecoder.getInstance();
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

        stage.setTitle("SPIEGEL Deobfuscator");
        stage.setScene(scene);
        stage.show();
    }

    private EventHandler<ActionEvent> loadAction(TextField textField, WebEngine webEngine) {
        return (ActionEvent event) -> {
            //String url = textField.getText();

            URL url = null;
            try {
                url = new URL(textField.getText());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            URLConnection connection = null;
            try {
                connection = url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }


            BufferedReader buffReader = null;
            try {
                buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("utf-8")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuffer buffer = new StringBuffer();
            try {
                String line;
                while ((line = buffReader.readLine()) != null) {
                    buffer.append(line);
                    //LOG.warning(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            var plainText = decoder.decodeFromURL(url.toString());
            LOG.info("Loading url: " + url);

            //String xxx = decoder.decodeFromString(buffer.toString());
            //LOG.info(xxx);

            webEngine.loadContent(plainText);
            //webEngine.load(url.toString());

        };
    }
}
