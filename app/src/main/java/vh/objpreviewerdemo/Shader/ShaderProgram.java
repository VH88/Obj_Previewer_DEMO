package vh.objpreviewerdemo.Shader;

import android.content.Context;
import android.opengl.GLES30;

import vh.objpreviewerdemo.Utils.ShaderHelper;

/**
 * Created by User on 2/14/2018.
 */

public class ShaderProgram {
    private final int mProgram;



    public ShaderProgram(final Context context, final int vertexShaderResource, final int fragmentShaderResource)
    {
        int vertexShader = ShaderHelper.compileShader(GLES30.GL_VERTEX_SHADER, ShaderHelper.readTextFileFromRawResource(context, vertexShaderResource));
        int fragmentShader = ShaderHelper.compileShader(GLES30.GL_FRAGMENT_SHADER, ShaderHelper.readTextFileFromRawResource(context, fragmentShaderResource));

        mProgram = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader);
    }

    public void setShader()  {     GLES30.glUseProgram(mProgram);   }

    public void release() { GLES30.glDeleteProgram(mProgram);}

    public final int getShaderHandle(){return  mProgram;}


}
