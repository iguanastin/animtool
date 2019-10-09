package animtool.gui.editor;

import animtool.animation.Frame;
import animtool.export.GifSequenceWriter;
import animtool.gui.Main;
import animtool.gui.media.DynamicImageView;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.*;
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
    public TextField fpsTextField;

    private Image playIcon = null;
    private Image pauseIcon = null;

    private WatchService watcher;
    private WatchKey watchKey;
    private File currentFolder;

    private final ObjectProperty<Timeline> timeline = new SimpleObjectProperty<>();
    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final IntegerProperty defaultDelay = new SimpleIntegerProperty(125);

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

        defaultDelay.addListener((observable, oldValue, newValue) -> fpsTextField.setText(newValue.intValue() + ""));
        defaultDelay.addListener((observable, oldValue, newValue) -> refreshTimeline());
        fpsTextField.setText(1000 / defaultDelay.get() + "");
        fpsTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                parseFPSFromTextField();
            }
        });
        fpsTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                fpsTextField.setText(oldValue);
            }
        });
        fpsTextField.setOnAction(event -> parseFPSFromTextField());
        playing.addListener((observable, oldValue, newValue) -> {
            if (playing.get()) {
                timeline.get().play();
            } else {
                timeline.get().pause();
            }
        });
        frames.addListener((ListChangeListener<? super Frame>) c -> {
            while (c.next()) {
                c.getAddedSubList().forEach(o -> o.delayProperty().addListener((observable, oldValue, newValue) -> refreshTimeline()));
            }
        });

        setFolder(currentFolder);
    }

    private void parseFPSFromTextField() {
        int fps = 1000 / defaultDelay.get();

        try {
            int val = Integer.parseInt(fpsTextField.getText());

            if (val > 0 && val <= 1000) {
                fps = val;
                defaultDelay.set(1000 / fps);
            }
        } catch (NumberFormatException ignore) {
        }

        fpsTextField.setText(fps + "");
    }

    private void initIcons() {
        playIcon = new Image(getClass().getResource("/icons/play.png").toString());
        pauseIcon = new Image(getClass().getResource("/icons/pause.png").toString());

        playButton.setText(null);
        playButton.setGraphic(new ImageView(playIcon));
        playing.addListener((observable, oldValue, newValue) -> ((ImageView) playButton.getGraphic()).setImage(newValue ? pauseIcon : playIcon));

        leftButton.setText(null);
        leftButton.setGraphic(new ImageView(getClass().getResource("/icons/left.png").toString()));

        rightButton.setText(null);
        rightButton.setGraphic(new ImageView(getClass().getResource("/icons/right.png").toString()));

        pinButton.setText(null);
        pinButton.setGraphic(new ImageView(getClass().getResource("/icons/pin.png").toString()));
        pinButton.setTooltip(new Tooltip("Pin window on top"));

        exportButton.setText(null);
        exportButton.setGraphic(new ImageView(getClass().getResource("/icons/export.png").toString()));
        exportButton.setTooltip(new Tooltip("Export as gif"));
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
                Frame frame = new Frame(file, defaultDelay);
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

    private void close() {
        // TODO Save config

        Platform.exit();
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

        int time = 0; // Time counter
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(time), "Frame " + i, event -> {
                timeLineListView.scrollTo(frame);
                timeLineListView.getSelectionModel().select(frame);
            }));

            // Time has to be counted in a rolling manner
            time += frame.getComputedDelay();
        }
        // Event to display last keyframe for correct delay
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(time)));

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
        Frame tmp = new Frame(new File(path), defaultDelay);
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
        frames.remove(new Frame(new File(path), defaultDelay));
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
            frames.add(new Frame(new File(path), defaultDelay));
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

        TranslateTransition tt = new TranslateTransition(Duration.millis(100), controlsVBox);
        tt.setFromY(controlsVBox.getHeight());
        tt.setToY(0);
        tt.playFromStart();
    }

    public void rootPaneOnMouseExited(MouseEvent event) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), controlsVBox);
        tt.setFromY(0);
        tt.setToY(controlsVBox.getHeight());
        tt.setOnFinished(event1 -> rootPane.setBottom(null));
        tt.playFromStart();
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
            GifExportDialog d = new GifExportDialog(defaultDelay.get(), true, GifSequenceWriter.RESTORE_TO_BACKGROUND_DISPOSAL);
            Optional<GifExportConfig> result = d.showAndWait();

            if (result.isPresent()) {
                GifExportConfig config = result.get();

                List<BufferedImage> imgs = new ArrayList<>();
                for (Frame frame : frames) {
                    imgs.add(SwingFXUtils.fromFXImage(frame.getImage(), null));
                }

                try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
                    GifSequenceWriter gsw = new GifSequenceWriter(ios, imgs.get(0).getType(), config.delay, config.loop, config.disposal);

                    for (BufferedImage img : imgs) {
                        gsw.writeToSequence(img);
                    }

                    gsw.close();

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Success");
                    a.setHeaderText("Successfully exported GIF");
                    a.setContentText(file.getAbsolutePath());
                    a.showAndWait();
                } catch (IOException e) {
                    Main.log.log(Level.SEVERE, "Unable to create GIF writer", e);

                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("FAILED");
                    a.setHeaderText("Exception while attempting to export GIF");
                    a.setContentText(e.getLocalizedMessage());
                    a.showAndWait();
                }
            }
        }
    }

    public void rootPaneOnKeyPressed(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.Q) {
                close();
            }
        }
    }

}
