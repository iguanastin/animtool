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

package animtool.gui.editor;


import animtool.animation.Frame;
import animtool.gui.Main;
import animtool.gui.media.DynamicImageView;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.logging.Level;

public class FrameListCell extends ListCell<Frame> {

    private static final String DEFAULT_STYLE_CLASS = "frame-list-cell";
    private static final String CUSTOM_DELAY_LABEL_STYLE_CLASS = "custom-delay-label";

    private final DynamicImageView imageView = new DynamicImageView();
    private final Label indexLabel = new Label(), delayLabel = new Label();
    private final TextField delayTextField = new TextField();
    private BorderPane topBorderPane;

    private final ChangeListener<Number> delayListener = (observable, oldValue, newValue) -> {
        if (newValue.intValue() > 0) {
            delayLabel.setText(newValue.intValue() + "ms");
            if (!delayLabel.getStyleClass().contains(CUSTOM_DELAY_LABEL_STYLE_CLASS)) delayLabel.getStyleClass().add(CUSTOM_DELAY_LABEL_STYLE_CLASS);
        } else {
            delayLabel.setText(getItem().getDefaultDelay() + "ms");
            delayLabel.getStyleClass().remove(CUSTOM_DELAY_LABEL_STYLE_CLASS);
        }
    };


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
        delayTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                updateItem(getItem(), false);
            }
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
                MenuItem defaultDelay = new MenuItem("Default Delay");
                defaultDelay.setOnAction(event1 -> item.setDelay(-1));

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

                ContextMenu cm = new ContextMenu(defaultDelay, new SeparatorMenuItem(), copyPath, openFolder);
                cm.show(this, event.getScreenX(), event.getScreenY());
            }
        });
    }

    @Override
    protected void updateItem(Frame item, boolean empty) {
        if (getItem() != null) {
            getItem().defaultDelayProperty().removeListener(delayListener);
            getItem().delayProperty().removeListener(delayListener);
        }

        super.updateItem(item, empty);

        imageView.setImage(null);
        indexLabel.setText(null);
        topBorderPane.setRight(null);
        if (item != null) {
            imageView.setImage(item.getThumbnail());
            indexLabel.setText(getIndex() + "");

            topBorderPane.setRight(delayLabel);
            if (item.getDelay() < 1) {
                delayLabel.setText(item.getDefaultDelay() + "ms");
                delayLabel.getStyleClass().remove(CUSTOM_DELAY_LABEL_STYLE_CLASS);
                item.defaultDelayProperty().addListener(delayListener);
            } else {
                delayLabel.setText(item.getDelay() + "ms");
                if (!delayLabel.getStyleClass().contains(CUSTOM_DELAY_LABEL_STYLE_CLASS)) delayLabel.getStyleClass().add(CUSTOM_DELAY_LABEL_STYLE_CLASS);
                item.delayProperty().addListener(delayListener);
            }
        }
    }

}
