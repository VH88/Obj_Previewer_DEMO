package vh.objpreviewerdemo;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.widget.Toast;

import java.nio.FloatBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javax.microedition.khronos.opengles.GL;

import vh.objpreviewerdemo.Model.ModelContainer;
import vh.objpreviewerdemo.Shader.Light;
import vh.objpreviewerdemo.Utils.MathHelper;
import vh.objpreviewerdemo.Utils.TextureHelper;

/**
 * Created by User on 2/13/2018.
 */

public class Model_With_Maps {
    private int mModelAlbedoData;
    private int mModelNormalData;
    private int mModelAoData;
    private int mModelFrenelData;
    private int mModelMetallicData;
    private int mModelRoughnessData;

    private float[] mModelPosWorld = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    private float[] mModelPosInEyeSpace = new float[4];

    private static int mPositionHandle = 0;
    private static int mTextureCoordHandle = 1;
    private static int mNormalHandle = 2;
    private static int mTangentHandle =3;
    private static int mBitangentHandle = 4;

    private float mDeltaX = 0.0f;
    private float mDeltaY = 0.0f;
    private float rotation_horizontal = 0.0f;
    private float rotation_vertical = 0.0f;
    private float rotation_horizontal_init;
    private float rotation_vertical_init;

    private int mModelPosInEyeSpaceHandle;
    private int mEnvMatrixHandle;
    private int mMVMatrixHandle;
    private int mMVPMatrixHandle;
    private int mInvMVMatrixHandle;
    private int mMVSkyMatrixHandle;


    private final float[] mRotateHorizontalQuaternion = new float[4];
    private final float[] mRotateVerticalQuaternion = new float[4];
    private final float[] mRotationQuaternion = new float[4];
    private final float[] mModelRotationMatrix = new float[16];

    private final float[] mModelMatrix = new float[16];
    private final float[] mInitModelMatrix = new float[16];
    private final float[] mMVMatrix = new float[16];
    private final float[] mTempMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mInvVMatrix = new float[16];

    private Context mContext;
    private OpenGLMain mActivity;
    private MyGLSurfaceView mSurfaceView;
    private int mObjResource;

    private int mTextureIrradCubemapHandle;
    private int mTexturePrefilteredCubemapHandle;
    private int mTextureBRDFLUTHandle;
    private int mLightDataHandle;
    private int mLightCountHandle;

    private int mModelTextureAlbedoHandle;
    private int mModelTextureNormalHandle;
    private int mModelTextureAoHandle;
    private int mModelFrenelHandle;
    private int mModelRoughnessHandle;
    private int mModelMetalicHandle;

    private boolean isFirstCall = true;

    private ModelContainer mModel = null;

