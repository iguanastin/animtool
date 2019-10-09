package animtool.gui.editor;

import animtool.export.GifSequenceWriter;
import animtool.gui.Main;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GifExportDialog extends Dialog<GifExportConfig> {

    public GifExportDialog(int delay, boolean loop, String disposalMethod) {
        setTitle("Export Gif");

        TextField delayTextField = new TextField(delay + "");
        HBox delayHBox = new HBox(5, new Label("Delay:"), delayTextField);
        delayHBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox loopCheckBox = new CheckBox();
        loopCheckBox.setSelected(loop);
        HBox loopHBox = new HBox(5, new Label("Looping:"), loopCheckBox);
        loopHBox.setAlignment(Pos.CENTER_LEFT);

        ChoiceBox<String> disposalChoiceBox = new ChoiceBox<>();
        disposalChoiceBox.getItems().addAll(GifSequenceWriter.NONE_DISPOSAL, GifSequenceWriter.RESTORE_TO_BACKGROUND_DISPOSAL, GifSequenceWriter.RESTORE_TO_PREVIOUS_DISPOSAL, GifSequenceWriter.UNDEFINED_DISPOSAL_METHOD_4, GifSequenceWriter.UNDEFINED_DISPOSAL_METHOD_5, GifSequenceWriter.UNDEFINED_DISPOSAL_METHOD_6, GifSequenceWriter.UNDEFINED_DISPOSAL_METHOD_6, GifSequenceWriter.UNDEFINED_DISPOSAL_METHOD_7);
        disposalChoiceBox.getSelectionModel().select(disposalMethod);
        HBox disposalHBox = new HBox(5, new Label("Disposal:"), disposalChoiceBox);
        disposalHBox.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(5, new Label("GIF export settings"), delayHBox, loopHBox, disposalHBox);
        getDialogPane().setContent(vBox);

        ButtonType ok = new ButtonType("Export", ButtonBar.ButtonData.FINISH);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(ok, cancel);
        setResultConverter(param -> param.equals(ok) ? new GifExportConfig(Integer.parseInt(delayTextField.getText()), loopCheckBox.isSelected(), disposalChoiceBox.getValue()) : null);
    }

}
