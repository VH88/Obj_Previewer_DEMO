package vh.objpreviewerdemo.Utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import vh.objpreviewerdemo.Model.ModelDataStruct;

/**
 * Created by User on 2/13/2018.
 */

public class ModelHelper {

    public  static float[][] loadModelNonIndexed(final Context context, final int resourceID){
        final InputStream inputStream = context.getResources().openRawResource(resourceID);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);


        String nextLine;

        int vertexCount = 0;
        int textureCount = 0;
        int normalCount = 0;
        int faceCount = 0;



        List<String> modelSource = new ArrayList<>();
        try{
            while ((nextLine = bufferedReader.readLine()) != null){
                modelSource.add(nextLine) ;
            }
        }
        catch (IOException e){       return null;     }


        for (int i = 0; i < modelSource.size(); i++){
            if(modelSource.get(i).length() != 0){
                if(modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == ' '){
                    vertexCount++;
                }
                else if(modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 't'){
                    textureCount++;
                }
                else if(modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 'n'){
                    normalCount++;
                }
                else if(modelSource.get(i).charAt(0) == 'f' ){
                    faceCount++;
                }
            }

        }

        // -------------- STORE DATA IN ARRAYS ------------------------------------------------------
        float[] vertexArray = new float[vertexCount*3];
        float[] textureArray = new float[textureCount*2];
        float[] normalArray = new float[normalCount*3];

        int vCount = 0;
        int tCount = 0;
        int nCount = 0;

        for (int i = 0; i < modelSource.size() ; i++){
            if(modelSource.get(i).length() != 0){
                if(modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == ' '){
                    String _vertexArray[] = modelSource.get(i).split(" ");

                    // because there are two spaces after 'v' vertArray[0] = "v"; [1] = ""
                    vertexArray[vCount * 3] = Float.valueOf(_vertexArray[2]);
                    vertexArray[vCount * 3 + 1] = Float.valueOf(_vertexArray[3]);
                    vertexArray[vCount * 3 + 2] = Float.valueOf(_vertexArray[4]);

                    vCount++;
                }
                else if(modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 't'){
                    String _textureArray[] = modelSource.get(i).split(" ");

                    textureArray[tCount * 2] = Float.valueOf(_textureArray[1]);
                    textureArray[tCount * 2 + 1] = Float.valueOf(_textureArray[2]);

                    tCount++;
                }
                else if(modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 'n'){
                    String _normalArray[] = modelSource.get(i).split(" ");

                    // because there are two spaces after 'v' vertArray[0] = "v"; [1] = ""
                    normalArray[nCount * 3] = Float.valueOf(_normalArray[1]);
                    normalArray[nCount * 3 + 1] = Float.valueOf(_normalArray[2]);
                    normalArray[nCount * 3 + 2] = Float.valueOf(_normalArray[3]);

                    nCount++;
                }
            }
        }


        // ------------------------ CREATE PER VERTEX ARRAYS ------------------------
        float[] modelVertexData = new float[faceCount * 3 * 3]; // face has 3 verts 3 coords each(x,y,z)
        float[] modelTextureData = new float[faceCount * 2 * 3];// face has 3 verts 2 coords each(x, y)
        float[] modelNormalData = new float[faceCount * 3 * 3];

