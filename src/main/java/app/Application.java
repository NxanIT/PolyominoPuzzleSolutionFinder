package app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException{

        
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("App-view.fxml"));
        Parent root = fxmlLoader.load();
        AppController controller = fxmlLoader.getController();
        controller.setupApp("C:/Users/User/Downloads/testPB");
        
        Scene scene = new Scene(root, 960, 540);
        stage.setTitle("PPSF - V0.1");
        stage.setScene(scene);
        stage.show();
    }
}
