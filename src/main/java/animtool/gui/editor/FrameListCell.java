package animtool.gui.editor;


import animtool.animation.Frame;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class FrameListCell extends ListCell<Frame> {

    private static final String DEFAULT_STYLE_CLASS = "frame-list-cell";

    private final ImageView imageView = new ImageView();
    private final Label indexLabel = new Label();


    FrameListCell() {
        getStyleClass().addAll(DEFAULT_STYLE_CLASS);

        setGraphic(new BorderPane(imageView, new BorderPane(null, null, null, null, indexLabel), null, null, null));
    }

    @Override
    protected void updateItem(Frame item, boolean empty) {
        super.updateItem(item, empty);

        imageView.setImage(null);
        indexLabel.setText(null);
        if (item != null) {
            imageView.setImage(item.getThumbnail());
            indexLabel.setText(getIndex() + "");
        }
    }

}
