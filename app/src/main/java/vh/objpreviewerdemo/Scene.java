package vh.objpreviewerdemo;

import android.content.Context;

import vh.objpreviewerdemo.Shader.ShaderManager;

/**
 * Created by User on 2/23/2018.
 */

public class Scene {

    private Model_No_Maps[] mModelNoMaps;
    private Model_With_Maps[] mModelWithMaps;
    private LightManager mLightManager;

    private final int mModelNoMapsCount;
    private final int mModelWithMapsCount;

    final boolean mIsDrawLightPoints;

    public Scene(Context context, OpenGLMain activity, MyGLSurfaceView surface, final int modelNoMapsCountCount,final int modelWithMapsCount, final int lightCount, boolean isDrawLightPoints)
    {
        mModelNoMapsCount = modelNoMapsCountCount;
        mModelWithMapsCount = modelWithMapsCount;
        // Initialize Models
        if(mModelNoMapsCount > 0)   {
            mModelNoMaps = new Model_No_Maps[mModelNoMapsCount];
            for(int i = 0; i < mModelNoMapsCount; i++){
                mModelNoMaps[i] = new Model_No_Maps(context, activity, surface);
            }
        }

        if(mModelWithMapsCount > 0)   {
            mModelWithMaps = new Model_With_Maps[mModelWithMapsCount];
            for(int i = 0; i < mModelWithMapsCount; i++){
                mModelWithMaps[i] = new Model_With_Maps(context, activity, surface);
            }
        }

        mLightManager = new LightManager(lightCount);
        mIsDrawLightPoints = isDrawLightPoints;
    }

    public void SetModelResourceNoMaps(int objNumber, final int resourceID)
    { mModelNoMaps[objNumber].SetResources(resourceID);}

    public void SetModelResourceWithMaps(int objNumber, final int resourceID)
    { mModelWithMaps[objNumber].SetResources(resourceID);}

    public void SetModelNoMapsProperties(int objNumber, final float[] albedo, final float[] frenel, final float metalic, final float roughness)
    {        mModelNoMaps[objNumber].SetProperties( albedo, frenel, metalic, roughness);    }

    public void SetModelWithMapsProperties(int objNumber, final int albedoRes, final int normalRes, final int ao, final int frenel, final int metalic, final int roughness)
    {        mModelWithMaps[objNumber].SetProperties( albedoRes, normalRes, ao, frenel, metalic, roughness);    }

    public void SetLights(int lightNumber, float horizontalRotation, float verticalRotation, float distance, final float[] color){
        mLightManager.SetLight( lightNumber, horizontalRotation, verticalRotation, distance, color);
    }

    public void Initialize()
    {
        if(mModelNoMapsCount >0){
            for(int i = 0; i < mModelNoMapsCount; i++){
                mModelNoMaps[i].Initialize();
            }
        }

        if(mModelWithMapsCount>0){
            for(int i = 0; i < mModelWithMapsCount; i++){
                mModelWithMaps[i].Initialize();
            }
        }

        mLightManager.Initialize();
    }

    public void draw(ShaderManager shader, float deltaX, float deltaY, RotationStateEnum state,
                     final float[] viewMatrix, final float[] projectionMatrix,
                     final float[] MVSkyMatrix, final float[] invMVSkyMatrix, final float[] MVPSkyMatrix, Skybox sky)
    {
        mLightManager.UpdateLightPosInEyeSpace( MVSkyMatrix );

        // Draw Geometry -----------------------------------
        shader.setPBRShader();
        for(int i = 0; i <mModelNoMapsCount; i++)
        {
            mModelNoMaps[i].draw(deltaX, deltaY, state, shader.getCurrentShaderProgramHandle(),
                    viewMatrix, projectionMatrix, MVSkyMatrix, invMVSkyMatrix, mLightManager, sky);
        }

        shader.setPBRWithMapsShader();
        for(int i = 0; i <mModelWithMapsCount; i++)
        {
            mModelWithMaps[i].draw(deltaX, deltaY, state, shader.getCurrentShaderProgramHandle(),
                    viewMatrix, projectionMatrix, MVSkyMatrix, invMVSkyMatrix, mLightManager, sky);
        }


        // Draw Light points -------------------------------------------
        shader.setPointLightShader();
        mLightManager.draw(shader.getCurrentShaderProgramHandle(), MVPSkyMatrix);




    }

    public void ResetScene()    {
        for(int i =0; i < mModelNoMapsCount; i++){
            mModelNoMaps[i].ResetRotation();
        }
        for(int i =0; i < mModelWithMapsCount; i++){
            mModelWithMaps[i].ResetRotation();
        }
    }

    public void release(){
        for(int i = 0; i < mModelNoMapsCount; i++){
            mModelNoMaps[i].release();
        }
        for(int i =0; i < mModelWithMapsCount; i++){
            mModelWithMaps[i].release();
        }
    }
}
