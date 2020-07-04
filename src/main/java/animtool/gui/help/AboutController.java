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

package animtool.gui.help;

import animtool.gui.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

public class AboutController {

    public VBox rootPane;
    public Label versionLabel;
    public Hyperlink githubHyperlink;


    @FXML
    public void initialize() {
        versionLabel.setText(Main.VERSION);
        githubHyperlink.setText(Main.GITHUB);
    }

    public static void open(Class<?> context, boolean dark) throws IOException {
        Parent root = FXMLLoader.load(context.getResource(Main.ABOUT_FXML));
        Dialog d = new Dialog();
        d.setTitle("About");
        d.getDialogPane().setContent(root);
        d.getDialogPane().getStylesheets().add(Main.COMMON_CSS);
        if (dark) d.getDialogPane().getStylesheets().add(Main.DARK_CSS);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        ((Stage) d.getDialogPane().getScene().getWindow()).getIcons().addAll(Main.ICONS);
        d.showAndWait();
    }

    public void openLogButtonOnAction(ActionEvent event) {
        try {
            Desktop.getDesktop().open(Main.LOG_FILE);
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to open log file", e);
        }
    }

    public void basicHyperlinkOnAction(ActionEvent event) {
        String url = ((Hyperlink) event.getSource()).getText();
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            Main.log.log(Level.SEVERE, "Failed to browse URL: " + url, e);
        }
    }

    private void close() {
        ((Stage) rootPane.getScene().getWindow()).close();
    }

    public void rootPaneKeyPressed(KeyEvent event) {
        if (event.isShortcutDown()) {
            switch (event.getCode()) {
                case W:
                case Q:
                    close();
                    event.consume();
                    break;
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            close();
            event.consume();
        }
    }

}
