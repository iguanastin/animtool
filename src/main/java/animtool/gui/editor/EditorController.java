package animtool.gui.editor;

import animtool.gui.media.DynamicImageView;
import animtool.gui.Main;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import animtool.animation.Frame;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;

import static java.nio.file.StandardWatchEventKinds.*;

public class EditorController {

    public BorderPane rootPane;
    public DynamicImageView previewImageView;
    public ListView<Frame> timeLineListView;
    public VBox controlsVBox;

    private WatchService watcher;
    private WatchKey watchKey;
    private File currentFolder;

    private final ObjectProperty<Timeline> timeline = new SimpleObjectProperty<>();
    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final IntegerProperty fps = new SimpleIntegerProperty(12);

    private final ObservableList<Frame> frames = FXCollections.observableArrayList();


    public EditorController(File folder) {
        currentFolder = folder;
    }

    @FXML
    public void initialize() {
        initWatchService();
        initTimeLineView();

        fps.addListener((observable, oldValue, newValue) -> refreshTimeline());
        playing.addListener((observable, oldValue, newValue) -> {
            if (playing.get()) {
                timeline.get().play();
            } else {
                timeline.get().pause();
            }
        });

        setFolder(currentFolder);
    }

    private void initTimeLineView() {
        frames.addListener((ListChangeListener<? super Frame>) c -> {
            timeLineListView.getItems().clear();
            timeLineListView.getItems().addAll(c.getList());
        });
        timeLineListView.setCellFactory(param -> new FrameListCell());
        timeLineListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super Frame>) c -> {
            if (!c.getList().isEmpty()) previewImageView.setImage(c.getList().get(0).getImage());
        });
    }

    /**
     * @param folder Folder containing the animation frames.
     */
    private void setFolder(File folder) {
        loadFramesFromFolder(folder);
        setWatchFolder(folder);
        currentFolder = folder;
    }

    /**
     * Loads animation frames from the files in a folder.
     *
     * @param folder Folder containing animation frames.
     */
    private void loadFramesFromFolder(File folder) {
        frames.clear();
        File[] images = folder.listFiles(Main.imageFilter);
        for (File file : Objects.requireNonNull(images)) {
            if (Main.imageFilter.accept(file.getParentFile(), file.getName())) {
                Frame frame = new Frame(file);
                frame.loadImage();
                frames.add(frame);
            }
        }
        Collections.sort(frames);
        refreshTimeline();
    }

    /**
     * @param folder Folder to watch for events.
     */
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

    /**
     * Initializes the watch service on the current folder, listening for file events.
     */
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

    /**
     * Sets the framerate of the animation.
     *
     * @param fps Framerate in frames per second.
     */
    private void setFramerate(int fps) {
        this.fps.set(fps);
    }

    /**
     * Refreshes the timeline object with current images.
     */
    private void refreshTimeline() {
        if (timeline.get() != null) {
            timeline.get().stop();
            timeline.set(null);
        }

        Timeline tl = new Timeline();
        timeline.set(tl);
        tl.setCycleCount(Animation.INDEFINITE);

        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / fps.get() * i), "Frame " + i, event -> {
                previewImageView.setImage(frame.getImage());
                timeLineListView.scrollTo(frame);
                timeLineListView.getSelectionModel().select(frame);
            }));
        }

        if (playing.get()) tl.play();
        else if (!frames.isEmpty()) previewImageView.setImage(frames.get(0).getImage());
    }

    /**
     * Called when a file modification event is captured.
     *
     * @param file File that was modified.
     */
    private void fileModified(File file) {
        String path = currentFolder.getAbsolutePath();
        if (!path.endsWith("/")) path += "/";
        path += file.getName();
        Frame tmp = new Frame(new File(path));
        if (frames.contains(tmp)) {
            frames.get(frames.indexOf(tmp)).loadImage();
        }
        System.out.println("File modified: " + path);
    }

    /**
     * Called when a file deletion event is captured.
     *
     * @param file File that was deleted.
     */
    private void fileDeleted(File file) {
        String path = currentFolder.getAbsolutePath();
        if (!path.endsWith("/")) path += "/";
        path += file.getName();
        frames.remove(new Frame(new File(path)));
        Collections.sort(frames);
        refreshTimeline();
        System.out.println("File deleted: " + path);
    }

    /**
     * Called when a file creation event is captured.
     *
     * @param file File that was created.
     */
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

    /**
     * Parses text from the framerate textfield and attempts to convert it into a valid int.
     *
     * @return True if it failed.
     */
    private boolean updateFramerateFromTextfield() {
        try {
            int i = Integer.parseInt("12"); // TODO
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

    public void alwaysOnTopToggleOnAction(ActionEvent event) {
        ((Stage) rootPane.getScene().getWindow()).setAlwaysOnTop(true); // TODO
        event.consume();
    }

    public void playButtonOnAction(ActionEvent event) {
        playing.set(!playing.get());
    }

    public void rootPaneOnMouseEntered(MouseEvent event) {
        rootPane.setBottom(controlsVBox);
    }

    public void rootPaneOnMouseExited(MouseEvent event) {
        rootPane.setBottom(null);
    }

}
