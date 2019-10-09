package animtool.animation;


import animtool.gui.Main;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.File;

/**
 * A single frame of an animation loaded from file.
 */
public class Frame implements Comparable<Frame> {

    public static final int THUMBNAIL_SIZE = 100;

    private final File file;
    private final DefaultDelayCallback delayCallback;

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();
    private final IntegerProperty delay = new SimpleIntegerProperty(-1);


    public Frame(File file, DefaultDelayCallback delayCallback) {
        this.file = file;
        this.delayCallback = delayCallback;
    }

    /**
     * Gets this frame's image. Loads the image if it is not already loaded in.
     *
     * @return This frame's image.
     */
    public synchronized Image getImage() {
        if (image.get() == null) {
            loadImage();
        }

        return image.get();
    }

    public synchronized Image loadImage() {
        Image img = new Image(file.toURI().toString(), true);
        Main.log.info("Loading image: " + file.getAbsolutePath());

        image.set(img);

        return img;
    }

    /**
     * @return This frame's file.
     */
    public File getFile() {
        return file;
    }

    public synchronized Image getThumbnail() {
        Image thumb = thumbnail.get();
        if (thumb == null) {
            thumb = loadThumbnail();
        }

        return thumb;
    }

    private synchronized Image loadThumbnail() {
        Image thumb = new Image(file.toURI().toString(), THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true, true);
        thumbnail.set(thumb);
        return thumb;
    }

    public synchronized int getDefaultDelay() {
        return delayCallback.getDefaultDelay();
    }

    public synchronized int getDelay() {
        return delay.get();
    }

    public synchronized void setDelay(int delay) {
        this.delay.set(delay);
    }

    public synchronized int getComputedDelay() {
        int d = getDelay();
        if (d <= 0) d = getDefaultDelay();
        return d;
    }

    public IntegerProperty delayProperty() {
        return delay;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Frame && ((Frame) obj).getFile().equals(getFile());
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }

    @Override
    public int compareTo(Frame o) {
        if (equals(o)) {
            return 0;
        } else {
            return getFile().compareTo(o.getFile());
        }
    }

}
