package test_javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {

        
        URI U2 = new URI("file:/C:/Informatik/Scala%203/PPSF/src/main/resources/hello-view.fxml");
        URL U = U2.toURL();
        
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        
        
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
