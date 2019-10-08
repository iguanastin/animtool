package main;


import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.File;

/**
 * A single frame of an animation loaded from file.
 */
public class Frame implements Comparable<Frame> {

    private final File file;

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
    private final LongProperty delay = new SimpleLongProperty(-1);


    public Frame(File file) {
        this.file = file;
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
        System.out.println("Loading image: " + file.getAbsolutePath());

        image.set(img);

        return img;
    }

    /**
     * @return This frame's file.
     */
    public File getFile() {
        return file;
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

    public LongProperty delayProperty() {
        return delay;
    }

    public synchronized void setDelay(long delay) {
        this.delay.set(delay);
    }

    public synchronized long getDelay() {
        return delay.get();
    }

}
