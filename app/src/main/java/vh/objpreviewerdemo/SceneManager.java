package vh.objpreviewerdemo;

import android.content.Context;
import android.graphics.Path;

import vh.objpreviewerdemo.Shader.ShaderManager;
import vh.objpreviewerdemo.Utils.TextureHelper;

/**
 * Created by User on 2/24/2018.
 */

public class SceneManager {
    private final int mSceneCount = 4;

    private final int[] mSceneModelNoMapsCount = new int[]{
            0, //Scene 0 obj no map count
            1,//1   //Scene 1 obj no map count
            1,
            1
    };

    private final int[] mSceneModelWithMapsCount = new int[]{
            1,   //Scene 0
            0,
            0,
            0
    };
    private final int[] mSceneLightsCount = new int[]{
            0, //Scene 0 obj no map count
            1,//1   // scene 1 obj no map count
            1,
            1
    };

    //------------------ OBJECT NO MAP ----------------------

    private final int[][] mObjNoMapResource =new int[][]{
            { R.raw.sphere_hres }, // scene 0, model 0
            { R.raw.helmet_low_triang },    // scene 1, model 0
            { R.raw.helmet_low_triang },
            { R.raw.helmet_low_triang }
    } ;

    private final float[][][] mObjNoMapAlbedo = new float[][][]{
            {{0.9f, 0.5f, 0.5f}}, // scene 0, model 0
            {{0.9f, 0.5f, 0.5f}}, // scene 1, model 0
            {{0.0f, 0.0f, 0.0f}},
            {{0.6f, 0.1f, 0.1f}},

    };

    private final float[][][]  mObjNoMapFresnel  = new float[][][]{
            {{1.0f, 0.86f, 0.51f}}, // scene 0 , model 0
            {{1.0f, 0.86f, 0.51f}}, // scene 1 , model 0
            {{0.5f, 0.25f, 0.25f}},
            {{0.5f, 0.25f, 0.25f}}
    };

    private final float[][] mObjMetallic = new float[][]{
            {1.0f}, // scene 0, model 0
            {1.0f}, // scene 1, model 0
            {0.0f},
            {0.0f}
    } ;


    private final float[][] mObjRoughness = new float[][]{
            {0.2f}, // scene 0 , model 0
            {0.2f}, // scene 1 , model 0
            {0.6f},
            {0.1f},

    };
    //----------------------- OBJECT WITH MAPS -------------------------------
    private final int[][] mObjWithMapResource = new int[][]{
            {R.raw.helmet_low_triang } //scene 0, model 0
    };

    private final int[][] mObjWithMapAlbedo = new int[][]{
            {R.drawable.helmet_low_albedo}, // scene 0, model 0
    };
    private final int[][] mObjWithMapNormal = new int[][]{
            {R.drawable.helmet_low_normal}, // scene 0, model 0
    };

    private final int[][] mObjWithMapsAO = new int[][]{
            {R.drawable.helmet_ao}
    };

    private final int[][]  mObjWithMapFresnel  = new int[][]{
            {R.drawable.helmet_frenel}, // scene 0 , model 0
    };

    private final int[][] mObjWithMapMetallic = new int[][]{
            {R.drawable.helmet_low_metallic}, // scene 0, model 0
    } ;

    private final int[][] mObjWithMapRoughness = new int[][]{
            {R.drawable.helmet_low_roughness}, // scene 0 , model 0
    };


    //-------------------------------------------------------------
    final float[][][] mLightPosition = new float[][][]{
            {{320.0f, 45.0f, 17.0f}}, // scene 0, light 0, horizontal rot / vertical rot/ distance
            {{320.0f, 45.0f, 17.0f}},
            {{320.0f, 45.0f, 17.0f}},
            {{320.0f, 45.0f, 17.0f}}
    };
    final float[][][] mLightColor = new float[][][]{
            {{1.0f, 1.0f, 1.0f, 1.0f}}, // scene 0, light 0
            {{60.0f, 60.0f, 60.0f, 1.0f}},
            {{120.0f, 120.0f, 120.0f, 1.0f}},
            {{120.0f, 120.0f, 120.0f, 1.0f}}
    };


