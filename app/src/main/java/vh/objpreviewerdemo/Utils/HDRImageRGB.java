package vh.objpreviewerdemo.Utils;

/**
 * Created by User on 2/15/2018.
 * Source:
 *          https://github.com/Ivelate/JavaHDR/blob/master/src/com/github/ivelate/JavaHDR/
 */

public class HDRImageRGB extends HDRImage {

    public HDRImageRGB(int width,int height)
    {
        super(width,height,new float[width*height*3]);
    }
    public HDRImageRGB(int width,int height,float[] data)
    {
        super(width,height,data);
    }
    @Override
    public float getPixelValue(int x, int y, int c) {
        return data[(y*this.getWidth()+x)*3+c];
    }
    @Override
    public void setPixelValue(int x, int y, int c, float val) {
        data[(y*this.getWidth()+x)*3+c]=val;
    }
    @Override
    public int getChannels() {
        return 3;
    }
}
