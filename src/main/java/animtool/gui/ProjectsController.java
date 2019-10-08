package animtool.gui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ProjectsController {

    private static final String DARK_CSS = "/fxml/dark.css";
    private static final File configFile = new File("animtool.json");

    public StackPane rootPane;
    public VBox recentVBox;
    public Button themeButton;


    private double windowDragOffsetX, windowDragOffsetY;

    private File lastFolder = null;
    private final List<File> recentFolders = new ArrayList<>();
    private final BooleanProperty darkTheme = new SimpleBooleanProperty();


    @FXML
    public void initialize() {
        initWindowDragListeners();
        initDarkThemeListener();

        Platform.runLater(() -> {
            loadConfig();
            initRecentFolders();
        });
    }

    private void initDarkThemeListener() {
        darkTheme.addListener(observable -> {
            if (darkTheme.get()) {
                if (!rootPane.getScene().getStylesheets().contains(DARK_CSS))
                    rootPane.getScene().getStylesheets().add(DARK_CSS);
                themeButton.setText("Light");
            } else {
                rootPane.getScene().getStylesheets().remove(DARK_CSS);
                themeButton.setText("Dark");
            }
        });
    }

    private void initWindowDragListeners() {
        rootPane.setOnMousePressed(event -> {
            windowDragOffsetX = rootPane.getScene().getWindow().getX() - event.getScreenX();
            windowDragOffsetY = rootPane.getScene().getWindow().getY() - event.getScreenY();
        });
        rootPane.setOnMouseDragged(event -> {
            rootPane.getScene().getWindow().setX(event.getScreenX() + windowDragOffsetX);
            rootPane.getScene().getWindow().setY(event.getScreenY() + windowDragOffsetY);
        });
    }

    private void initRecentFolders() {
        for (File folder : recentFolders) {
            Label name = new Label(folder.getName());
            name.setMinWidth(Region.USE_PREF_SIZE);
            name.setFont(new Font(16));

            Label path = new Label(folder.getAbsolutePath());
            path.setTooltip(new Tooltip(path.getText()));
            path.setTextFill(Color.gray(0.5));
            HBox.setHgrow(path, Priority.ALWAYS);

            Button open = new Button("Open");
            open.setMinWidth(Region.USE_PREF_SIZE);
            open.setOnAction(event -> openProject(folder));

            HBox h = new HBox(5, name, path, open);
            h.setAlignment(Pos.CENTER);
            recentVBox.getChildren().add(h);
        }
    }

    private void loadConfig() {
        try {
            JSONObject json = new JSONObject(String.join("\n", Files.readAllLines(configFile.toPath())));

            // Get last folder if present
            if (json.has("last_folder")) {
                lastFolder = new File(json.getString("last_folder"));
            }

            // Get dark theme if present
            if (json.has("dark_theme")) {
                darkTheme.set(json.getBoolean("dark_theme"));
            }

            // Get recent folders if present
            if (json.has("recent")) {
                JSONArray arr = json.getJSONArray("recent");
                for (int i = 0; i < arr.length(); i++) {
                    recentFolders.add(new File(arr.getString(i)));
                }
            }
        } catch (IOException e) {
            Main.log.log(Level.WARNING, "Unable to read config json: " + configFile, e);
        }
    }

    private void saveConfig() throws IOException {
        JSONObject json = new JSONObject();
        json.put("last_folder", lastFolder.getAbsolutePath());
        json.put("dark_theme", darkTheme.get());
        recentFolders.forEach(s -> json.append("recent", s));

        try (FileWriter fw = new FileWriter(configFile)) {
            json.write(fw, 2, 0);
        }
    }

    public void openFolderButtonOnAction(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Open Folder");
        dc.setInitialDirectory(lastFolder);
        File file = dc.showDialog(rootPane.getScene().getWindow());

        if (file != null) {
            lastFolder = file.getParentFile();

            openProject(file);
        }
    }

    private void openProject(File folder) {
        recentFolders.remove(folder);
        recentFolders.add(0, folder);

        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(param -> new MainController(folder)); // TODO
            Scene scene = new Scene(loader.load());
            if (darkTheme.get()) scene.getStylesheets().add(DARK_CSS);
            stage.setScene(scene);
            stage.setTitle("AnimTool");
            stage.show();
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to open main stage", e);
        }

        ((Stage) rootPane.getScene().getWindow()).close();

        try {
            saveConfig();
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to save config file: " + configFile, e);
        }
    }

    public void themeButtonOnAction(ActionEvent event) {
        darkTheme.set(!darkTheme.get());
    }

    public void exitButtonOnAction(ActionEvent event) {
        try {
            saveConfig();
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to save config file: " + configFile, e);
        }
        Platform.exit();
    }

}
