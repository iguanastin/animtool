/*
 * MIT License
 *
 * Copyright (c) 2019. Austin Thompson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package animtool.gui.projects;

import animtool.gui.Main;
import animtool.gui.editor.EditorController;
import animtool.gui.help.AboutController;
import animtool.gui.help.HelpController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ProjectsController {

    private static final Preferences prefs = Preferences.userRoot().node("iguanastin/animtool");

    public BorderPane rootPane;
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

            rootPane.getScene().getWindow().setOnCloseRequest(event -> close());
        });
    }

    private void initDarkThemeListener() {
        darkTheme.addListener(observable -> {
            if (darkTheme.get()) {
                if (!rootPane.getScene().getStylesheets().contains(Main.DARK_CSS))
                    rootPane.getScene().getStylesheets().add(Main.DARK_CSS);
                themeButton.setText("Light Theme");
            } else {
                rootPane.getScene().getStylesheets().remove(Main.DARK_CSS);
                themeButton.setText("Dark Theme");
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
            Button delete = new Button("X");
            delete.setOnAction(event -> {
                recentFolders.remove(folder);
                recentVBox.getChildren().remove(delete.getParent());
            });
            delete.prefWidthProperty().bind(delete.heightProperty());
            delete.setTooltip(new Tooltip("Forget"));

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

            HBox h = new HBox(5, delete, name, path, open);
            h.setAlignment(Pos.CENTER);
            recentVBox.getChildren().add(h);
        }
    }

    public static void open(Class ref) throws IOException {
        Stage stage = new Stage(StageStyle.UNDECORATED);
        Scene scene = new Scene(FXMLLoader.load(ref.getResource(Main.PROJECTS_FXML)));
        scene.getStylesheets().add(Main.COMMON_CSS);
        stage.setScene(scene);
        stage.setTitle(Main.TITLE);
        stage.show();
    }

    private void close() {
        try {
            saveConfig();
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to save preferences", e);
        }

        ((Stage) rootPane.getScene().getWindow()).close();
    }

    private void loadConfig() {
        lastFolder = new File(prefs.get("last-folder", ""));
        darkTheme.set(prefs.getBoolean("dark-theme", true));
        rootPane.getScene().getWindow().setX(prefs.getDouble("window-x", 300));
        rootPane.getScene().getWindow().setY(prefs.getDouble("window-y", 300));

        String[] recent = prefs.get("recent", "").split(";");
        for (String path : recent) {
            if (path.isBlank()) continue;

            File file = new File(path);
            if (file.isDirectory()) recentFolders.add(file);
        }
    }

    private void saveConfig() throws IOException {
        prefs.put("last-folder", lastFolder.getAbsolutePath());
        prefs.putBoolean("dark-theme", darkTheme.get());
        prefs.putDouble("window-x", rootPane.getScene().getWindow().getX());
        prefs.putDouble("window-y", rootPane.getScene().getWindow().getY());
        prefs.put("recent", recentFolders.stream().map(File::getAbsolutePath).collect(Collectors.joining(";")));
    }

    public void openFolderButtonOnAction(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Open Folder");
        if (lastFolder.isDirectory()) dc.setInitialDirectory(lastFolder);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Main.EDITOR_FXML));
            loader.setControllerFactory(param -> new EditorController(folder));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(Main.COMMON_CSS);
            if (darkTheme.get()) scene.getStylesheets().add(Main.DARK_CSS);
            stage.setScene(scene);
            stage.setTitle("AnimTool - " + folder.getAbsolutePath());
            stage.getIcons().addAll(Main.ICONS);
            stage.show();
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to open main stage", e);
        }

        close();
    }

    public void themeButtonOnAction(ActionEvent event) {
        darkTheme.set(!darkTheme.get());
    }

    public void exitButtonOnAction(ActionEvent event) {
        close();
    }

    public void rootPaneOnKeyPressed(KeyEvent event) {
        if (event.isShortcutDown()) {
            switch (event.getCode()) {
                case W:
                case Q:
                    close();
                    event.consume();
                    break;
            }
        } else {
            switch (event.getCode()) {
                case ESCAPE:
                    close();
                    event.consume();
                    break;
            }
        }
    }

    public void helpButtonOnAction(ActionEvent event) {
        try {
            HelpController.open(getClass(), darkTheme.get());
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to open Help stage", e);
        }
    }

    public void aboutButtonOnAction(ActionEvent event) {
        try {
            AboutController.open(getClass(), darkTheme.get());
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to open About stage", e);
        }
    }

}
