package main;


import javafx.scene.image.Image;

import java.io.File;

/**
 * A single frame of an animation loaded from file.
 */
public class Frame implements Comparable<Frame> {

    private final File file;
    private Image image;


    public Frame(File file) {
        this.file = file;
    }

    /**
     * Gets this frame's image. Loads the image if it is not already loaded in.
     *
     * @param reload Force a reload from file.
     * @return This frame's image.
     */
    public Image getImage(boolean reload) {
        if (reload || image == null) {
            image = new Image(file.toURI().toString(), true);
            System.out.println("Loading image: " + file.getAbsolutePath());
        }

        return image;
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

}