    // Async loading -----------------------------------------------/
    private ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    class AsyncLoad implements Runnable
    {
        @Override
        public void run(){
            try {
                mSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mModel = new ModelContainer(mContext, mObjResource);
                    }
                });
            }catch (OutOfMemoryError e)
            {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, "Out of memory while loading model: " +  Integer.toString(mObjResource), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    public Model_With_Maps(final Context context, final OpenGLMain activity, MyGLSurfaceView surfView)
    {
        mContext = context;
        mActivity= activity;
        mSurfaceView = surfView;

        Matrix.setIdentityM(mModelMatrix,0);

        //----- Calculate initial rotation -----------------------------------
        if(rotation_horizontal >= 6.28319f) { rotation_horizontal += MathHelper.degToRad(mDeltaX) - 6.28319f;}
        else if( rotation_horizontal <= -6.28319f) { rotation_horizontal += MathHelper.degToRad(mDeltaX) + 6.28319f;}
        else{ rotation_horizontal += MathHelper.degToRad(mDeltaX);}

        if(rotation_vertical >= 6.28319f) { rotation_vertical += MathHelper.degToRad(mDeltaY) - 6.28319f;}
        else if( rotation_vertical <= -6.28319f) { rotation_vertical += MathHelper.degToRad(mDeltaY) + 6.28319f;}
        else{ rotation_vertical += MathHelper.degToRad(mDeltaY);}

        MathHelper.quaternionFromAxisAngle( mRotateHorizontalQuaternion, 0.0f, 1.0f, 0.0f, rotation_horizontal );
        MathHelper.quaternionFromAxisAngle(mRotateVerticalQuaternion, 1.0f, 0.0f, 0.0f, rotation_vertical);
        MathHelper.quaternionMultyply(mRotateHorizontalQuaternion, mRotateVerticalQuaternion, mRotationQuaternion);
        MathHelper.quaternionToMatrix(mRotationQuaternion, mModelRotationMatrix);

        Matrix.multiplyMM(mInitModelMatrix, 0, mModelRotationMatrix, 0, mModelMatrix, 0);

        rotation_horizontal_init = rotation_horizontal;
        rotation_vertical_init = rotation_vertical;
    }

    public void draw(float deltaX, float deltaY, RotationStateEnum rotationState, final int program,
                     final float[] viewMatrix,final float[] projMatrix, final float[] MVSkyMatrix, final float[] invMVSkyMatrix,
                     LightManager lightManager, Skybox sky)
    {
        if(mModel != null) {
            int stride = (mModel.POSITION_DATA_SIZE + mModel.TEXTURE_DATA_SIZE + mModel.NORMAL_DATA_SIZE
                    + mModel.TANGENT_DATA_SIZE + mModel.BITANGENT_DATA_SIZE) * mModel.BYTES_PER_FLOAT;


            rotateModel(deltaX, deltaY, rotationState);

            Matrix.multiplyMM(mMVMatrix, 0, viewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, projMatrix, 0, mMVMatrix, 0);

            Matrix.multiplyMM(mTempMatrix, 0, viewMatrix, 0, sky.GetRotationMatrix(), 0);
            Matrix.invertM(mInvVMatrix, 0, mTempMatrix, 0);

            Matrix.multiplyMV(mModelPosInEyeSpace, 0, viewMatrix, 0, mModelPosWorld, 0);

            //---------------------------------------------------------------------------
            if(isFirstCall) {
                mMVMatrixHandle = GLES30.glGetUniformLocation(program, "uMVMatrix");
                mMVPMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
                mInvMVMatrixHandle = GLES30.glGetUniformLocation(program, "uInvMVSkyMatrix");
                mEnvMatrixHandle = GLES30.glGetUniformLocation(program, "uEnvMatrix");
                mMVSkyMatrixHandle = GLES30.glGetUniformLocation(program, "uMVSkyMatrix");


                mTextureIrradCubemapHandle = GLES30.glGetUniformLocation(program, "uTextureIrradianceCubemap");
                mTexturePrefilteredCubemapHandle = GLES30.glGetUniformLocation(program, "uPrefilterMap");
                mTextureBRDFLUTHandle = GLES30.glGetUniformLocation(program, "uBRDFLUT");

                mLightCountHandle = GLES30.glGetUniformLocation(program, "uLightCount");
                mLightDataHandle = GLES30.glGetUniformLocation(program, "uLightSources");

                mModelPosInEyeSpaceHandle = GLES30.glGetUniformLocation(program, "uModelPosInEyeSpace");
                mModelTextureAlbedoHandle = GLES30.glGetUniformLocation(program, "uAlbedo");
                mModelTextureNormalHandle = GLES30.glGetUniformLocation(program, "uTextureNormal");
                mModelTextureAoHandle = GLES30.glGetUniformLocation(program, "uTextureAO");
                mModelFrenelHandle = GLES30.glGetUniformLocation(program, "uFresnel");
                mModelMetalicHandle = GLES30.glGetUniformLocation(program, "uMetallic");
                mModelRoughnessHandle = GLES30.glGetUniformLocation(program, "uRoughness");



                isFirstCall = false;
            }

            //--------- HANDLE TEXTURES ----------------
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, sky.getIrradianceCubemap());
            GLES30.glUniform1i(mTextureIrradCubemapHandle, 0);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, sky.getSpecularCubemap());
            GLES30.glUniform1i(mTexturePrefilteredCubemapHandle, 1);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, sky.getBRDFLUTTexture());
            GLES30.glUniform1i(mTextureBRDFLUTHandle, 2);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE3);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mModelAlbedoData);
            GLES30.glUniform1i(mModelTextureAlbedoHandle, 3);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mModelNormalData);
            GLES30.glUniform1i(mModelTextureNormalHandle, 4);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mModelMetallicData);
            GLES30.glUniform1i(mModelMetalicHandle, 5);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE6);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mModelRoughnessData);
            GLES30.glUniform1i(mModelRoughnessHandle, 6);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE7);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mModelAoData);
            GLES30.glUniform1i(mModelTextureAoHandle, 7);

            GLES30.glActiveTexture(GLES30.GL_TEXTURE8);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mModelFrenelData);
            GLES30.glUniform1i(mModelFrenelHandle, 8);

            //--------------------------------------------------------
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mModel.getModelDataVBO());
            GLES30.glEnableVertexAttribArray(mPositionHandle);
            GLES30.glEnableVertexAttribArray(mTextureCoordHandle);
            GLES30.glEnableVertexAttribArray(mNormalHandle);
            GLES30.glEnableVertexAttribArray(mTangentHandle);
            GLES30.glEnableVertexAttribArray(mBitangentHandle);

            GLES30.glVertexAttribPointer(mPositionHandle, mModel.POSITION_DATA_SIZE, GLES30.GL_FLOAT, false, stride, 0);
            GLES30.glVertexAttribPointer(mTextureCoordHandle, mModel.TEXTURE_DATA_SIZE, GLES30.GL_FLOAT, false, stride,
                    mModel.POSITION_DATA_SIZE * mModel.BYTES_PER_FLOAT);
            GLES30.glVertexAttribPointer(mNormalHandle, mModel.NORMAL_DATA_SIZE, GLES30.GL_FLOAT, false, stride,
                    (mModel.POSITION_DATA_SIZE + mModel.TEXTURE_DATA_SIZE) * mModel.BYTES_PER_FLOAT);
            GLES30.glVertexAttribPointer(mTangentHandle, mModel.TANGENT_DATA_SIZE, GLES30.GL_FLOAT, false, stride,
                    (mModel.POSITION_DATA_SIZE + mModel.TEXTURE_DATA_SIZE + mModel.TANGENT_DATA_SIZE) * mModel.BYTES_PER_FLOAT);
            GLES30.glVertexAttribPointer(mBitangentHandle, mModel.TANGENT_DATA_SIZE, GLES30.GL_FLOAT, false, stride,
                    (mModel.POSITION_DATA_SIZE + mModel.TEXTURE_DATA_SIZE + mModel.TANGENT_DATA_SIZE + mModel.BITANGENT_DATA_SIZE) * mModel.BYTES_PER_FLOAT);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glUniformMatrix4fv(mEnvMatrixHandle, 1, false, mModelMatrix, 0);
            GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVMatrix, 0);
            GLES30.glUniformMatrix4fv(mInvMVMatrixHandle, 1, false, invMVSkyMatrix, 0);
            GLES30.glUniformMatrix4fv(mMVSkyMatrixHandle, 1, false, MVSkyMatrix, 0);

            GLES30.glUniform1i(mLightCountHandle, lightManager.GetLightCount());
            // light count * 2 because array hold positions and colors
            GLES30.glUniform4fv(mLightDataHandle, lightManager.GetLightCount() * 2 , lightManager.GetLightUniformArray(), 0);



            GLES30.glUniform3f(mModelPosInEyeSpaceHandle, mModelPosInEyeSpace[0],mModelPosInEyeSpace[1], mModelPosInEyeSpace[2]);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, mModel.getModelIndexVBO());
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, mModel.VERTEX_COUNT, GLES30.GL_UNSIGNED_INT, 0);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

            GLES30.glDisableVertexAttribArray(mPositionHandle);
            GLES30.glDisableVertexAttribArray(mTextureCoordHandle);
            GLES30.glDisableVertexAttribArray(mNormalHandle);
            GLES30.glDisableVertexAttribArray(mTangentHandle);
            GLES30.glDisableVertexAttribArray(mBitangentHandle);
        }
    }

    public void SetProperties(final int albedoRes, final int normalRes, final int ao, final int frenel, int metalic, int roughness)
    {
        mModelAlbedoData = albedoRes;
        mModelNormalData = normalRes;
        mModelAoData = ao;
        mModelFrenelData = frenel;
        mModelMetallicData = metalic;
        mModelRoughnessData = roughness;
    }

    public void SetResources(final int objResource)    {mObjResource = objResource;}

    public void Initialize()    {    mSingleThreadExecutor.submit(new AsyncLoad());    }

    private void rotateModel(float deltaX, float deltaY, RotationStateEnum state)
    {
        if(state == RotationStateEnum.ROTATE_OBJECT ) {
            mDeltaX = deltaX;
            mDeltaY = deltaY;

            if (rotation_horizontal >= 6.28319f) {
                rotation_horizontal += MathHelper.degToRad(mDeltaX) - 6.28319f;
            } else if (rotation_horizontal <= -6.28319f) {
                rotation_horizontal += MathHelper.degToRad(mDeltaX) + 6.28319f;
            } else {
                rotation_horizontal += MathHelper.degToRad(mDeltaX);
            }

            if (rotation_vertical >= 6.28319f) {
                rotation_vertical += MathHelper.degToRad(mDeltaY) - 6.28319f;
            } else if (rotation_vertical <= -6.28319f) {
                rotation_vertical += MathHelper.degToRad(mDeltaY) + 6.28319f;
            } else {
                rotation_vertical += MathHelper.degToRad(mDeltaY);
            }

            MathHelper.quaternionFromAxisAngle(mRotateHorizontalQuaternion, 0.0f, 1.0f, 0.0f, rotation_horizontal);
            MathHelper.quaternionFromAxisAngle(mRotateVerticalQuaternion, 1.0f, 0.0f, 0.0f, rotation_vertical);
            MathHelper.quaternionMultyply(mRotateHorizontalQuaternion, mRotateVerticalQuaternion, mRotationQuaternion);
            MathHelper.quaternionToMatrix(mRotationQuaternion, mModelRotationMatrix);


            Matrix.multiplyMM(mModelMatrix, 0, mModelRotationMatrix, 0, mInitModelMatrix, 0);
        }

    }

    public void release()
    {
        mModel.release();
        final int[] texturesToDelete = new int[]{mModelAlbedoData, mModelNormalData, mModelAoData, mModelMetallicData, mModelRoughnessData, mModelFrenelData};
        GLES30.glDeleteTextures(texturesToDelete.length, texturesToDelete, 0);
    }

    public void ResetRotation()
    {
        rotation_horizontal = rotation_horizontal_init;
        rotation_vertical = rotation_vertical_init;
        rotateModel(0.0f, 0.0f, RotationStateEnum.ROTATE_OBJECT);
    }
}
