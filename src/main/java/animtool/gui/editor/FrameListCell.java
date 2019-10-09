package animtool.gui.editor;


import animtool.animation.Frame;
import animtool.gui.Main;
import animtool.gui.media.DynamicImageView;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.logging.Level;

public class FrameListCell extends ListCell<Frame> {

    private static final String DEFAULT_STYLE_CLASS = "frame-list-cell";

    private final DynamicImageView imageView = new DynamicImageView();
    private final Label indexLabel = new Label();


    FrameListCell() {
        getStyleClass().addAll(DEFAULT_STYLE_CLASS);

        BorderPane bp = new BorderPane(imageView);
        bp.setMinSize(Frame.THUMBNAIL_SIZE, Frame.THUMBNAIL_SIZE);
        bp.setMaxSize(Frame.THUMBNAIL_SIZE, Frame.THUMBNAIL_SIZE);
        setGraphic(new BorderPane(bp, new BorderPane(null, null, null, null, indexLabel), null, null, null));

        setOnContextMenuRequested(event -> {
            if (getItem() != null) {
                MenuItem copyPath = new MenuItem("Copy Path");
                copyPath.setOnAction(event1 -> {
                    StringSelection selection = new StringSelection(getItem().getFile().getAbsolutePath());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                });

                MenuItem openFolder = new MenuItem("Open Folder");
                openFolder.setOnAction(event1 -> {
                    try {
                        Desktop.getDesktop().open(getItem().getFile().getParentFile());
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
        setTooltip(null);
        if (item != null) {
            imageView.setImage(item.getThumbnail());
            indexLabel.setText(getIndex() + "");
            setTooltip(new Tooltip(item.getFile().getAbsolutePath()));
        }
    }

}
