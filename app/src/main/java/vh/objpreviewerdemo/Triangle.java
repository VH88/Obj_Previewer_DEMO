package vh.objpreviewerdemo;

import android.content.Context;
import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

import vh.objpreviewerdemo.Utils.ShaderHelper;

/**
 * Created by User on 2/13/2018.
 */

public class Triangle {
    final int mProgram;
    private int mPositionHandle;
    private FloatBuffer vertexBuffer;
    private int vertexVBOIdx;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {   // in counterclockwise order:
            0.0f,  0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Triangle(Context context) {

        vertexBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        final int buffers[] = new int[1];
        GLES30.glGenBuffers(1, buffers, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
        vertexVBOIdx = buffers[0];

        int vertexShader = ShaderHelper.compileShader(GLES30.GL_VERTEX_SHADER, ShaderHelper.readTextFileFromRawResource(context, R.raw.triangle_vertex_shader));
        int fragmentShader = ShaderHelper.compileShader(GLES30.GL_FRAGMENT_SHADER, ShaderHelper.readTextFileFromRawResource(context, R.raw.triangle_fragment_shader));

        mProgram  = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader);

    }

    public void draw(float[] mvpMatrix)
    {
        GLES30.glUseProgram(mProgram);
        mPositionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition");
        int mvpMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix");

        // For array
        //GLES30.glEnableVertexAttribArray(mPositionHandle);
        //GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);
        //GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        // For VBO
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexVBOIdx);
        GLES30.glEnableVertexAttribArray(mPositionHandle);
        GLES30.glVertexAttribPointer(mPositionHandle, 3, GLES30.GL_FLOAT, false, 0, 0);

        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3);

        GLES30.glDisableVertexAttribArray(mPositionHandle);
    }
}
