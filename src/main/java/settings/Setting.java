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

import org.json.JSONObject;

public abstract class Setting {

    static final String ID_KEY = "id";

    private final String id;
    private String label;
    private String tip;
    private boolean hidden;


    public Setting(String id, String label, String tip, boolean hidden) {
        this(id);
        this.label = label;
        this.tip = tip;
        this.hidden = hidden;
    }

    public Setting(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public abstract SettingNode makeJFXNode();

    abstract void initFromJSON(JSONObject json);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Setting) {
            Setting s = (Setting) obj;
            String id1 = getID(), id2 = s.getID();
            String l1 = getLabel(), l2 = s.getLabel();
            String tip1 = getTip(), tip2 = s.getTip();
            boolean h1 = isHidden(), h2 = s.isHidden();
            return equalsNullable(id1, id2) && equalsNullable(l1, l2) && equalsNullable(tip1, tip2) && h1 == h2;
        }

        return false;
    }

    JSONObject toJSON() {
        return new JSONObject().put(ID_KEY, getID());
    }

    private static boolean equalsNullable(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;

        if (obj1 != null) {
            return obj1.equals(obj2);
        }

        return false;
    }

}
