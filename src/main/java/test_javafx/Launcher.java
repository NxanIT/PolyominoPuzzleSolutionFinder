package test_javafx;

import javafx.application.Application;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Launcher {
    public static void main(String[] args) throws URISyntaxException, MalformedURLException {

        
        
        Application.launch(HelloApplication.class, args);
    }
}
