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

package animtool.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import org.json.JSONObject;

public class BooleanSetting extends Setting {

    private static final String VALUE_KEY = "value";

    private final BooleanProperty value = new SimpleBooleanProperty();


    public BooleanSetting(String identifier, String label, String tip, boolean hidden, boolean value) {
        super(identifier, label, tip, hidden);
        this.value.set(value);
    }

    public BooleanSetting(String identifier, boolean value) {
        super(identifier);
        this.value.set(value);
    }

    public BooleanSetting(String identifier) {
        super(identifier);
    }

    public BooleanSetting hide() {
        setHidden(true);
        return this;
    }

    public BooleanSetting tip(String tip) {
        setTip(tip);
        return this;
    }

    public BooleanSetting label(String label) {
        setLabel(label);
        return this;
    }

    public boolean getValue() {
        return value.get();
    }

    public void setValue(boolean value) {
        this.value.set(value);
    }

    public BooleanProperty valueProperty() {
        return value;
    }

    @Override
    public SettingNode makeJFXNode() {
        CheckBox checkBox = new CheckBox(getLabel());
        checkBox.setSelected(getValue());
        if (getTip() != null && !getTip().isEmpty()) {
            checkBox.setTooltip(new Tooltip(getTip()));
        }

        return new SettingNode() {
            @Override
            public void applyToSetting() {
                setValue(checkBox.isSelected());
            }

            @Override
            public Node getNode() {
                return checkBox;
            }
        };
    }

    @Override
    JSONObject toJSON() {
        return super.toJSON().put(VALUE_KEY, getValue());
    }

    @Override
    void initFromJSON(JSONObject json) {
        setValue(json.getBoolean(VALUE_KEY));
    }

    @Override
    public String toString() {
        return "Boolean(id:\"" + getID() + "\", label:\"" + getLabel() + "\", value:" + getValue() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof BooleanSetting && ((BooleanSetting) obj).getValue() == getValue();
    }

}
