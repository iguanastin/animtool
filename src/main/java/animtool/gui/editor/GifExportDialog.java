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

import animtool.export.GifSequenceWriter;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class GifExportDialog extends Dialog<GifExportConfig> {

    private final Map<String, String> disposalMap = new HashMap<>();


    public GifExportDialog(double delay, boolean loop, String disposalMethod) {
        disposalMap.put("Do nothing", GifSequenceWriter.NONE_DISPOSAL);
        disposalMap.put("Restore to background", GifSequenceWriter.RESTORE_TO_BACKGROUND_DISPOSAL);
        disposalMap.put("Restore to previous", GifSequenceWriter.RESTORE_TO_PREVIOUS_DISPOSAL);

        setTitle("Export Gif");

        TextField delayTextField = new TextField(delay + "");
        HBox delayHBox = new HBox(5, new Label("Default Delay:"), delayTextField);
        delayHBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox loopCheckBox = new CheckBox();
        loopCheckBox.setSelected(loop);
        HBox loopHBox = new HBox(5, new Label("Looping:"), loopCheckBox);
        loopHBox.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<String> disposalChoiceBox = new ChoiceBox<>();
        disposalChoiceBox.getItems().addAll(disposalMap.keySet());
        for (Map.Entry<String, String> entry : disposalMap.entrySet()) {
            if (entry.getKey().equals(disposalMethod)) disposalChoiceBox.getSelectionModel().select(entry.getKey());
        }
        if (disposalChoiceBox.getSelectionModel().isEmpty()) disposalChoiceBox.getSelectionModel().selectFirst();
        HBox disposalHBox = new HBox(5, new Label("Disposal:"), disposalChoiceBox);
        disposalHBox.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(5, new Label("GIF export settings"), delayHBox, loopHBox, disposalHBox);
        getDialogPane().setContent(vBox);

        ButtonType ok = new ButtonType("Export", ButtonBar.ButtonData.FINISH);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(ok, cancel);

        setResultConverter(param -> {
            if (param.equals(ok)) {
                double d = delay;
                try {
                    double val = Double.parseDouble(delayTextField.getText());
                    if (val > 0) {
                        d = val;
                    }
                } catch (NumberFormatException ignore) {
                }
                return new GifExportConfig((int) d, loopCheckBox.isSelected(), disposalMap.get(disposalChoiceBox.getValue()));
            } else {
                return null;
            }
        });
    }

}
