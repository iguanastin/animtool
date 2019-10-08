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

import javafx.beans.property.*;
import menagerie.gui.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.logging.Level;

/**
 * A JavaFX Application settings object supporting 4 types, file storing, and settings listeners via Property objects.
 * <p>
 * Property events are handled on the FX thread.
 */
public class OldSettings {

    /**
     * Settings keys
     */
    public enum Key {VLCJ_PATH, LICENSES_AGREED, USER_FILETYPES, USE_FILENAME_FROM_URL, BACKUP_DATABASE, WINDOW_MAXIMIZED, DO_AUTO_IMPORT, AUTO_IMPORT_MOVE_TO_DEFAULT, MUTE_VIDEO, REPEAT_VIDEO, GRID_WIDTH, WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_X, WINDOW_Y, DEFAULT_FOLDER, DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, AUTO_IMPORT_FOLDER, CONFIDENCE, SHOW_HELP_ON_START, TAG_TAGME, TAG_VIDEO, TAG_IMAGE, EXPAND_ITEM_INFO}

    private final Map<Key, Property> vars = new HashMap<>();
    private File file;


    /**
     * Constructs a settings object using settings read from file. If setting is not present in the file, default value is used.
     *
     * @param file File to read from
     */
    public OldSettings(File file) {
        this();

        if (file != null) {
            this.file = file;
            try {
                Scanner scan = new Scanner(file);

                while (scan.hasNextLine()) {
                    String line = scan.nextLine();
                    if (line.startsWith("#") || line.isEmpty()) continue;
                    Main.log.config("Settings read line: " + line);

                    try {
                        final int firstColonIndex = line.indexOf(':');
                        final int secondColonIndex = line.indexOf(':', firstColonIndex + 1);
                        final Key key = keyFromName(line.substring(0, firstColonIndex));
                        final String typeName = line.substring(firstColonIndex + 1, secondColonIndex);
                        final String valueString = line.substring(secondColonIndex + 1);

                        if (key == null) {
                            Main.log.warning("Settings tried to load unknown key in line: " + line);
                            continue;
                        }

                        if (typeName.equalsIgnoreCase("BOOLEAN")) {
                            setBoolean(key, Boolean.parseBoolean(valueString));
                        } else if (typeName.equalsIgnoreCase("STRING")) {
                            setString(key, valueString);
                        } else if (typeName.equalsIgnoreCase("DOUBLE")) {
                            setDouble(key, Double.parseDouble(valueString));
                        } else if (typeName.equalsIgnoreCase("INTEGER")) {
                            setInt(key, Integer.parseInt(valueString));
                        } else {
                            Main.log.warning("Settings tried to load unknown type from line: " + line);
                        }
                    } catch (Exception e) {
                        Main.log.log(Level.WARNING, "Error trying to read settings line: " + line);
                    }
                }

                scan.close();
            } catch (IOException e) {
                Main.log.log(Level.WARNING, "Error trying to read settings file: " + file, e);
            }
        }
    }

    /**
     * Constructs a settings object using default settings.
     */
    public OldSettings() {
        setBoolean(Key.USE_FILENAME_FROM_URL, true);
        setBoolean(Key.BACKUP_DATABASE, true);
        setBoolean(Key.WINDOW_MAXIMIZED, true);
        setBoolean(Key.DO_AUTO_IMPORT, false);
        setBoolean(Key.AUTO_IMPORT_MOVE_TO_DEFAULT, true);
        setBoolean(Key.MUTE_VIDEO, true);
        setBoolean(Key.REPEAT_VIDEO, true);
        setBoolean(Key.SHOW_HELP_ON_START, true);
        setBoolean(Key.TAG_TAGME, true);
        setBoolean(Key.TAG_IMAGE, false);
        setBoolean(Key.TAG_VIDEO, true);
        setBoolean(Key.EXPAND_ITEM_INFO, false);
        setInt(Key.GRID_WIDTH, 2);
        setInt(Key.WINDOW_WIDTH, Integer.MIN_VALUE);
        setInt(Key.WINDOW_HEIGHT, Integer.MIN_VALUE);
        setInt(Key.WINDOW_X, Integer.MIN_VALUE);
        setInt(Key.WINDOW_Y, Integer.MIN_VALUE);
        setInt(Key.LICENSES_AGREED, 0);
        setString(Key.DEFAULT_FOLDER, null);
        setString(Key.DATABASE_URL, "~/menagerie");
        setString(Key.DATABASE_USER, "sa");
        setString(Key.DATABASE_PASSWORD, "");
        setString(Key.AUTO_IMPORT_FOLDER, null);
        setString(Key.USER_FILETYPES, null);
        setString(Key.VLCJ_PATH, null);
        setDouble(Key.CONFIDENCE, 0.95);
    }

