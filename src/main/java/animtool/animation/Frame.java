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

package animtool.animation;


import animtool.gui.Main;
import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.File;

/**
 * A single frame of an animation loaded from file.
 */
public class Frame implements Comparable<Frame> {

    public static final int THUMBNAIL_SIZE = 100;

    private final File file;
    private final DoubleProperty defaultDelay;

    private final ObjectProperty<Image> image = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> thumbnail = new SimpleObjectProperty<>();
    private final DoubleProperty delay = new SimpleDoubleProperty(-1);


    public Frame(File file, DoubleProperty defaultDelay) {
        this.file = file;
        this.defaultDelay = defaultDelay;
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

    public synchronized double getDefaultDelay() {
        return defaultDelay.get();
    }

    public DoubleProperty defaultDelayProperty() {
        return defaultDelay;
    }

    public synchronized double getDelay() {
        return delay.get();
    }

    public synchronized void setDelay(double delay) {
        this.delay.set(delay);
    }

    public synchronized double getComputedDelay() {
        double d = getDelay();
        if (d <= 0) d = getDefaultDelay();
        return d;
    }

    public DoubleProperty delayProperty() {
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
