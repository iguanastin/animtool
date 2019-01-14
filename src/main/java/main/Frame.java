package main;


import javafx.scene.image.Image;

import java.io.File;

public class Frame implements Comparable<Frame> {

    private final File file;
    private Image image;


    public Frame(File file) {
        this.file = file;
    }

    public Image getImage(boolean reload) {
        if (reload || image == null) {
            image = new Image(file.toURI().toString(), true);
            System.out.println("Loading image: " + file.getAbsolutePath());
        }

        return image;
    }

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
