package vh.objpreviewerdemo.Utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by User on 2/15/2018.
 *
 * Code is taken from
 * https://github.com/Ivelate/JavaHDR/blob/master/src/com/github/ivelate/JavaHDR/
 */

public class HDREncoder {
    private static final String IDENTIFIER="#?RGBE";
    private static final String DEFAULT_FORMAT="32-bit_rle_rgbe";

    public static HDRImageRGB readHDR(final Context context, final int resourceID) throws IOException
    {
        final InputStream inputStream = context.getResources().openRawResource(resourceID);

        //FileInputStream fi = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        DataInput di = new DataInputStream(bis);

        RGBE.Header header = RGBE.readHeader(di);

        int width = header.getWidth();
        int height = header.getHeight();

        byte[] bbuff = new byte[width * 4];
        float[] outImage=new float[width * height * 3];
        int floatOffset=0;
        for(int h=0;h<height;h++)
        {
            RGBE.readPixelsRawRLE(di, bbuff, 0, width, 1);

            //Convert RGBE into float
            floatOffset = scanlineToFloatRGB(bbuff,outImage,floatOffset);
        }

        bis.close();
        inputStream.close();
        return  new HDRImageRGB(width,height,outImage);
    }

    private static int scanlineToFloatRGB(byte[] scanline,float[] dest,int initialOffset)
    {
        int off=initialOffset;
        for(int i=0;i<scanline.length;i+=4)
        {
            RGBE.rgbe2float(dest, scanline, i,off);
            off+=3;
        }
        return off;
    }
}