    private Scene mScene = null;
    private Context mContext;
    private OpenGLMain mActivity;
    private MyGLSurfaceView mSurfaceView;

    private int mCurrentScene;

    public SceneManager(Context context, OpenGLMain activity, MyGLSurfaceView surface, final int defaultSceneNumber){
        mContext = context;
        mActivity = activity;
        mSurfaceView = surface;
        mCurrentScene = defaultSceneNumber;

        ChangeScene( defaultSceneNumber );
    }

    public void draw(ShaderManager shader, float deltaX, float deltaY, RotationStateEnum state, final float[] viewMatrix, final float[] projMatrix,
                final float[] MVSkyMatrix, final float[] invMVSkyMatrix, final float[] MVPSkyMatrix, Skybox sky)
    {
        mScene.draw(shader, deltaX, deltaY, state, viewMatrix, projMatrix, MVSkyMatrix, invMVSkyMatrix, MVPSkyMatrix, sky);
    }

    public void ResetScene(){ mScene.ResetScene();}

    public void ChangeScene(int sceneNumber)
    {
        if(mScene != null){   mScene.release();   mScene = null;}

        mScene = new Scene(mContext, mActivity, mSurfaceView, mSceneModelNoMapsCount[sceneNumber], mSceneModelWithMapsCount[sceneNumber], mSceneLightsCount[sceneNumber], true);

        for(int i = 0; i < mSceneModelNoMapsCount[sceneNumber]; i++){
            mScene.SetModelResourceNoMaps(i, mObjNoMapResource[sceneNumber][i]);
            mScene.SetModelNoMapsProperties(i, mObjNoMapAlbedo[sceneNumber][i], mObjNoMapFresnel[sceneNumber][i],
                    mObjMetallic[sceneNumber][i], mObjRoughness[sceneNumber][i]);
        }

        for(int i = 0; i < mSceneModelWithMapsCount[sceneNumber]; i++){
            mScene.SetModelResourceWithMaps(i, mObjWithMapResource[sceneNumber][i]);

            // This was a temporary fix, can be moved back to Model_With_Maps class ->>>
            final int mModelAlbedoData = TextureHelper.loadTexture_2D(mContext, mObjWithMapAlbedo[sceneNumber][i]);
            final int mModelNormalData = TextureHelper.loadTexture_2D(mContext, mObjWithMapNormal[sceneNumber][i]);
            final int mModelAoData = TextureHelper.loadTexture_2D(mContext, mObjWithMapsAO[sceneNumber][i]);
            final int mModelFrenelData = TextureHelper.loadTexture_2D(mContext, mObjWithMapFresnel[sceneNumber][i]);
            final int mModelMetallicData = TextureHelper.loadTexture_2D(mContext, mObjWithMapMetallic[sceneNumber][i]);
            final int mModelRoughnessData = TextureHelper.loadTexture_2D(mContext, mObjWithMapRoughness[sceneNumber][i]);
            mScene.SetModelWithMapsProperties(i, mModelAlbedoData, mModelNormalData, mModelAoData , mModelFrenelData,
                    mModelMetallicData, mModelRoughnessData);
        }


        for(int i = 0; i < mSceneLightsCount[sceneNumber]; i++){
            mScene.SetLights(i, mLightPosition[sceneNumber][i][0], mLightPosition[sceneNumber][i][1], mLightPosition[sceneNumber][i][2], mLightColor[sceneNumber][i]);
        }

        mScene.Initialize();
    }

    public void NextScene()    {
        mCurrentScene++;

        if(mCurrentScene > mSceneCount - 1){
            mCurrentScene = 0;
        }

        ChangeScene( mCurrentScene );
    }

    public void PrevScene(){
        mCurrentScene--;

        if(mCurrentScene < 0){
            mCurrentScene = mSceneCount-1;
        }

        ChangeScene(mCurrentScene);
    }

}
