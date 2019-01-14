package main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;

import static java.nio.file.StandardWatchEventKinds.*;

public class MainController {

    public DynamicImageView previewImageView;
    public Button playPauseButton;
    public Label animFolderLabel;
    public ToggleButton alwaysOnTopToggleButton;
    public TextField fpsTextField;

    private WatchService watcher;
    private WatchKey watchKey;
    private File currentFolder;

    private Timeline timeline;
    private boolean playing = false;
    private int fps = 12;

    private final ArrayList<Frame> frames = new ArrayList<>();


    @FXML
    public void initialize() {
        initWatchService();
    }

    private void setFolder(File folder) {
        loadFramesFromFolder(folder);
        setWatchFolder(folder);
        currentFolder = folder;
    }

    private void loadFramesFromFolder(File folder) {
        frames.clear();
        File[] images = folder.listFiles((dir, name) -> {
            String work = name.toLowerCase();
            return work.endsWith(".png") || work.endsWith(".jpg") || work.endsWith(".jpeg");
        });
        if (images != null) {
            for (File file : images) {
                if (Main.imageFilter.accept(file.getParentFile(), file.getName())) {
                    frames.add(new Frame(file));
                }
            }
        }
        Collections.sort(frames);
        refreshTimeline();
    }

    private void setWatchFolder(File folder) {
        if (watchKey != null) watchKey.cancel();
        try {
            watchKey = folder.toPath().register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error registering WatchKey");
            a.setContentText("Unable to register WatchKey: " + folder.getAbsolutePath());
            a.showAndWait();
        }
    }

    private void initWatchService() {
        try {
            watcher = FileSystems.getDefault().newWatchService();

            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watcher.take();

                        for (WatchEvent event : key.pollEvents()) {
                            if (event.kind() == ENTRY_CREATE) {
                                fileCreated(((Path) event.context()).toFile());
                            } else if (event.kind() == ENTRY_DELETE) {
                                fileDeleted(((Path) event.context()).toFile());
                            } else if (event.kind() == ENTRY_MODIFY) {
                                fileModified(((Path) event.context()).toFile());
                            } else if (event.kind() == OVERFLOW) {
                                Platform.runLater(() -> {
                                    Alert a = new Alert(Alert.AlertType.ERROR);
                                    a.setTitle("Error in WatchService");
                                    a.setContentText("WatchService encountered OVERFLOW event!");
                                    a.showAndWait();
                                });
                            }
                        }

                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setTitle("Error running WatchService thread");
                        a.setContentText(e.getLocalizedMessage());
                        a.showAndWait();
                    });
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error creating WatchService");
            a.setContentText(e.getLocalizedMessage());
            a.showAndWait();
        }
    }

    private void setFramerate(int fps) {
        this.fps = fps;
        refreshTimeline();
    }

    private void refreshTimeline() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000.0 / fps * i), "Frame " + i, event -> updateImage(frame.getImage(false))));
        }

        if (playing) timeline.play();
        else if (!frames.isEmpty()) updateImage(frames.get(0).getImage(false));
    }

    private void fileModified(File file) {
        String path = currentFolder.getAbsolutePath();
        if (!path.endsWith("/")) path += "/";
        path += file.getName();
        Frame tmp = new Frame(new File(path));
        if (frames.contains(tmp)) {
            frames.get(frames.indexOf(tmp)).getImage(true);
        }
        System.out.println("File modified: " + path);
    }

    private void fileDeleted(File file) {
        String path = currentFolder.getAbsolutePath();
        if (!path.endsWith("/")) path += "/";
        path += file.getName();
        frames.remove(new Frame(new File(path)));
        Collections.sort(frames);
        refreshTimeline();
        System.out.println("File deleted: " + path);
    }

    private void fileCreated(File file) {
        String path = currentFolder.getAbsolutePath();
        if (!path.endsWith("/")) path += "/";
        path += file.getName();
        if (Main.imageFilter.accept(new File(path).getParentFile(), file.getName())) {
            frames.add(new Frame(new File(path)));
            Collections.sort(frames);
            refreshTimeline();
        }
        System.out.println("File created: " + path);
    }

    private void updateImage(Image img) {
        synchronized (previewImageView) {
            previewImageView.setImage(img);
        }
    }

    public void alwaysOnTopToggleOnAction(ActionEvent event) {
        ((Stage) alwaysOnTopToggleButton.getScene().getWindow()).setAlwaysOnTop(alwaysOnTopToggleButton.isSelected());
        event.consume();
    }

    public void playPauseButtonOnAction(ActionEvent event) {
        event.consume();

        updateFramerateFromTextfield();

        playing = !playing;
        if (playing) {
            playPauseButton.setText("Pause");
            timeline.play();
        } else {
            playPauseButton.setText("Play");
            timeline.pause();
        }
    }

    public void fpsTextFieldOnAction(ActionEvent event) {
        if (updateFramerateFromTextfield()) return;
        event.consume();
    }

    private boolean updateFramerateFromTextfield() {
        try {
            int i = Integer.parseInt(fpsTextField.getText());
            if (i <= 0 || i >= 100) throw new NumberFormatException();
            setFramerate(i);
        } catch (NumberFormatException e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Invalid FPS. Must be integer in range (1-100)");
            a.setTitle("Error");
            a.showAndWait();
            return true;
        }
        return false;
    }

    public void browseButtonOnAction(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        File directory = dc.showDialog(previewImageView.getScene().getWindow());
        if (directory != null) {
            setFolder(directory);
            animFolderLabel.setText(directory.getAbsolutePath());
        }
    }

}
