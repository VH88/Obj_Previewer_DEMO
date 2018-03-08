package vh.objpreviewerdemo.Utils;

/**
 * Created by User on 2/15/2018.
 * Source:
 *          https://github.com/Ivelate/JavaHDR/blob/master/src/com/github/ivelate/JavaHDR/
 */

public abstract class HDRImage {
    protected float[] data;
    private int width;
    private int height;

    public HDRImage(int width, int height, float[] data)
    {
        this.data = data;
        this.width = width;
        this.height = height;
    }
    public abstract float getPixelValue(int x, int y, int c);

    public abstract void setPixelValue(int x, int y, int c, float val);

    public float[] getInternalData()    { return this.data;}

    public int getWidth() {return  this.width;}
    public int getHeight() {return  this.height;}

    public abstract int getChannels();

}
