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

package animtool.settings;

import animtool.gui.Main;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupSetting extends Setting {

    private static final String TOGGLEABLE_KEY = "toggleable";
    private static final String ENABLED_KEY = "enabled";
    private static final String CHILDREN_KEY = "children";

    private final List<Setting> children = new ArrayList<>();
    private final BooleanProperty enabled = new SimpleBooleanProperty(true);
    private final BooleanProperty toggleable = new SimpleBooleanProperty(false);


    public GroupSetting(String identifier, String label, String tip, boolean hidden, boolean toggleable, boolean enabled) {
        super(identifier, label, tip, hidden);
        this.toggleable.set(toggleable);
        this.enabled.set(enabled);
    }

    public GroupSetting(String identifier, String label, String tip, boolean hidden, boolean toggleable, boolean enabled, Setting... children) {
        this(identifier, label, tip, hidden, toggleable, enabled);
        for (Setting child : children) {
            getChildren().add(child);
        }
    }

    public GroupSetting(String identifier) {
        super(identifier);
    }

    public GroupSetting(String identifier, Setting... children) {
        this(identifier);
        for (Setting child : children) {
            getChildren().add(child);
        }
    }

    public GroupSetting disable() {
        setEnabled(false);
        return this;
    }

    public GroupSetting toggleable() {
        setToggleable(true);
        return this;
    }

    public GroupSetting hide() {
        setHidden(true);
        return this;
    }

    public GroupSetting tip(String tip) {
        setTip(tip);
        return this;
    }

    public GroupSetting label(String label) {
        setLabel(label);
        return this;
    }

    public List<Setting> getChildren() {
        return children;
    }

    public Setting getChild(String id) {
        for (Setting s : getChildren()) {
            if (s.getID().equalsIgnoreCase(id)) return s;
        }

        return null;
    }

    public boolean isToggleable() {
        return toggleable.get();
    }

    public void setToggleable(boolean toggleable) {
        this.toggleable.set(toggleable);
    }

    public BooleanProperty toggleableProperty() {
        return toggleable;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean b) {
        enabled.set(b);
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    @Override
    public SettingNode makeJFXNode() {
        VBox root = new VBox(5);
        root.setFillWidth(true);
        VBox v = new VBox(5);
        v.setPadding(new Insets(0, 0, 0, 20));
        v.setDisable(!isEnabled());

        CheckBox checkBox = null;
        if (isToggleable()) {
            checkBox = new CheckBox(getLabel());
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> v.setDisable(!newValue));
            checkBox.setSelected(isEnabled());
            checkBox.setFont(new Font("System Bold", Font.getDefault().getSize()));
            root.getChildren().add(checkBox);
        } else {
            Label label = new Label(getLabel());
            label.setFont(new Font("System Bold Italic", Font.getDefault().getSize()));
            root.getChildren().add(label);
        }

        root.getChildren().add(v);

        List<SettingNode> childNodes = new ArrayList<>();
        for (Setting child : getChildren()) {
            if (child.isHidden()) continue;

            SettingNode node = child.makeJFXNode();
            childNodes.add(node);
            v.getChildren().add(node.getNode());
        }

        final CheckBox finalCheckBox = checkBox;
        return new SettingNode() {
            @Override
            public void applyToSetting() {
                if (finalCheckBox != null) setEnabled(finalCheckBox.isSelected());
                childNodes.forEach(SettingNode::applyToSetting);
            }

            @Override
            public Node getNode() {
                return root;
            }
        };
    }

    @Override
    void initFromJSON(JSONObject json) {
        setToggleable(json.getBoolean(TOGGLEABLE_KEY));
        setEnabled(json.getBoolean(ENABLED_KEY));

        if (json.has(CHILDREN_KEY)) {
            JSONArray arr = json.getJSONArray(CHILDREN_KEY);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject j = arr.getJSONObject(i);

                Setting setting = getChild(j.getString(Setting.ID_KEY));
                if (setting != null) {
                    setting.initFromJSON(j);
                } else {
                    Main.log.warning("Unexpected setting found: " + j);
                }
            }
        }
    }

    @Override
    JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put(TOGGLEABLE_KEY, isToggleable());
        json.put(ENABLED_KEY, isEnabled());
        for (Setting setting : getChildren()) {
            json.append(CHILDREN_KEY, setting.toJSON());
        }
        return json;
    }

    @Override
    public String toString() {
        return "Group(id:\"" + getID() + "\", label:\"" + getLabel() + "\", toggleable:" + isToggleable() + ", enabled:" + isEnabled() + ", children:" + getChildren().size() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) && obj instanceof GroupSetting && ((GroupSetting) obj).isToggleable() == isToggleable() && ((GroupSetting) obj).isEnabled() == isEnabled()) {
            if (((GroupSetting) obj).getChildren().size() != getChildren().size()) return false;
            for (int i = 0; i < getChildren().size(); i++) {
                if (!getChildren().get(i).equals(((GroupSetting) obj).getChildren().get(i))) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

}
