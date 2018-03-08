package vh.objpreviewerdemo.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

import vh.objpreviewerdemo.OpenGLMain;

/**
 * Created by User on 2/13/2018.
 */

public class TextureHelper {

    /**
     *  Return a handle to texture buffer
     * @param context Current active context
     * @param resourceId resource ID of the 3D texture(jpg or png)
     * @return int texture handle
     */
    public static int loadTexture_2D(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error generating texture name.");
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;	// No pre-scaling

        // Read in the resource
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        // Bind to the texture in OpenGL
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        //GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA8, bitmap.getWidth(), bitmap.getHeight(), 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, bitmap.);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        bitmap.recycle();

        return textureHandle[0];
    }

    public static int loadTexture_2D_hdr(final Context context,  final int resourceId)
    {
        final int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        if (textureHandle[0] == 0)  {    throw new RuntimeException("Error generating texture name.");     }

        try {
            HDRImageRGB hdr = HDREncoder.readHDR( context, resourceId) ;
            FloatBuffer buffer = ByteBuffer.allocateDirect(hdr.getInternalData().length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            buffer.put(hdr.getInternalData()).position(0);
            //Log.e("dd", Float.toString(hdr.getInternalData()[0]) + ",  " +Float.toString(hdr.getInternalData()[1]));
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB32F, hdr.getWidth(), hdr.getHeight(), 0, GLES30.GL_RGB, GLES30.GL_FLOAT, buffer);
        }catch (IOException  e)
        {
            Log.e("loadTexture_2D_hdr", " Failed to load HDR");
        }

        return textureHandle[0];
    }

    public static int loadTexture_cubeMap(final Context context,boolean isGenMipMaps, final int tex_pos_x, final int tex_neg_x,
                                          final int tex_pos_y, final int tex_neg_y,
                                          final int tex_pos_z, final int tex_neg_z)
    {
        ByteBuffer fcBuffer;
        int[] cubeTexture = new int[1];
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glGenTextures(1, cubeTexture, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, cubeTexture[0]);

        // Positive X ------------------------------------
        Bitmap img = null;
        img = BitmapFactory.decodeResource(context.getResources(), tex_pos_x);
        fcBuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() *  4);
        img.copyPixelsToBuffer(fcBuffer);
        fcBuffer.position(0);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES30.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, fcBuffer);
        fcBuffer.clear();
        img.recycle();

        // Negative X ------------------------------------
        img = BitmapFactory.decodeResource(context.getResources(), tex_neg_x);
        fcBuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() *  4);
        img.copyPixelsToBuffer(fcBuffer);
        fcBuffer.position(0);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES30.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, fcBuffer);
        fcBuffer.clear();
        img.recycle();

        // Positive Y ------------------------------------
        img = BitmapFactory.decodeResource(context.getResources(), tex_pos_y);
        fcBuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() *  4);
        img.copyPixelsToBuffer(fcBuffer);
        fcBuffer.position(0);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES30.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, fcBuffer);
        fcBuffer.clear();
        img.recycle();

        // Negative Y ------------------------------------
        img = BitmapFactory.decodeResource(context.getResources(), tex_neg_y);
        fcBuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() *  4);
        img.copyPixelsToBuffer(fcBuffer);
        fcBuffer.position(0);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES30.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, fcBuffer);
        fcBuffer.clear();
        img.recycle();

        // Positive Z ------------------------------------
        img = BitmapFactory.decodeResource(context.getResources(), tex_pos_z);
        fcBuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() *  4);
        img.copyPixelsToBuffer(fcBuffer);
        fcBuffer.position(0);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES30.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, fcBuffer);
        fcBuffer.clear();
        img.recycle();

        // Negative Z ------------------------------------
        img = BitmapFactory.decodeResource(context.getResources(), tex_neg_z);
        fcBuffer = ByteBuffer.allocateDirect(img.getHeight() * img.getWidth() *  4);
        img.copyPixelsToBuffer(fcBuffer);
        fcBuffer.position(0);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES30.GL_RGBA,
                img.getWidth(), img.getHeight(), 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, fcBuffer);
        fcBuffer.clear();
        img.recycle();

        if(!isGenMipMaps) {
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        }else
        {
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_CUBE_MAP);
        }

        return cubeTexture[0];
    }


}
