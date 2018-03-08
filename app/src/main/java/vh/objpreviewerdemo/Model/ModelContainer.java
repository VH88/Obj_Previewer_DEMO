package vh.objpreviewerdemo.Model;

import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import vh.objpreviewerdemo.Utils.ModelHelper;

/**
 * Created by User on 2/13/2018.
 */

public class ModelContainer {

    final int mModelDataBufferIdx;
    final int mModelIndexBufferIdx;

    public static final int COORDS_PER_VERTEX = 3;
    public static final int POSITION_DATA_SIZE = 3;
    public static final int TEXTURE_DATA_SIZE = 2;
    public static final int NORMAL_DATA_SIZE = 3;
    public static final int TANGENT_DATA_SIZE = 3;
    public static final int BITANGENT_DATA_SIZE = 3;

    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_INTEGER = 4;

    public final int VERTEX_COUNT;

    public ModelContainer(final Context context, final int resourceID )
    {
        // Load Obj Model to data struct class then compute tangent basis
        final ModelDataStruct modelData = ModelHelper.loadModelIndexed(context, resourceID);
        final float[][] tangents = ModelHelper.computeTangentsBasisToArray(modelData.GetVertexData(), modelData.GetTextureData(), modelData.GetIndexData());

        VERTEX_COUNT = modelData.GetIndexData().length;

        // Data length = vertPos + texture + normal + tangent + bitangent;
        final int objDataLength = modelData.GetVertexData().length + modelData.GetTextureData().length
                                + modelData.GetNormalData().length + tangents[0].length + tangents[1].length;

        int objPositionOffset = 0;
        int objTextureOffset = 0;
        int objNormalOffset = 0;
        int objTangentOffset = 0;
        int objBitangentOffset = 0;

        // Generate a single interleaved Byte Buffer
        final FloatBuffer objBuffer = ByteBuffer.allocateDirect(objDataLength * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for(int i =0; i < modelData.GetVertexData().length / COORDS_PER_VERTEX; i++)
        {
            objBuffer.put(modelData.GetVertexData(), objPositionOffset, POSITION_DATA_SIZE);
            objPositionOffset += POSITION_DATA_SIZE;
            objBuffer.put(modelData.GetTextureData(), objTextureOffset, TEXTURE_DATA_SIZE);
            objTextureOffset += TEXTURE_DATA_SIZE;
            objBuffer.put(modelData.GetNormalData(), objNormalOffset, NORMAL_DATA_SIZE);
            objNormalOffset += NORMAL_DATA_SIZE;
            objBuffer.put(tangents[0], objTangentOffset, TANGENT_DATA_SIZE);
            objTangentOffset += TANGENT_DATA_SIZE;
            objBuffer.put(tangents[1], objBitangentOffset, BITANGENT_DATA_SIZE);
            objBitangentOffset += BITANGENT_DATA_SIZE;
        }
        objBuffer.position(0);

        final IntBuffer indxBuffer = ByteBuffer.allocateDirect(modelData.GetIndexData().length * BYTES_PER_INTEGER).order(ByteOrder.nativeOrder()).asIntBuffer();
        indxBuffer.put(modelData.GetIndexData()).position(0);

        /** Create VBO ------------------------------*/
        final int buffers[] = new int[2];
        GLES30.glGenBuffers(1, buffers, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, objBuffer.capacity() * BYTES_PER_FLOAT, objBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glGenBuffers(1, buffers, 1);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffers[1]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, indxBuffer.capacity() * BYTES_PER_INTEGER, indxBuffer, GLES30.GL_STATIC_DRAW);

        mModelDataBufferIdx = buffers[0];
        mModelIndexBufferIdx = buffers[1];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

        objBuffer.limit(0);
        indxBuffer.limit(0);

    }

    public void release()
    {
        final int[] buffersToDelete = new int[] {mModelDataBufferIdx, mModelIndexBufferIdx};
        GLES30.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }

    public final int getModelDataVBO(){return mModelDataBufferIdx;}
    public final int getModelIndexVBO(){return mModelIndexBufferIdx;}
}
