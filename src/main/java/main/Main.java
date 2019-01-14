package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FilenameFilter;


public class Main extends Application {

    public static FilenameFilter imageFilter = (dir, name) -> {
        name = name.toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg");
    };


    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Simple Anim Preview");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
