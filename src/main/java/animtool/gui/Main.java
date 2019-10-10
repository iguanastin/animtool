package animtool.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class Main extends Application {

    public static final String COMMON_CSS = "/fxml/common.css";
    public static final String DARK_CSS = "/fxml/dark.css";
    public static final String PROJECTS_FXML = "/fxml/projects.fxml";
    public static final String TITLE = "AnimTool";

    public static FilenameFilter imageFilter = (dir, name) -> {
        name = name.toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    };

    public static final Logger log = Logger.getGlobal();
    private static final String logFilePath = "animtool.log";


    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(PROJECTS_FXML));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(COMMON_CSS);
        primaryStage.setScene(scene);
        primaryStage.setTitle(TITLE);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        log.setLevel(Level.ALL); // Default log level

        // Set log level to severe only with arg
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--quiet")) {
                log.setLevel(Level.SEVERE);
            }
        }
        initLogger();

        // Log some simple system info
        if (Runtime.getRuntime().maxMemory() == Long.MAX_VALUE) {
            log.info("Max Memory: No limit");
        } else {
            log.info(String.format("Max Memory: %.2fGB", Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0));
        }
        log.info(String.format("Processors: %d", Runtime.getRuntime().availableProcessors()));
        log.info(String.format("Operating System: %s", System.getProperty("os.name")));
        log.info(String.format("OS Version: %s", System.getProperty("os.version")));
        log.info(String.format("OS Architecture: %s", System.getProperty("os.arch")));
        log.info(String.format("Java version: %s", System.getProperty("java.version")));
        log.info(String.format("Java runtime version: %s", System.getProperty("java.runtime.version")));
        log.info(String.format("JavaFX version: %s", System.getProperty("javafx.version")));
        log.info(String.format("JavaFX runtime version: %s", System.getProperty("javafx.runtime.version")));

        // Launch application
        log.info("Starting JFX Application...");
        launch(args);
    }

    private static void initLogger() {
        // Clear log file
        if (!new File(logFilePath).delete())
            Main.log.warning(String.format("Could not clear log file: %s", logFilePath));
        try {
            if (!new File(logFilePath).createNewFile())
                Main.log.warning(String.format("Could not create new log file: %s", logFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Init logger handler
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Main.log.log(Level.SEVERE, "Uncaught exception in thread: " + t, e));
        log.setUseParentHandlers(false);
        log.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                StringBuilder str = new StringBuilder(new Date(record.getMillis()).toString());
                str.append(" [").append(record.getLevel()).append("]: ").append(record.getMessage());

                // Print to sout/serr
                PrintStream s = System.out;
                if (record.getLevel() == Level.SEVERE) s = System.err;
                s.println(str.toString());
                if (record.getThrown() != null) record.getThrown().printStackTrace();

                // Print to file
                if (record.getThrown() != null) {
                    str.append("\n").append(record.getThrown().toString());
                    for (StackTraceElement element : record.getThrown().getStackTrace()) {
                        str.append("\n    at ").append(element.toString());
                    }
                }
                str.append("\n");
                try {
                    Files.write(Paths.get(logFilePath), str.toString().getBytes(), StandardOpenOption.APPEND);
                } catch (IOException e) {
                    System.err.println(String.format("Failed to write log to file: %s", logFilePath));
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
    }

}
