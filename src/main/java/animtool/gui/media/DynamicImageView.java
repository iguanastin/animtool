package animtool.gui.media;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DynamicImageView extends ImageView {


    public DynamicImageView() {
        super();
    }

    public DynamicImageView(String path, boolean backgroundLoading) {
        this(new Image(path, backgroundLoading));
    }

    public DynamicImageView(String path, int width, int height, boolean preserveRatio, boolean smooth, boolean backgroundLoading) {
        this(new Image(path, width, height, preserveRatio, smooth, backgroundLoading));
    }

    public DynamicImageView(Image img) {
        super(img);
    }

    @Override
    public double minWidth(double height) {
        return 40;
    }

    @Override
    public double prefWidth(double height) {
        Image I = getImage();
        if (I == null) return minWidth(height);
        return I.getWidth();
    }

    @Override
    public double maxWidth(double height) {
        return 16384;
    }

    @Override
    public double minHeight(double width) {
        return 40;
    }

    @Override
    public double prefHeight(double width) {
        Image I = getImage();
        if (I == null) return minHeight(width);
        return I.getHeight();
    }

    @Override
    public double maxHeight(double width) {
        return 16384;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {
        if (getImage() == null) {
            setFitWidth(width);
            setFitHeight(height);
        } else {
            double scale = 1;
            if (scale * getImage().getWidth() > width) scale = width / getImage().getWidth();
            if (scale * getImage().getHeight() > height) scale = height / getImage().getHeight();

            setFitWidth(getImage().getWidth() * scale);
            setFitHeight(getImage().getHeight() * scale);
        }
    }

}