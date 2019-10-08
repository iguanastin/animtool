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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import menagerie.util.Util;
import org.json.JSONObject;

public class StringSetting extends Setting {

    private static final String VALUE_KEY = "value";

    private final StringProperty value = new SimpleStringProperty();


    public StringSetting(String identifier, String label, String tip, boolean hidden, String value) {
        super(identifier, label, tip, hidden);
        this.value.set(value);
    }

    public StringSetting(String identifier, String value) {
        super(identifier);
        this.value.set(value);
    }

    public StringSetting(String identifier) {
        super(identifier);
    }

    public StringSetting hide() {
        setHidden(true);
        return this;
    }

    public StringSetting tip(String tip) {
        setTip(tip);
        return this;
    }

    public StringSetting label(String label) {
        setLabel(label);
        return this;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public StringProperty valueProperty() {
        return value;
    }

    @Override
    public SettingNode makeJFXNode() {
        Label label = new Label(getLabel());
        TextField textField = new TextField(getValue());
        if (getTip() != null && !getTip().isEmpty()) {
            textField.setPromptText(getTip());
            textField.setTooltip(new Tooltip(getTip()));
        }
        HBox h = new HBox(5, label, textField);
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

    @Override
    void initFromJSON(JSONObject json) {
        if (json.has(VALUE_KEY)) {
            setValue(json.getString(VALUE_KEY));
        } else {
            setValue(null);
        }
    }

    @Override
    JSONObject toJSON() {
        return super.toJSON().put(VALUE_KEY, getValue());
    }

    @Override
    public String toString() {
        return "String(id:\"" + getID() + "\", label:\"" + getLabel() + "\", value:" + getValue() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof StringSetting && Util.equalsNullable(((StringSetting) obj).getValue(), getValue());
    }

}
