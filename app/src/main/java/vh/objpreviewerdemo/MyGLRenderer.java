package vh.objpreviewerdemo;

import android.content.Context;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import vh.objpreviewerdemo.Shader.ShaderManager;
import vh.objpreviewerdemo.Utils.MathHelper;


/**
 * Created by User on 2/13/2018.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer
{
    private ShaderManager mShaderManager;
    private Skybox mSkybox;
    private SceneManager mSceneManager;



    private float mDeltaX = 0.0f;
    private float mDeltaY = 0.0f;
    private float mScaleFactor = 1.0f;
    private boolean mIsScaling = false;

    private float mRotationViewHorizontal = 0.0f;
    private float mRotationViewVertical = 0.0f;
    private float rotation_direction = 1.0f;


    final int VIEWPORT_NEAR = 1;
    final int VIEWPORT_FAR = 200;

    final int mObjResource = R.raw.sphere_hres;
    final int mObjDiffTexture = R.drawable.military_png;
    final int mObjNormTexture = R.drawable.null_normal_map;


    private final float[] mRotateHorizontalQuaternion = new float[4];
    private final float[] mRotateVerticalQuaternion = new float[4];
    private final float[] mRotationQuaternion = new float[4];
    private final float[] mViewRotationMatrix = new float[16];


    private final float[] mProjectionMatrix = new float[16];
    private final float[] mInitViewMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mSkyboxViewMatrix = new float[16];
    private final float[] mInvMVSkyMatrix = new float[16];
    private final float[] mVPMatrix = new float[16];
    private final float[] mMVPSkyMatrix = new float[16];
    private final float[] mMVSkyMatrix = new float[16];


    private RotationStateEnum mRotationState;


    private final int SCREEN_WIDTH;
    private final int SCREEN_HEIGHT;


    private Context mContext;
    private OpenGLMain mActivity;
    private MyGLSurfaceView mSurfaceView;

    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private final Object lock3 = new Object();
    private final Object lock4 = new Object();
    private final Object lock5 = new Object();
    private final Object lock6 = new Object();

    private boolean mIsShowSkybox;


    public MyGLRenderer(final Context context, final OpenGLMain activity, final MyGLSurfaceView surface,int width, int height)
    {
        mContext = context;
        mActivity = activity;
        mSurfaceView = surface;

        SCREEN_HEIGHT = height;
        SCREEN_WIDTH = width;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        mShaderManager = new ShaderManager(mContext);
        mSkybox = new Skybox(mContext, mShaderManager,SCREEN_WIDTH, SCREEN_HEIGHT);
        mSceneManager = new SceneManager(mContext, mActivity, mSurfaceView, 0);

        //----------------------------------------------------------------------------------------
        mRotationState = RotationStateEnum.ROTATE_SCENE;
        mIsShowSkybox = true;

        // Set initial camera position(View matrix)
        final float lookAtX = 0.0f;
        final float lookAtY = 0.0f;
        final float lookAtZ = 0.0f;
        Matrix.setLookAtM(mInitViewMatrix, 0,0.0f, 0.0f,7.0f,lookAtX, lookAtY,lookAtZ, 0f, 1.0f, 0.0f);

        MathHelper.quaternionIdentity(mRotationQuaternion);

        // Set initial rotation ---------------------------------------------------------
        mRotationViewVertical = MathHelper.degToRad(-20.0f);

        if(mRotationViewHorizontal >= 6.28319f) { mRotationViewHorizontal += MathHelper.degToRad(mDeltaX) - 6.28319f;}
        else if( mRotationViewHorizontal <= -6.28319f) { mRotationViewHorizontal += MathHelper.degToRad(mDeltaX) + 6.28319f;}
        else{ mRotationViewHorizontal += MathHelper.degToRad(mDeltaX);}

        if(mRotationViewVertical >= 6.28319f) { mRotationViewVertical += MathHelper.degToRad(mDeltaY) - 6.28319f;}
        else if( mRotationViewVertical <= -6.28319f) { mRotationViewVertical += MathHelper.degToRad(mDeltaY) + 6.28319f;}
        else{ mRotationViewVertical += MathHelper.degToRad(mDeltaY);}

        MathHelper.quaternionFromAxisAngle( mRotateHorizontalQuaternion, 0.0f, rotation_direction, 0.0f, mRotationViewHorizontal);
        MathHelper.quaternionFromAxisAngle(mRotateVerticalQuaternion, 1.0f, 0.0f, 0.0f, mRotationViewVertical);
        MathHelper.quaternionMultyply(mRotateHorizontalQuaternion, mRotateVerticalQuaternion, mRotationQuaternion);
        MathHelper.quaternionToMatrix(mRotationQuaternion, mViewRotationMatrix);

        System.arraycopy(mSkyboxViewMatrix, 0, mViewMatrix, 0 , 16);

        //---------------------------------------------------------------------
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

    }

    public void onDrawFrame(GL10 unused)
    {

        // Clear Color Buffer Bit and if Depth is no clear it too
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        // timer
        long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Perform Matrix Calculations -----------------------------------------------------
        rotateView(mDeltaX, mDeltaY, mRotationState);

        scaleView();

        // VP used by skybox
        Matrix.multiplyMM(mViewMatrix, 0, mInitViewMatrix, 0, mViewRotationMatrix, 0);
        Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //MV Sky used to calc light || MVP Sky  used to render light point || Inv MV Sky used for reflection mapping by model
        Matrix.multiplyMM(mMVSkyMatrix, 0, mViewMatrix, 0, mSkybox.GetRotationMatrix(), 0);
        Matrix.multiplyMM(mMVPSkyMatrix, 0, mProjectionMatrix, 0, mMVSkyMatrix, 0);
        Matrix.invertM(mInvMVSkyMatrix, 0, mMVSkyMatrix, 0);


        mSceneManager.draw(mShaderManager, mDeltaX, mDeltaY, mRotationState, mViewMatrix, mProjectionMatrix, mMVSkyMatrix, mInvMVSkyMatrix, mMVPSkyMatrix, mSkybox);



        // Draw skybox ------------------------------------------------------------------------------
        // Remove translation from VP Matrix, skybox doesn't need translation (If translation will be introduced)
        mShaderManager.setSkyboxShader();
        mSkybox.draw(mDeltaX, mDeltaY, mShaderManager.getCurrentShaderProgramHandle(), mVPMatrix, mRotationState, mIsShowSkybox);


        mDeltaX = 0.0f; mDeltaY = 0.0f;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, VIEWPORT_NEAR, VIEWPORT_FAR);

    }


    private void UpdateRotationDirection()
    {
        final float[] up = new float[]{0.0f, 1.0f, 0.0f, 0.0f};
        MathHelper.quaternionRotateVector( mRotationQuaternion, up, up);
        final float dot = MathHelper.vectorDot(up[0], up[1], up[2], 0.0f, 0.0f,1.0f);

        rotation_direction = dot > 0.0 ? -1.0f : 1.0f;
    }

    public void UpdateDeltas(float dX, float dY)
    {
        synchronized (lock1){
            mDeltaX = dX; mDeltaY = dY;
        }
    }
    public void SetScalingFactor(float factor, boolean isScaling)
    {
        synchronized (lock2)
        {
            mScaleFactor = factor;
            mIsScaling = isScaling;
        }
    }
    public void SetRotationState(RotationStateEnum state)
    {
        synchronized (lock3)
        {
            mRotationState = state;
        }
    }

    public void SetShowSkybox(boolean isSkybox)
    {
        synchronized (lock4)
        {
            mIsShowSkybox = isSkybox;
        }
    }
    public void ResetScene()
    {
        synchronized (lock5)
        {
            mRotationViewHorizontal = 0.0f;
            mRotationViewVertical = 0.0f;
            rotateView(0.0f, 0.0f, RotationStateEnum.ROTATE_SCENE);
            mSkybox.ResetRotation();
            mSceneManager.ResetScene();
        }
    }

    public void NextScene(){
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mSceneManager.NextScene();
            }
        });
    }

    public void PrevScene(){
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mSceneManager.PrevScene();
            }
        });
    }

    public void SetHDR(int number){
        mSkybox.SetHDR(number);
    }

    private void rotateView(float deltaX, float deltaY, RotationStateEnum state)
    {
        if(state == RotationStateEnum.ROTATE_SCENE)
        {
            // Handle View Rotation ------------------------------------------------------------------
            if(mRotationViewHorizontal >= 6.28319f) { mRotationViewHorizontal += MathHelper.degToRad(deltaX) - 6.28319f;}
            else if( mRotationViewHorizontal <= -6.28319f) { mRotationViewHorizontal += MathHelper.degToRad(deltaY) + 6.28319f;}
            else{ mRotationViewHorizontal += MathHelper.degToRad(mDeltaX);}

            if(mRotationViewVertical >= 6.28319f) { mRotationViewVertical += MathHelper.degToRad(mDeltaY) - 6.28319f;}
            else if( mRotationViewVertical <= -6.28319f) { mRotationViewVertical += MathHelper.degToRad(mDeltaY) + 6.28319f;}
            else{ mRotationViewVertical += MathHelper.degToRad(mDeltaY);}

            //--------------
            MathHelper.quaternionFromAxisAngle( mRotateHorizontalQuaternion, 0.0f, rotation_direction, 0.0f, mRotationViewHorizontal);
            MathHelper.quaternionFromAxisAngle(mRotateVerticalQuaternion, 1.0f, 0.0f, 0.0f, mRotationViewVertical);
            MathHelper.quaternionMultyply(mRotateHorizontalQuaternion, mRotateVerticalQuaternion, mRotationQuaternion);
            MathHelper.quaternionToMatrix(mRotationQuaternion, mViewRotationMatrix);


        }
    }


    private void scaleView()
    {
        if(mIsScaling) {
            // Just do it the simple way
            float z = 7.0f * (float) Math.pow(3, mScaleFactor - 1.0f);
            if (z <= 3.5f) {
                z = 3.5f;
            } else if (z >= 30.0f) {
                z = 30.0f;
            }
            //Log.e("d",Float.toString(z) + ", " + Float.toString(z));
            Matrix.setLookAtM(mInitViewMatrix, 0, 0.0f, 0.0f, z, 0.0f, 0.0f, 0.0f, 0f, 1.0f, 0.0f);
        }
    }

}