    /**
     * Saves the current settings to a file.
     *
     * @param file File to write settings to.
     * @throws FileNotFoundException If file cannot be created when it does not exist.
     */
    public void save(File file) throws FileNotFoundException {
        this.file = file;

        save();
    }

    /**
     * Saves the current settings to the file stored in this object.
     *
     * @throws FileNotFoundException If the file cannot be created when it does not exist.
     */
    public void save() throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(file);

        writer.println("# ------------- Menagerie Settings --------------");
        writer.println("# " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()).format(new Date().toInstant()));

        for (Map.Entry<Key, Property> entry : vars.entrySet()) {
            // Prefix + Delimiter
            writer.print(entry.getKey() + ":");

            // Type
            if (entry.getValue() instanceof BooleanProperty) {
                writer.print("BOOLEAN");
            } else if (entry.getValue() instanceof StringProperty) {
                writer.print("STRING");
            } else if (entry.getValue() instanceof DoubleProperty) {
                writer.print("DOUBLE");
            } else if (entry.getValue() instanceof IntegerProperty) {
                writer.print("INTEGER");
            }

            // Delimiter
            writer.print(":");

            // TODO: Sanitize strings
            // Value
            if (entry.getValue().getValue() != null) {
                writer.print(entry.getValue().getValue());
            }

            // Newline
            writer.println();
        }

        writer.close();
    }

    /**
     * Sets a boolean value for the given key.
     *
     * @param key   Key to store value at.
     * @param value Value to store
     */
    public void setBoolean(Key key, boolean value) {
        BooleanProperty get = (BooleanProperty) vars.get(key);
        if (get != null) {
            get.setValue(value);
        } else {
            vars.put(key, new SimpleBooleanProperty(value));
        }
    }

    private static Key keyFromName(String name) {
        for (Key key : Key.values()) {
            if (key.toString().equals(name)) return key;
        }

        return null;
    }

    /**
     * Sets a string value for the given key.
     *
     * @param key   Key to store value at.
     * @param value Value to store.
     * @return True if the string was accepted. False if no change was made because the string is not sanitary.
     */
    public boolean setString(Key key, String value) {
        if (value != null && value.contains("\n")) return false;

        StringProperty get = (StringProperty) vars.get(key);
        if (get != null) {
            get.setValue(value);
        } else {
            vars.put(key, new SimpleStringProperty(value));
        }
        return true;
    }

    /**
     * Sets a double value for the given key.
     *
     * @param key   Key to store value at.
     * @param value Value to store.
     */
    public void setDouble(Key key, double value) {
        DoubleProperty get = (DoubleProperty) vars.get(key);
        if (get != null) {
            get.setValue(value);
        } else {
            vars.put(key, new SimpleDoubleProperty(value));
        }
    }

    /**
     * Sets an int value for the given key.
     *
     * @param key   Key to store value at.
     * @param value Value to store.
     */
    public void setInt(Key key, int value) {
        IntegerProperty get = (IntegerProperty) vars.get(key);
        if (get != null) {
            get.setValue(value);
        } else {
            vars.put(key, new SimpleIntegerProperty(value));
        }
    }

    /**
     * Gets the backing property that backs the setting for a key.
     *
     * @param key Key.
     * @return The property associated with that key. Null if no default value exists AND no value was set to this key.
     */
    public Property getProperty(Key key) {
        return vars.get(key);
    }

    /**
     * Gets a boolean setting for a key.
     *
     * @param key Key.
     * @return Value.
     */
    public boolean getBoolean(Key key) {
        return ((BooleanProperty) vars.get(key)).get();
    }

    /**
     * Gets a String setting for a key.
     *
     * @param key Key.
     * @return Value.
     */
    public String getString(Key key) {
        return ((StringProperty) vars.get(key)).get();
    }

    /**
     * Gets a double setting for a key.
     *
     * @param key Key.
     * @return Value.
     */
    public double getDouble(Key key) {
        return ((DoubleProperty) vars.get(key)).get();
    }

    /**
     * Gets an int setting for a key.
     *
     * @param key Key.
     * @return Value.
     */
    public int getInt(Key key) {
        return ((IntegerProperty) vars.get(key)).get();
    }

}
