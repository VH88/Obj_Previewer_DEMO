package vh.objpreviewerdemo.Model;

/**
 * Created by User on 2/13/2018.
 */

public class ModelDataStruct {
    private final float[] vertexArray;
    private final float[] textureArray;
    private final float[] normalArray;
    private final int[] indexArray;

    public ModelDataStruct(float[] vertexData, float[] textureData, float[] normalData, int[] indexData)
    {
        vertexArray = vertexData;
        textureArray = textureData;
        normalArray = normalData;
        indexArray = indexData;
    }

    public float[] GetVertexData() { return vertexArray;}
    public float[] GetTextureData() {return textureArray;}
    public float[] GetNormalData() { return normalArray;}
    public int[] GetIndexData() {return indexArray;}

}
