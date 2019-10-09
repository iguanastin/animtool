package animtool.gui.editor;

import animtool.animation.Frame;
import animtool.export.GifSequenceWriter;
import animtool.gui.Main;
import animtool.gui.media.DynamicImageView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static java.nio.file.StandardWatchEventKinds.*;

public class EditorController {

    public BorderPane rootPane;
    public DynamicImageView previewImageView;
    public ListView<Frame> timeLineListView;
    public VBox controlsVBox;
    public Button leftButton;
    public Button playButton;
    public Button rightButton;
    public ToggleButton pinButton;
    public Button exportButton;

    private Image playIcon = null;
    private Image pauseIcon = null;

    private WatchService watcher;
    private WatchKey watchKey;
    private File currentFolder;

    private final ObjectProperty<Timeline> timeline = new SimpleObjectProperty<>();
    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final IntegerProperty fps = new SimpleIntegerProperty(8);

    private final ObservableList<Frame> frames = FXCollections.observableArrayList();


    public EditorController(File folder) {
        currentFolder = folder;
    }

    @FXML
    public void initialize() {
        rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                playing.set(!playing.get());
                event.consume();
            }
        });

        initWatchService();
        initTimeLineView();
        initIcons();

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

    private void initIcons() {
        playIcon = new Image(getClass().getResource("/icons/play.png").toString());
        pauseIcon = new Image(getClass().getResource("/icons/pause.png").toString());

        playButton.setText(null);
        playButton.setGraphic(new ImageView(playIcon));
        playButton.prefWidthProperty().bind(playButton.heightProperty());
        playing.addListener((observable, oldValue, newValue) -> ((ImageView) playButton.getGraphic()).setImage(newValue ? pauseIcon : playIcon));

        leftButton.setText(null);
        leftButton.setGraphic(new ImageView(getClass().getResource("/icons/left.png").toString()));
        leftButton.prefWidthProperty().bind(leftButton.heightProperty());

        rightButton.setText(null);
        rightButton.setGraphic(new ImageView(getClass().getResource("/icons/right.png").toString()));
        rightButton.prefWidthProperty().bind(rightButton.heightProperty());

        pinButton.setText(null);
        pinButton.setGraphic(new ImageView(getClass().getResource("/icons/pin.png").toString()));
        pinButton.setTooltip(new Tooltip("Pin window on top"));
        pinButton.prefWidthProperty().bind(pinButton.heightProperty());

        exportButton.setText(null);
        exportButton.setGraphic(new ImageView(getClass().getResource("/icons/export.png").toString()));
        exportButton.setTooltip(new Tooltip("Export as gif"));
        exportButton.prefWidthProperty().bind(exportButton.heightProperty());
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
        timeLineListView.getSelectionModel().select(0);
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
                timeLineListView.scrollTo(frame);
                timeLineListView.getSelectionModel().select(frame);
            }));
        }
        // Necessary as a sort of cleanup. Without this next line, the last frame never gets any time to show.
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / fps.get() * frames.size())));

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

    public void playButtonOnAction(ActionEvent event) {
        playing.set(!playing.get());
    }

    public void rootPaneOnMouseEntered(MouseEvent event) {
        rootPane.setBottom(controlsVBox);
    }

    public void rootPaneOnMouseExited(MouseEvent event) {
        rootPane.setBottom(null);
    }

    public void leftButtonOnAction(ActionEvent event) {
        int i = timeLineListView.getSelectionModel().getSelectedIndex();
        if (i > 0) {
            timeLineListView.getSelectionModel().select(i - 1);
        }
    }

    public void rightButtonOnAction(ActionEvent event) {
        int i = timeLineListView.getSelectionModel().getSelectedIndex();
        if (i < timeLineListView.getItems().size() - 1) {
            timeLineListView.getSelectionModel().select(i + 1);
        }
    }

    public void pinButtonOnAction(ActionEvent event) {
        ((Stage) rootPane.getScene().getWindow()).setAlwaysOnTop(pinButton.isSelected());
    }

    public void exportButtonOnAction(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(currentFolder);
        fc.setInitialFileName("result.gif");
        fc.setTitle("Export as GIF");
        fc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("GIF image", "*.gif"));
        File file = fc.showSaveDialog(rootPane.getScene().getWindow());

        if (file != null) {
            List<BufferedImage> imgs = new ArrayList<>();
            for (Frame frame : frames) {
                imgs.add(SwingFXUtils.fromFXImage(frame.getImage(), null));
            }

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
                GifSequenceWriter gsw = new GifSequenceWriter(ios, imgs.get(0).getType(), 1000 / fps.get(), true);

                for (BufferedImage img : imgs) {
                    gsw.writeToSequence(img);
                }

                gsw.close();
            } catch (IOException e) {
                Main.log.log(Level.SEVERE, "Unable to create GIF writer", e);
            }
        }
    }

}
