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

import animtool.gui.Main;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Settings {

    private static final String VERSION_KEY = "version";
    private static final String SETTINGS_KEY = "settings";

    private final List<Setting> settings = new ArrayList<>();
    private int version = 1;


    public void load(File file) throws IOException, SettingsException, JSONException {
        String fileText = String.join("\n", Files.readAllLines(file.toPath()));
        JSONObject json = new JSONObject(fileText);

        if (!json.has(VERSION_KEY)) throw new SettingsException("No version tag");
        if (json.getInt(VERSION_KEY) != version) throw new SettingsException("Unknown settings version");

        if (json.has(SETTINGS_KEY)) {
            JSONArray arr = json.getJSONArray(SETTINGS_KEY);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject j = arr.getJSONObject(i);

                Setting setting = getSetting(j.getString(Setting.ID_KEY));
                if (setting != null) {
                    setting.initFromJSON(j);
                } else {
                    Main.log.warning("Unexpected setting: " + j);
                }
            }
        }
    }

    public Setting getSetting(String identifier) {
        for (Setting setting : settings) {
            if (setting.getID().equalsIgnoreCase(identifier)) {
                return setting;
            } else if (setting instanceof GroupSetting) {
                Setting child = ((GroupSetting) setting).getChild(identifier);
                if (child != null) {
                    return child;
                }
            }
        }

        return null;
    }

    public BooleanSetting getBool(String id) {
        return (BooleanSetting) getSetting(id);
    }

    public DoubleSetting getDouble(String id) {
        return (DoubleSetting) getSetting(id);
    }

    public FileSetting getFile(String id) {
        return (FileSetting) getSetting(id);
    }

    public FolderSetting getFolder(String id) {
        return (FolderSetting) getSetting(id);
    }

    public GroupSetting getGroup(String id) {
        return (GroupSetting) getSetting(id);
    }

    public IntSetting getInt(String id) {
        return (IntSetting) getSetting(id);
    }

    public StringSetting getString(String id) {
        return (StringSetting) getSetting(id);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Settings) {
            if (getVersion() != ((Settings) obj).getVersion()) return false;
            if (getSettings().size() != ((Settings) obj).getSettings().size()) return false;

            for (int i = 0; i < getSettings().size(); i++) {
                if (!getSettings().get(i).equals(((Settings) obj).getSettings().get(i))) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public void save(File file) throws IOException {
        JSONObject json = new JSONObject();
        json.put(VERSION_KEY, getVersion());

        for (Setting setting : settings) {
            json.append("settings", setting.toJSON());
        }

        try (FileWriter fw = new FileWriter(file)) {
            json.write(fw, 2, 0);
        }
    }

    static boolean equalsNullable(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;

        if (obj1 != null) {
            return obj1.equals(obj2);
        }

        return false;
    }

}
