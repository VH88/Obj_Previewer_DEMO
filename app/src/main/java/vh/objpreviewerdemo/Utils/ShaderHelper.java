package vh.objpreviewerdemo.Utils;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by User on 2/13/2018.
 */

public class ShaderHelper {
    /**
     * Helper function to parse ASCII file and load into list of Strings
     *
     * @param context: context of the activity
     * @param resourceID: example (R.raw.filename)
     * @return a list of Strings
     */
    public static String readTextFileFromRawResource(final Context context, final int resourceID){

        final InputStream inputStream = context.getResources().openRawResource(resourceID);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try{
            while ((nextLine = bufferedReader.readLine()) != null){
                body.append(nextLine);
                body.append('\n');
            }
        }
        catch (IOException e){
            return null;
        }

        return  body.toString();

    }


    /**
     * Helper function to compile shader
     *
     * @param shaderType: Shader Type
     * @param shaderSource: Shader source code
     * @return an opneGL handle to the shader
     */
    public static  int compileShader(final int shaderType, final String shaderSource){
        int shaderHandle = GLES30.glCreateShader(shaderType);

        if(shaderHandle != 0){
            GLES30.glShaderSource(shaderHandle, shaderSource);
            GLES30.glCompileShader(shaderHandle);

            final int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(shaderHandle, GLES30.GL_COMPILE_STATUS, compileStatus, 0);

            if(compileStatus[0] == 0){
                Log.e("UtilClass", "Error compiling shader: " + GLES30.glGetProgramInfoLog(shaderHandle));
                GLES30.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0 ){
            throw new RuntimeException("Error creating shader");
        }

        return shaderHandle;
    }

    /**
     * Helper to create and link shader program
     * @param vertexShaderHandle: Handle to compiled vertex shader
     * @param fragmentShaderHandle: Handle to compiled fragment shader
     * @return handle to the shader program
     */
    public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle){
        int programHandle = GLES30.glCreateProgram();

        if(programHandle != 0 ){
            GLES30.glAttachShader(programHandle, vertexShaderHandle);
            GLES30.glAttachShader(programHandle, fragmentShaderHandle);


            GLES30.glLinkProgram(programHandle);

            GLES30.glDetachShader(programHandle, vertexShaderHandle );
            GLES30.glDetachShader(programHandle, fragmentShaderHandle );
            GLES30.glDeleteShader(vertexShaderHandle);
            GLES30.glDeleteShader(fragmentShaderHandle);


            final int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(programHandle, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if(linkStatus[0] == 0){
                Log.e("UtilClass", "Error compiling program: " + GLES30.glGetProgramInfoLog(programHandle));
                GLES30.glDeleteProgram(programHandle);
                programHandle = 0;
            }

            if(programHandle == 0){
                throw new RuntimeException("Error creating program" );
            }
        }

        return programHandle;
    }


}
