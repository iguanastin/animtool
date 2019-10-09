package animtool.gui.editor;

public class GifExportConfig {

    public int delay = 1000 / 12;
    public boolean loop = true;
    public String disposal = "none";


    public GifExportConfig(int delay, boolean loop, String disposal) {
        this.delay = delay;
        this.loop = loop;
        this.disposal = disposal;
    }

}
