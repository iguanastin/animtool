module animtool {
    requires javafx.controls;
    requires javafx.swing;
    requires javafx.fxml;
    requires org.json;
    requires java.logging;
    requires java.prefs;

    opens animtool.gui to javafx.graphics;
    opens animtool.gui.projects to javafx.fxml;
    opens animtool.gui.editor to javafx.fxml;
    opens animtool.gui.help to javafx.fxml;
    opens animtool.gui.media to javafx.fxml;
}