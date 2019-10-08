/*
 MIT License

 Copyright (c) 2019. Austin Thompson

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package settings;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class FolderSetting extends StringSetting {


    public FolderSetting(String identifier, String label, String tip, boolean hidden, String value) {
        super(identifier, label, tip, hidden, value);
    }

    public FolderSetting(String identifier, String value) {
        super(identifier, value);
    }

    public FolderSetting(String identifier) {
        super(identifier);
    }

    public FolderSetting hide() {
        setHidden(true);
        return this;
    }

    public FolderSetting tip(String tip) {
        setTip(tip);
        return this;
    }

    public FolderSetting label(String label) {
        setLabel(label);
        return this;
    }

    @Override
    public SettingNode makeJFXNode() {
        Label label = new Label(getLabel());
        TextField textField = new TextField(getValue());
        if (getTip() != null && !getTip().isEmpty()) {
            textField.setPromptText(getTip());
            textField.setTooltip(new Tooltip(getTip()));
        }
        Button browse = new Button("Browse");
        browse.setOnAction(event -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle(getLabel());
            if (getValue() != null && !getValue().isEmpty()) {
                File current = new File(getValue());
                if (current.exists()) {
                    dc.setInitialDirectory(current);
                }
            }
            File result = dc.showDialog(browse.getScene().getWindow());
            if (result != null) {
                textField.setText(result.getAbsolutePath());
            }
        });
        HBox h = new HBox(5, label, textField, browse);
        h.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textField, Priority.ALWAYS);

        return new SettingNode() {
            @Override
            public void applyToSetting() {
                setValue(textField.getText());
            }

            @Override
            public Node getNode() {
                return h;
            }
        };
    }

}
