package vh.objpreviewerdemo;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

import vh.objpreviewerdemo.Utils.ShaderHelper;
import vh.objpreviewerdemo.Utils.TextureHelper;

/**
 * Created by User on 2/13/2018.
 */

public class Quad {
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordsBuffer;
    private int vertexVBOIdx;
    private int textureVBOIdx;


    static float quadPos[] = {   // in counterclockwise order:
            -1.0f, 1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f,  -1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f,  1.0f, 0.0f,
    };

    static float quadTexCoords[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };



    public Quad(Context context) {

        vertexBuffer = ByteBuffer.allocateDirect(quadPos.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(quadPos);
        vertexBuffer.position(0);

        texCoordsBuffer = ByteBuffer.allocateDirect(quadTexCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordsBuffer.put(quadTexCoords).position(0);

        final int buffers[] = new int[2];
        GLES30.glGenBuffers(1, buffers, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
        vertexVBOIdx = buffers[0];

        GLES30.glGenBuffers(1, buffers, 1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, texCoordsBuffer.capacity() * 4, texCoordsBuffer, GLES30.GL_STATIC_DRAW);
        textureVBOIdx = buffers[1];

        vertexBuffer.limit(0);
        texCoordsBuffer.limit(0);

    }

    public void drawForBRDFcalc()
    {
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexVBOIdx);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, textureVBOIdx);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);
    }


    public void draw(final int program, final int texture)
    {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glClearColor(1.0f,1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT );

        int map = GLES30.glGetUniformLocation(program, "map");

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture);
        GLES30.glUniform1i(map, 0);


        // For VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexVBOIdx);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, textureVBOIdx);
        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, 0);

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        GLES30.glDisableVertexAttribArray(0);
        GLES30.glDisableVertexAttribArray(1);

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    }


}
