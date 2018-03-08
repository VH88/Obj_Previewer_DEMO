package vh.objpreviewerdemo.Shader;

import android.content.Context;

import vh.objpreviewerdemo.R;

/**
 * Created by User on 2/14/2018.
 */

public class ShaderManager {

    private ShaderProgram mBlinn;
    private ShaderProgram mPBR;
    private ShaderProgram mPBR_withMaps;
    private ShaderProgram mPointLight;
    private ShaderProgram mSkybox;
    private ShaderProgram mSkybox_equirect;
    private ShaderProgram mTriangle; // render to square, simply pass color to from texture
    private ShaderProgram mIrradiance_calc;
    private ShaderProgram mSpecular_calc;
    private ShaderProgram mBRDF_calc;

    private int mCurrentShader = -1;

    private final int mBlinnVertexResource = R.raw.blinn_vertex_shader;
    private final int mBlinnFragmentResource = R.raw.blinn_fragment_shader;

    private final int mPointLightVertexResource = R.raw.point_vertex_shader;
    private final int mPointLightFragmentResource = R.raw.point_fragment_shader;

    private final int mSkyboxVertexResource = R.raw.skybox_vertex_shader;
    private final int mSkyboxFragmentResource = R.raw.skybox_fragment_shader;

    private final int mSkyboxEquirectVRes = R.raw.skybox_equirect_v;
    private final int mSkyboxEquirectFRes = R.raw.skybox_equirect_f;

    private final int mPBRVertexResource = R.raw.pbr_no_maps_vertex_shader;
    private final int mPBRFragmentResource = R.raw.pbr_no_maps_fragment_shader;

    private final int mPBRWithMapsVertRes = R.raw.pbr_with_maps_vertex_shader;
    private final int mPBRWithMapsFragRes = R.raw.pbr_with_maps_fragment_shader;

    private final int mTriangleVertexRes = R.raw.triangle_vertex_shader;
    private final int mTriangleFragRes = R.raw.triangle_fragment_shader;

    private final int mIrradianceVRes = R.raw.calc_irradiance_vertex_shader;
    private final int mIrradianceFRes = R.raw.calc_irradiance_fragment_shader;

    private final int mSpecularVRes = R.raw.convolute_specular_cubemap_shader_v;
    private final int mSpecularFRes = R.raw.convolute_specular_cubemap_shader_f;

    private final int mBRDFVertexRes  = R.raw.calc_brdf_vertex;
    private final int mBRDFFragmentRes = R.raw.calc_brdf_fragment;

    public ShaderManager(final Context context)
    {
        mBlinn = new ShaderProgram(context, mBlinnVertexResource, mBlinnFragmentResource);
        mPointLight = new ShaderProgram(context, mPointLightVertexResource, mPointLightFragmentResource);
        mSkybox = new ShaderProgram(context, mSkyboxVertexResource, mSkyboxFragmentResource);
        mSkybox_equirect = new ShaderProgram(context, mSkyboxEquirectVRes, mSkyboxEquirectFRes);
        mPBR = new ShaderProgram(context, mPBRVertexResource, mPBRFragmentResource);
        mPBR_withMaps = new ShaderProgram(context, mPBRWithMapsVertRes, mPBRWithMapsFragRes);

        mTriangle = new ShaderProgram(context, mTriangleVertexRes, mTriangleFragRes);
        mIrradiance_calc = new ShaderProgram(context, mIrradianceVRes, mIrradianceFRes);
        mSpecular_calc = new ShaderProgram(context, mSpecularVRes, mSpecularFRes);
        mBRDF_calc = new ShaderProgram(context, mBRDFVertexRes, mBRDFFragmentRes);
    }

    public void setBlinnShader(){ mBlinn.setShader(); mCurrentShader = mBlinn.getShaderHandle();}

    public void setPBRShader(){mPBR.setShader(); mCurrentShader = mPBR.getShaderHandle();}

    public void setPBRWithMapsShader(){mPBR_withMaps.setShader(); mCurrentShader = mPBR_withMaps.getShaderHandle();}


    public void setPointLightShader() {mPointLight.setShader(); mCurrentShader = mPointLight.getShaderHandle();}

    public void setSkyboxShader() {mSkybox.setShader(); mCurrentShader = mSkybox.getShaderHandle();}

    public void setSkyboxEquirectShader(){mSkybox_equirect.setShader(); mCurrentShader = mSkybox_equirect.getShaderHandle();}

    public void setTriangleShader(){mTriangle.setShader(); mCurrentShader = mTriangle.getShaderHandle();}

    public void setIrradianceCalcShader(){mIrradiance_calc.setShader(); mCurrentShader = mIrradiance_calc.getShaderHandle();}

    public void setSpecularCalcShader() {mSpecular_calc.setShader(); mCurrentShader = mSpecular_calc.getShaderHandle();}

    public void setBRDFCalcShader() {mBRDF_calc.setShader(); mCurrentShader = mBRDF_calc.getShaderHandle();}

    public final int getCurrentShaderProgramHandle(){return mCurrentShader;}

    public void release()
    {
        mBlinn.release();
        mPointLight.release();
        mSkybox.release();
        mSkybox_equirect.release();
        mPBR.release();
        mPBR_withMaps.release();
        mTriangle.release();
        mIrradiance_calc.release();

    }

}