        // f:    vertexID / textureID / normalID
        int fCount = 0;
        for (int i = 0; i < modelSource.size() ; i++){
            if(modelSource.get(i).length() != 0) {
                if (modelSource.get(i).charAt(0) == 'f') {
                    int nextSeq = 0;

                    // index starts at '1' because 0 is 'f'
                    String faceArray[] = modelSource.get(i).split(" ");
                    String vertex1[] = faceArray[1].split("/");
                    String vertex2[] = faceArray[2].split("/");
                    String vertex3[] = faceArray[3].split("/");
                    //Log.e("Vertex Count", "Line Count: " + faceArray[1]);



                    // FIRST VERTEX OF THE FACE ------------------------------------------
                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex1[0]) - 1) * 3];
                    modelTextureData[fCount  * 6 + nextSeq] = textureArray[(Integer.valueOf(vertex1[1]) - 1) * 2];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex1[2]) - 1) * 3];
                    nextSeq++;

                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex1[0]) - 1) * 3 + 1];
                    modelTextureData[fCount  * 6 + nextSeq] = textureArray[(Integer.valueOf(vertex1[1]) - 1) * 2 + 1];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex1[2]) - 1) * 3 + 1];
                    nextSeq++;

                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex1[0]) - 1) * 3 + 2];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex1[2]) - 1) * 3 + 2];
                    nextSeq++;

                    // SECOND VERTEX OF THE FACE -----------------------------------------
                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex2[0]) - 1) * 3];
                    modelTextureData[fCount  * 6 + nextSeq - 1] = textureArray[(Integer.valueOf(vertex2[1]) - 1) * 2];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex2[2]) - 1) * 3];
                    nextSeq++;

                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex2[0]) - 1) * 3 + 1];
                    modelTextureData[fCount  * 6 + nextSeq - 1] = textureArray[(Integer.valueOf(vertex2[1]) - 1) * 2 + 1];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex2[2]) - 1) * 3 + 1];
                    nextSeq++;

                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex2[0]) - 1) * 3 + 2];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex2[2]) - 1) * 3 + 2];
                    nextSeq++;

                    // THIRD VERTEX OF THE FACE -----------------------------------------
                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex3[0]) - 1) * 3];
                    modelTextureData[fCount  * 6 + nextSeq - 2] = textureArray[(Integer.valueOf(vertex3[1]) - 1) * 2];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex3[2]) - 1) * 3];
                    nextSeq++;

                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex3[0]) - 1) * 3 + 1];
                    modelTextureData[fCount  * 6 + nextSeq - 2] = textureArray[(Integer.valueOf(vertex3[1]) - 1) * 2 + 1];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex3[2]) - 1) * 3+1];
                    nextSeq++;

                    modelVertexData[fCount  * 9 + nextSeq] = vertexArray[(Integer.valueOf(vertex3[0]) - 1) * 3 + 2];
                    modelNormalData[fCount  * 9 + nextSeq] = normalArray[(Integer.valueOf(vertex3[2]) - 1) * 3+2];


                    fCount++;
                }
            }
        }

        return new float[][]{ modelVertexData, modelTextureData, modelNormalData};
    }

    /**
     * Load *.obj file and convert to Indexed arrays
     * @param context
     * @param resourceID
     * @return ModelDataStruct
     */
    public  static ModelDataStruct loadModelIndexed(final Context context, final int resourceID) {
        final InputStream inputStream = context.getResources().openRawResource(resourceID);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;

        int vertexCount = 0;
        int textureCount = 0;
        int normalCount = 0;
        int faceCount = 0;

        List<String> modelSource = new ArrayList<>();
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                modelSource.add(nextLine);
            }
        } catch (IOException e) {
            return null;
        }

        for (int i = 0; i < modelSource.size(); i++) {
            if (modelSource.get(i).length() != 0) {
                if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == ' ') {
                    vertexCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 't') {
                    textureCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 'n') {
                    normalCount++;
                } else if (modelSource.get(i).charAt(0) == 'f') {
                    faceCount++;
                }
            }
        }

        // -------------- STORE DATA IN ARRAYS ------------------------------------------------------
        float[] vertexArray = new float[vertexCount * 3];
        float[] textureArray = new float[textureCount * 2];
        float[] normalArray = new float[normalCount * 3];
        int[][][] faceIndexArray = new int[faceCount][3][3]; // float[faceCount][vert]{ position, texture, normal}

        int vCount = 0;
        int tCount = 0;
        int nCount = 0;

        // Load Vertices, Texture Coords and Normals to arrays ---------------------------------------
        for (int i = 0; i < modelSource.size(); i++) {
            if (modelSource.get(i).length() != 0) {
                if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == ' ') {
                    String _vertexArray[] = modelSource.get(i).split(" ");

                    // because there are two spaces after 'v' vertArray[0] = "v"; [1] = ""
                    vertexArray[vCount * 3] = Float.valueOf(_vertexArray[1]);
                    vertexArray[vCount * 3 + 1] = Float.valueOf(_vertexArray[2]);
                    vertexArray[vCount * 3 + 2] = Float.valueOf(_vertexArray[3]);

                    vCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 't') {
                    String _textureArray[] = modelSource.get(i).split(" ");

                    textureArray[tCount * 2] = Float.valueOf(_textureArray[1]);
                    textureArray[tCount * 2 + 1] = Float.valueOf(_textureArray[2]);

                    tCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 'n') {
                    String _normalArray[] = modelSource.get(i).split(" ");

                    // because there are two spaces after 'v' vertArray[0] = "v"; [1] = ""
                    normalArray[nCount * 3] = Float.valueOf(_normalArray[1]);
                    normalArray[nCount * 3 + 1] = Float.valueOf(_normalArray[2]);
                    normalArray[nCount * 3 + 2] = Float.valueOf(_normalArray[3]);

                    nCount++;
                }
            }
        }

        // Load Face data to arrays -----------------------------------------------------------
        int fCount = 0;
        for (int i = 0; i < modelSource.size() ; i++) {
            if (modelSource.get(i).length() != 0) {
                if (modelSource.get(i).charAt(0) == 'f') {

                    // index starts at '1' because 0 is 'f'
                    String faceData[] = modelSource.get(i).split(" ");
                    String vertex1[] = faceData[1].split("/");
                    String vertex2[] = faceData[2].split("/");
                    String vertex3[] = faceData[3].split("/");

                    faceIndexArray[fCount][0][0] = Integer.valueOf(vertex1[0]) - 1;
                    faceIndexArray[fCount][0][1] = Integer.valueOf(vertex1[1]) - 1;
                    faceIndexArray[fCount][0][2] = Integer.valueOf(vertex1[2]) - 1;

                    faceIndexArray[fCount][1][0] = Integer.valueOf(vertex2[0]) - 1;
                    faceIndexArray[fCount][1][1] = Integer.valueOf(vertex2[1]) - 1;
                    faceIndexArray[fCount][1][2] = Integer.valueOf(vertex2[2]) - 1;

                    faceIndexArray[fCount][2][0] = Integer.valueOf(vertex3[0]) - 1;
                    faceIndexArray[fCount][2][1] = Integer.valueOf(vertex3[1]) - 1;
                    faceIndexArray[fCount][2][2] = Integer.valueOf(vertex3[2]) - 1;

                    fCount++;
                }
            }
        }



        // Convert Multi Index Obj data to a single index data ---------------------------------
        List<Integer> newIndexList = new ArrayList<>();

        int[] newVertIndex = new int[faceCount*3];
        int[] newTCoordIndex = new int[faceCount*3];
        int[] newNormalIndex = new int[faceCount*3];

        int count = 0;
        for (int f = 0; f < faceCount; f++)
        {   // For every vert in the face
            for(int v = 0; v < 3; v++)
            {
                int result = isIndexADuplicate(faceIndexArray[f][v][0], faceIndexArray[f][v][1],faceIndexArray[f][v][2],
                        newVertIndex, newTCoordIndex, newNormalIndex, count );
                //result = -1;
                if(result == -1)
                {
                    // If no duplicates add indices to a new array
                    newVertIndex[count] = faceIndexArray[f][v][0];
                    newTCoordIndex[count] = faceIndexArray[f][v][1];
                    newNormalIndex[count] = faceIndexArray[f][v][2];

                    newIndexList.add(count);

                    count++;
                }
                else
                {
                    newIndexList.add(result);
                }
            }
        }


        // Convert index arrays to float arrays
        float[] newVertexArray = new float[count * 3];
        float[] newTCoordArray = new float[count * 2];
        float[] newNormalArray = new float[count * 3];

        int[] newIndexArray = new int[newIndexList.size()];

        int VNsequence = 0;
        int Tsequence = 0;
        for (int i = 0; i < count; i++)
        {
            newVertexArray[VNsequence] = vertexArray[newVertIndex[i] * 3 + 0];
            newTCoordArray[Tsequence] = textureArray[newTCoordIndex[i] * 2 + 0];
            newNormalArray[VNsequence] = normalArray[newNormalIndex[i] * 3 + 0];
            VNsequence++; Tsequence++;

            newVertexArray[VNsequence] = vertexArray[newVertIndex[i] * 3 + 1];
            newTCoordArray[Tsequence] = textureArray[newTCoordIndex[i] * 2 + 1];
            newNormalArray[VNsequence] = normalArray[newNormalIndex[i] * 3 + 1];
            VNsequence++; Tsequence++;

            newVertexArray[VNsequence] = vertexArray[newVertIndex[i] * 3 + 2];
            newNormalArray[VNsequence] = normalArray[newNormalIndex[i] * 3 + 2];
            VNsequence++;
        }

        for(int i = 0; i < newIndexArray.length; i++)
        {
            newIndexArray[i] = newIndexList.get(i);
        }


        //Log.e("results", "index Count: " + Integer.toString(newIndexArray.length) + " vertex count: " + Integer.toString(newVertexArray.length/3));
        return new ModelDataStruct( newVertexArray, newTCoordArray, newNormalArray, newIndexArray);
    }

    public  static ModelDataStruct loadModelIndexedNoMap(final Context context, final int resourceID) {
        final InputStream inputStream = context.getResources().openRawResource(resourceID);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;

        int vertexCount = 0;
        int textureCount = 0;
        int normalCount = 0;
        int faceCount = 0;

        List<String> modelSource = new ArrayList<>();
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                modelSource.add(nextLine);
            }
        } catch (IOException e) {
            return null;
        }

        for (int i = 0; i < modelSource.size(); i++) {
            if (modelSource.get(i).length() != 0) {
                if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == ' ') {
                    vertexCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 't') {
                    textureCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 'n') {
                    normalCount++;
                } else if (modelSource.get(i).charAt(0) == 'f') {
                    faceCount++;
                }
            }
        }

        // -------------- STORE DATA IN ARRAYS ------------------------------------------------------
        float[] vertexArray = new float[vertexCount * 3];
        float[] textureArray = new float[textureCount * 2];
        float[] normalArray = new float[normalCount * 3];
        int[][][] faceIndexArray = new int[faceCount][3][3]; // float[faceCount][vert]{ position, texture, normal}

        int vCount = 0;
        int tCount = 0;
        int nCount = 0;

        // Load Vertices, Texture Coords and Normals to arrays ---------------------------------------
        for (int i = 0; i < modelSource.size(); i++) {
            if (modelSource.get(i).length() != 0) {
                if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == ' ') {
                    String _vertexArray[] = modelSource.get(i).split(" ");

                    // because there are two spaces after 'v' vertArray[0] = "v"; [1] = ""
                    vertexArray[vCount * 3] = Float.valueOf(_vertexArray[2]);
                    vertexArray[vCount * 3 + 1] = Float.valueOf(_vertexArray[3]);
                    vertexArray[vCount * 3 + 2] = Float.valueOf(_vertexArray[4]);

                    vCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 't') {
                    String _textureArray[] = modelSource.get(i).split(" ");

                    textureArray[tCount * 2] = Float.valueOf(_textureArray[1]);
                    textureArray[tCount * 2 + 1] = Float.valueOf(_textureArray[2]);

                    tCount++;
                } else if (modelSource.get(i).charAt(0) == 'v' && modelSource.get(i).charAt(1) == 'n') {
                    String _normalArray[] = modelSource.get(i).split(" ");

                    // because there are two spaces after 'v' vertArray[0] = "v"; [1] = ""
                    normalArray[nCount * 3] = Float.valueOf(_normalArray[1]);
                    normalArray[nCount * 3 + 1] = Float.valueOf(_normalArray[2]);
                    normalArray[nCount * 3 + 2] = Float.valueOf(_normalArray[3]);

                    nCount++;
                }
            }
        }

        // Load Face data to arrays -----------------------------------------------------------
        int fCount = 0;
        for (int i = 0; i < modelSource.size() ; i++) {
            if (modelSource.get(i).length() != 0) {
                if (modelSource.get(i).charAt(0) == 'f') {

                    // index starts at '1' because 0 is 'f'
                    String faceData[] = modelSource.get(i).split(" ");
                    String vertex1[] = faceData[1].split("/");
                    String vertex2[] = faceData[2].split("/");
                    String vertex3[] = faceData[3].split("/");

                    faceIndexArray[fCount][0][0] = Integer.valueOf(vertex1[0]) - 1;
                    faceIndexArray[fCount][0][1] = Integer.valueOf(vertex1[1]) - 1;
                    faceIndexArray[fCount][0][2] = Integer.valueOf(vertex1[2]) - 1;

                    faceIndexArray[fCount][1][0] = Integer.valueOf(vertex2[0]) - 1;
                    faceIndexArray[fCount][1][1] = Integer.valueOf(vertex2[1]) - 1;
                    faceIndexArray[fCount][1][2] = Integer.valueOf(vertex2[2]) - 1;

                    faceIndexArray[fCount][2][0] = Integer.valueOf(vertex3[0]) - 1;
                    faceIndexArray[fCount][2][1] = Integer.valueOf(vertex3[1]) - 1;
                    faceIndexArray[fCount][2][2] = Integer.valueOf(vertex3[2]) - 1;

                    fCount++;
                }
            }
        }



        // Convert Multi Index Obj data to a single index data ---------------------------------
        List<Integer> newIndexList = new ArrayList<>();

        int[] newVertIndex = new int[faceCount*3];
        int[] newTCoordIndex = new int[faceCount*3];
        int[] newNormalIndex = new int[faceCount*3];

        int count = 0;
        for (int f = 0; f < faceCount; f++)
        {   // For every vert in the face
            for(int v = 0; v < 3; v++)
            {
                int result = isIndexADuplicate(faceIndexArray[f][v][0], faceIndexArray[f][v][1],faceIndexArray[f][v][2],
                        newVertIndex, newTCoordIndex, newNormalIndex, count );
                //result = -1;
                if(result == -1)
                {
                    // If no duplicates add indices to a new array
                    newVertIndex[count] = faceIndexArray[f][v][0];
                    newTCoordIndex[count] = faceIndexArray[f][v][1];
                    newNormalIndex[count] = faceIndexArray[f][v][2];

                    newIndexList.add(count);

                    count++;
                }
                else
                {
                    newIndexList.add(result);
                }
            }
        }


        // Convert index arrays to float arrays
        float[] newVertexArray = new float[count * 3];
        float[] newTCoordArray = new float[count * 2];
        float[] newNormalArray = new float[count * 3];

        int[] newIndexArray = new int[newIndexList.size()];

        int VNsequence = 0;
        int Tsequence = 0;
        for (int i = 0; i < count; i++)
        {
            newVertexArray[VNsequence] = vertexArray[newVertIndex[i] * 3 + 0];
            newTCoordArray[Tsequence] = textureArray[newTCoordIndex[i] * 2 + 0];
            newNormalArray[VNsequence] = normalArray[newNormalIndex[i] * 3 + 0];
            VNsequence++; Tsequence++;

            newVertexArray[VNsequence] = vertexArray[newVertIndex[i] * 3 + 1];
            newTCoordArray[Tsequence] = textureArray[newTCoordIndex[i] * 2 + 1];
            newNormalArray[VNsequence] = normalArray[newNormalIndex[i] * 3 + 1];
            VNsequence++; Tsequence++;

            newVertexArray[VNsequence] = vertexArray[newVertIndex[i] * 3 + 2];
            newNormalArray[VNsequence] = normalArray[newNormalIndex[i] * 3 + 2];
            VNsequence++;
        }

        for(int i = 0; i < newIndexArray.length; i++)
        {
            newIndexArray[i] = newIndexList.get(i);
        }


        //Log.e("results", "index Count: " + Integer.toString(newIndexArray.length) + " vertex count: " + Integer.toString(newVertexArray.length/3));
        return new ModelDataStruct( newVertexArray, newTCoordArray, newNormalArray, newIndexArray);
    }

    /**
     * Used by loadModelIndexed() function. Determines if similar vertex is already in the array
     * @param VertIndex index of the vertex to make the face
     * @param TCoordIndex index of a texture to make the face
     * @param NormalIndex index of a normal to make the face
     * @param vertIndxArray current vertex index where we store new data
     * @param TCoordIndxArray current texture index where we store new data
     * @param NormalIndxArray current Normal index where we store new data
     * @param count number of indices parsed
     * @return -1 if no duplicates, else returns matching index
     */
    private static int isIndexADuplicate(int VertIndex, int TCoordIndex, int NormalIndex, int[] vertIndxArray, int[] TCoordIndxArray, int[] NormalIndxArray, int count)
    {
        int result = -1;

        for(int i = 0; i < count ; i++  )
        {
            if(VertIndex == vertIndxArray[i] && TCoordIndex == TCoordIndxArray[i] && NormalIndex == NormalIndxArray[i])
            {
                result = i;
                break;
            }

        }

        return result;
    }

    /**
     * Computes Tangents and Bitangents from given data
     * @param vertexData vertex positions
     * @param uvs texture coordinates
     * @normal - reserved for Tangents orthogonalization. Seems to work fine without it
     * @param indices face indices
     * @return
     */
    public static float[][] computeTangentsBasisToArray(float[] vertexData, float[] uvs, int[] indices)
    {
        final int dataLength = indices.length;
        float[] Tangents[] = new float[2][vertexData.length];

        // Initialize tangent arrays
        for (int i = 0; i < Tangents[0].length; i++)
        {
            Tangents[0][i] = 0.0f;
            Tangents[1][i] = 0.0f;
        }

        // Calculate tangent basis per face
        // Per Face (3 floats per vert * 3 verts per face)
        for (int i = 0; i < indices.length; i+=3)
        {
            // Vertices of a face
            float[] v0 = new float[]{vertexData[indices[i + 0] * 3+0], vertexData[indices[i + 0] * 3+1], vertexData[indices[i + 0] * 3+2]};
            float[] v1 = new float[]{vertexData[indices[i + 1] * 3+0], vertexData[indices[i + 1] * 3+1], vertexData[indices[i + 1] * 3+2]};
            float[] v2 = new float[]{vertexData[indices[i + 2] * 3+0], vertexData[indices[i + 2] * 3+1], vertexData[indices[i + 2] * 3+2]};

            // UVs fo a face
            float[] uv0 = new float[]{uvs[indices[i + 0] * 2+0], uvs[indices[i + 0] * 2+1]};
            float[] uv1 = new float[]{uvs[indices[i + 1] * 2+0], uvs[indices[i + 1] * 2+1]};
            float[] uv2 = new float[]{uvs[indices[i + 2] * 2+0], uvs[indices[i + 2] * 2+1]};

            // compute Position delta and UV delta
            float[] deltaPos1 = new float[]{v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
            float[] deltaPos2 = new float[]{v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};

            float[] deltaUV1 = new float[]{uv1[0] - uv0[0], uv1[1] - uv0[1]};
            float[] deltaUV2 = new float[]{uv2[0] - uv0[0], uv2[1] - uv0[1]};

            // Compute Tangent and Bitangent
            // 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x)
            float r = 1.0f / (deltaUV1[0] * deltaUV2[1] - deltaUV1[1] * deltaUV2[0]);
            // (deltaPos1 * deltaUV2.y - deltaPos2 * deltaUV1.y) * r
            float[] tangent = new float[]{ (deltaPos1[0] * deltaUV2[1] - deltaPos2[0] * deltaUV1[1]) * r,
                    (deltaPos1[1] * deltaUV2[1] - deltaPos2[1] * deltaUV1[1]) * r,
                    (deltaPos1[2] * deltaUV2[1] - deltaPos2[2] * deltaUV1[1]) * r};
            // (deltaPos2 * deltaUV1.x - deltaPos1 * deltaUV2.x) * r
            float[] bitangent = new float[]{(deltaPos2[0] * deltaUV1[0] - deltaPos1[0] * deltaUV2[0]) * r,
                    (deltaPos2[1] * deltaUV1[0] - deltaPos1[1] * deltaUV2[0]) * r,
                    (deltaPos2[2] * deltaUV1[0] - deltaPos1[2] * deltaUV2[0]) * r};

            // Put Calculations in an output array

            for (int m=0; m < 3; m++)
            {   //face verts
                for (int n = 0; n < 3; n++)
                {   // XYZ
                    Tangents[0][(indices[i + m]) * 3 + n] += tangent[n];
                    Tangents[1][(indices[i + m]) * 3 + n] += bitangent[n];
                }
            }
        }

        // Normalize Tangent Basis
        for(int i = 0; i < Tangents[0].length;i+=3)
        {
            // compute magnitude
            float magnitude0 = (float)Math.sqrt( Tangents[0][i + 0] * Tangents[0][i + 0] +
                    Tangents[0][i + 1] * Tangents[0][i + 1] +
                    Tangents[0][i + 2] * Tangents[0][i + 2]);
            float magnitude1 = (float)Math.sqrt( Tangents[1][i + 0] * Tangents[1][i + 0] +
                    Tangents[1][i + 1] * Tangents[1][i + 1] +
                    Tangents[1][i + 2] * Tangents[1][i + 2]);
            // Normalize
            Tangents[0][i +0] = Tangents[0][i + 0] / magnitude0;
            Tangents[0][i +1] = Tangents[0][i + 1] / magnitude0;
            Tangents[0][i +2] = Tangents[0][i + 2] / magnitude0;

            Tangents[1][i +0] = Tangents[1][i + 0] / magnitude1;
            Tangents[1][i +1] = Tangents[1][i + 1] / magnitude1;
            Tangents[1][i +2] = Tangents[1][i + 2] / magnitude1;
        }
/*
        // Make tangent orthogonal to normal
        for(int i = 0; i < normal.length ; i+=3)
        {
            // new Tangent = normalize(t - n * dot(n, t) )
            float[] temp = vectorMultiply( normal[i + 0], normal[i + 1], normal[i + 0],
                                        dotProduct(normal[i + 0], normal[i + 1], normal[i + 0],
                                                    Tangents[0][i + 0], Tangents[0][i + 1], Tangents[0][i + 2]));
            float[] newTangent = vectorNormalize( vectorSubtract(Tangents[0][i + 0],Tangents[0][i + 1], Tangents[0][i + 2],
                                                                temp[0], temp[1], temp[2]));

            Tangents[0][i + 0] = newTangent[0];
            Tangents[0][i + 1] = newTangent[1];
            Tangents[0][i + 2] = newTangent[2];
        }*/


        return Tangents;
    }
}
