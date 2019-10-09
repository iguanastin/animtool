package animtool.gui.editor;


import animtool.animation.Frame;
import animtool.gui.Main;
import animtool.gui.media.DynamicImageView;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.logging.Level;

public class FrameListCell extends ListCell<Frame> {

    private static final String DEFAULT_STYLE_CLASS = "frame-list-cell";

    private final DynamicImageView imageView = new DynamicImageView();
    private final Label indexLabel = new Label(), delayLabel = new Label();
    private final TextField delayTextField = new TextField();
    private BorderPane topBorderPane;


    FrameListCell() {
        getStyleClass().addAll(DEFAULT_STYLE_CLASS);

        initGraphic();
        initContextMenu();
    }

    private void initGraphic() {
        BorderPane bp = new BorderPane(imageView);
        bp.setMinSize(Frame.THUMBNAIL_SIZE, Frame.THUMBNAIL_SIZE);
        bp.setMaxSize(Frame.THUMBNAIL_SIZE, Frame.THUMBNAIL_SIZE);
        topBorderPane = new BorderPane(null, null, delayLabel, null, indexLabel);
        delayTextField.setPrefWidth(50);
        delayTextField.setOnAction(event -> {
            getItem().setDelay(Integer.parseInt(delayTextField.getText()));
            updateItem(getItem(), false);
        });
        delayTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) updateItem(getItem(), false);
        });
        delayLabel.setOnMouseClicked(event -> {
            if (getItem() != null) {
                topBorderPane.setRight(delayTextField);
                delayTextField.setText(getItem().getDelay() + "");
                delayTextField.requestFocus();
                delayTextField.selectAll();
            }
        });
        setGraphic(new BorderPane(bp, topBorderPane, null, null, null));
    }

    private void initContextMenu() {
        setOnContextMenuRequested(event -> {
            Frame item = getItem();
            if (item != null) {
                MenuItem copyPath = new MenuItem("Copy Path");
                copyPath.setOnAction(event1 -> {
                    StringSelection selection = new StringSelection(item.getFile().getAbsolutePath());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                });

                MenuItem openFolder = new MenuItem("Open Folder");
                openFolder.setOnAction(event1 -> {
                    try {
                        Desktop.getDesktop().open(item.getFile().getParentFile());
                    } catch (IOException e) {
                        Main.log.log(Level.WARNING, "Unable to open file in desktop", e);
                    }
                });

                ContextMenu cm = new ContextMenu(copyPath, openFolder);
                cm.show(this, event.getScreenX(), event.getScreenY());
            }
        });
    }

    @Override
    protected void updateItem(Frame item, boolean empty) {
        super.updateItem(item, empty);

        imageView.setImage(null);
        indexLabel.setText(null);
        topBorderPane.setRight(null);
        setTooltip(null);
        if (item != null) {
            imageView.setImage(item.getThumbnail());
            indexLabel.setText(getIndex() + "");
            setTooltip(new Tooltip(item.getFile().getAbsolutePath()));

            topBorderPane.setRight(delayLabel);
            if (item.getDelay() < 1) {
                delayLabel.setText(item.getDefaultDelay() + "ms");
            } else {
                delayLabel.setText(item.getDelay() + "ms");
            }
        }
    }

}
