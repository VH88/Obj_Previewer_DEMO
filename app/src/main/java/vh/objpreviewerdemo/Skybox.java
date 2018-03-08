package vh.objpreviewerdemo;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLES32;
import android.opengl.Matrix;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import vh.objpreviewerdemo.Model.ModelContainer;
import vh.objpreviewerdemo.Shader.ShaderManager;
import vh.objpreviewerdemo.Utils.MathHelper;
import vh.objpreviewerdemo.Utils.TextureHelper;

/**
 * Created by User on 2/15/2018.
 */

public class Skybox {
    private final int VERTEX_COUNT = 36;
    private final float CUBEMAP_SCALE_FACTOR = 50.0f;
    private final int CUBEMAP_RESOLUTION = 512;
    private final int IRRADIANCEMAP_RESOLUTION = 32;
    private final int SPECULARMAP_RESOLUTION = 128;
    private final int BRDF_RESOLUTION = 512;


    private final int[] mResourceList = new int[]{
            R.raw.gravel_plaza,
            R.raw.charles_river,
            R.raw.ice_lake,
            R.raw.old_industrial,
            R.raw.popcorn_lobby,
            R.raw.tropical_ruins,
            R.raw.winter_forest
    };

    private final int SCREEN_WIDTH;
    private final int SCREEN_HEIGHT;


    // NOTE: 16-bit precision float is recommended format by Epic Games
    private final int mVertexVBOHandle;
    private int mIrradianceCubemapHandle;
    private int mSpecularCubemapHandle;
    private int mEnvCubemapHandle;
    private int mBRDFLUTTextureHandle;

    private boolean mIsEnvHandleReady = false;
    private boolean mIsIrradHandleReady = false;
    private boolean mIsSpecHandleReady = false;
    private boolean mIsBRDFLUTHandleReady = false;
    private int mTempEnvHandle;
    private int mTempIrradHandle;
    private int mTempSpecHandle;
    private int mTempBRDFLUTHandle;


    private int mvpMatrixHandle;
    private int textureHandle;
    private int mRoughnessHandle;

    private Quad mQuad;

    private int mCurrentHDR;
    private int mHDRToChange = 0;
    private boolean mIsChangeHDR = false;
    private boolean isFirstCall = true;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] mModelInitMatrix = new float[16];
    private final float[] mModelRotationMatrix =  new float[16];
    private float mRotationHorizontal = 0.0f;
    private float mRotationVertical = 0.0f;
    private final float[] mRotationVerticalQuaternion = new float[4];
    private final float[] mRotationHorizontalQuaternion = new float[4];

    private Context mContext;
    private ShaderManager mShaderManager;


    final int[] captureFBO = new int[1];
    final int[] captureRBO = new int[1];
    
    float cubePositionData[] = {
            -1.0f,  1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f, -1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f, -1.0f,  1.0f,
            -1.0f, -1.0f,  1.0f,

            -1.0f,  1.0f, -1.0f,
            1.0f,  1.0f, -1.0f,
            1.0f,  1.0f,  1.0f,
            1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  1.0f,
            -1.0f,  1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f,  1.0f,
            1.0f, -1.0f,  1.0f
    };



    public Skybox(final Context context, final ShaderManager shader, int width, int height)
    {
        mContext = context;
        mShaderManager = shader;

        mShaderManager = shader;
        SCREEN_HEIGHT = height; SCREEN_WIDTH = width;

        for(int i = 0; i < cubePositionData.length; i++)
        {cubePositionData[i] *= CUBEMAP_SCALE_FACTOR;}
        // Initialize Vertex Buffer Object
        FloatBuffer vertexBuffer;
        vertexBuffer = ByteBuffer.allocateDirect(cubePositionData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(cubePositionData);
        vertexBuffer.position(0);

        int[] buffer = new int[1];
        GLES30.glGenBuffers(1, buffer, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffer[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);
        mVertexVBOHandle = buffer[0];
        vertexBuffer.limit(0);

        mQuad = new Quad(context);

        final int nullmap =  R.drawable.null_texture;
        mTempEnvHandle = TextureHelper.loadTexture_cubeMap( context, false, nullmap, nullmap, nullmap, nullmap, nullmap, nullmap );
        mTempIrradHandle = mTempEnvHandle;
        mTempSpecHandle  = TextureHelper.loadTexture_cubeMap( context, true, nullmap, nullmap, nullmap, nullmap, nullmap, nullmap );
        mTempBRDFLUTHandle = TextureHelper.loadTexture_2D(context, R.drawable.null_texture);


        equirectMapToCubemap(mShaderManager, mContext, mResourceList[0]);
        mCurrentHDR = 0;

        setIsMapsReady(false);
        mIsEnvHandleReady = true;

        Matrix.setIdentityM(mModelInitMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setIdentityM(mModelRotationMatrix, 0);
    }

    private void equirectMapToCubemap(ShaderManager shader, Context context, final int resourceID)
    {
        final int mCubemapEquirectHandle = TextureHelper.loadTexture_2D_hdr(context, resourceID);

        //---- EQUIRECT MAP TO CUBEMAP --------------------------------------------
        // Render 6 side of the cube to capture environmental map
        final int[] envCubemap = new int[1];

        GLES30.glGenFramebuffers(1, captureFBO, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);

        // Create a texture to store new cubemap with an empty texture bind
        GLES30.glGenTextures(1, envCubemap, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, envCubemap[0]);

        for(int i =0; i < 6; i++)
        {
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GLES30.GL_RGB16F,
                    CUBEMAP_RESOLUTION, CUBEMAP_RESOLUTION, 0,  GLES30.GL_RGB, GLES30.GL_FLOAT, null);
        }
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);


        mEnvCubemapHandle = envCubemap[0];

        GLES30.glGenTextures(1, captureRBO, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, captureRBO[0]);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, CUBEMAP_RESOLUTION , CUBEMAP_RESOLUTION);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, captureRBO[0]);
        //GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_CUBE_MAP);

        // Check if buffer was created
        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)!= GLES30.GL_FRAMEBUFFER_COMPLETE)
        {        Log.e("FrameBuffer"," Framebuffer wasn't created in skybox");       }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        // Generate 6 view matrices with FOV 90 to capture entire surface of cube's side
        final float[][] captureViews = new float[6][16];
        final float[] projMatrix = new float[16];
        final float[] VPMatrix = new float[16];

        Matrix.setLookAtM(captureViews[0], 0, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[1], 0, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[2], 0, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0f, 0.0f, 1.0f);
        Matrix.setLookAtM(captureViews[3], 0, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0f, 0.0f, -1.0f);
        Matrix.setLookAtM(captureViews[4], 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[5], 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0f, -1.0f, 0.0f);

        float ratio =  1.0f;
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 1, 50);

        // IMPORTANT: don't forget to set the viewport resolution
        GLES30.glViewport(0, 0, CUBEMAP_RESOLUTION, CUBEMAP_RESOLUTION);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);

        for (int i = 0; i < 6; ++i) {
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, mEnvCubemapHandle, 0);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            Matrix.multiplyMM(VPMatrix, 0, projMatrix, 0, captureViews[i], 0);
            shader.setSkyboxEquirectShader();
            drawToFramebuffer(shader.getCurrentShaderProgramHandle(), VPMatrix, mCubemapEquirectHandle, 0.0f, false);
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, envCubemap[0]);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_CUBE_MAP);
        // Restore default
        final int[] deleteEquirectTexture = new int[]{mCubemapEquirectHandle};
        GLES30.glDeleteTextures(1, deleteEquirectTexture, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    }

    private void convoluteIrradiance(ShaderManager shader)
    {
        final int[] irradianceCubemap = new int[1];

        GLES30.glGenFramebuffers(1, captureFBO, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);

        // Create a texture to store new cubemap with an empty texture bind
        GLES30.glGenTextures(1, irradianceCubemap, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, irradianceCubemap[0]);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, irradianceCubemap[0]);
        for(int i =0; i < 6; i++)
        {
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GLES30.GL_RGB16F,
                    IRRADIANCEMAP_RESOLUTION, IRRADIANCEMAP_RESOLUTION, 0,  GLES30.GL_RGB, GLES30.GL_FLOAT, null);
        }
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

        mIrradianceCubemapHandle = irradianceCubemap[0];

        GLES30.glGenTextures(1, captureRBO, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, captureRBO[0]);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, IRRADIANCEMAP_RESOLUTION , IRRADIANCEMAP_RESOLUTION);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, captureRBO[0]);

        // Check if buffer was created
        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)!= GLES30.GL_FRAMEBUFFER_COMPLETE)
        {        Log.e("FrameBuffer"," Framebuffer wasn't created in skybox");       }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        // Generate 6 view matrices with FOV 90 to capture entire surface of cube's side
        final float[][] captureViews = new float[6][16];
        final float[] projMatrix = new float[16];
        final float[] VPMatrix = new float[16];

        Matrix.setLookAtM(captureViews[0], 0, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[1], 0, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[2], 0, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0f, 0.0f, 1.0f);
        Matrix.setLookAtM(captureViews[3], 0, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0f, 0.0f, -1.0f);
        Matrix.setLookAtM(captureViews[4], 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[5], 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0f, -1.0f, 0.0f);

        float ratio =  1.0f;
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 1, 50);

        // IMPORTANT: don't forget to set the viewport resolution
        GLES30.glViewport(0, 0, IRRADIANCEMAP_RESOLUTION, IRRADIANCEMAP_RESOLUTION);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);

        for (int i = 0; i < 6; ++i) {
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, mIrradianceCubemapHandle, 0);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
            Matrix.multiplyMM(VPMatrix, 0, projMatrix, 0, captureViews[i], 0);
            shader.setIrradianceCalcShader();
            drawToFramebuffer(shader.getCurrentShaderProgramHandle(), VPMatrix, mEnvCubemapHandle, 0.0f, true);
        }

        // Restore default
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void convoluteSpecularCubemap(ShaderManager shader)
    {
        final int[] specularCubemap = new int[1];

        GLES30.glGenFramebuffers(1, captureFBO, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);

        // Create a texture to store new cubemap with an empty texture bind
        GLES30.glGenTextures(1, specularCubemap, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, specularCubemap[0]);

        for(int i =0; i < 6; i++)
        {
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GLES30.GL_RGB16F,
                    SPECULARMAP_RESOLUTION, SPECULARMAP_RESOLUTION, 0,  GLES30.GL_RGB, GLES30.GL_FLOAT, null);
        }
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_CUBE_MAP);


        GLES30.glGenTextures(1, captureRBO, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, captureRBO[0]);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, SPECULARMAP_RESOLUTION , SPECULARMAP_RESOLUTION);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, captureRBO[0]);

        // Check if buffer was created
        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)!= GLES30.GL_FRAMEBUFFER_COMPLETE)
        {        Log.e("FrameBuffer"," Framebuffer wasn't created in skybox");       }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        mSpecularCubemapHandle = specularCubemap[0];


        // Generate 6 view matrices with FOV 90 to capture entire surface of cube's side
        final float[][] captureViews = new float[6][16];
        final float[] projMatrix = new float[16];
        final float[] VPMatrix = new float[16];

        Matrix.setLookAtM(captureViews[0], 0, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[1], 0, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[2], 0, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0f, 0.0f, 1.0f);
        Matrix.setLookAtM(captureViews[3], 0, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0f, 0.0f, -1.0f);
        Matrix.setLookAtM(captureViews[4], 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0f, -1.0f, 0.0f);
        Matrix.setLookAtM(captureViews[5], 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0f, -1.0f, 0.0f);

        float ratio =  1.0f;
        Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 1, 50);

        // IMPORTANT: don't forget to set the viewport resolution
        GLES30.glViewport(0, 0, SPECULARMAP_RESOLUTION, SPECULARMAP_RESOLUTION);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);

        final int maxMipLevel = 5;
        for(int mip = 0; mip < maxMipLevel; ++mip) {
            // resize framebuffer according to mip-level size
            int mipWidth = 128 / ((2 * mip == 0)? 1: 2*mip);
            int mipHeight = 128 / ((2 * mip == 0)? 1: 2*mip);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, captureRBO[0]);
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, mipWidth, mipHeight);
            GLES30.glViewport(0,0, mipWidth, mipHeight);

            float roughness = (float)mip / (float)(maxMipLevel - 1);

            for (int i = 0; i < 6; ++i) {
                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, mSpecularCubemapHandle, mip);
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
                Matrix.multiplyMM(VPMatrix, 0, projMatrix, 0, captureViews[i], 0);
                shader.setSpecularCalcShader();
                drawToFramebuffer(shader.getCurrentShaderProgramHandle(), VPMatrix, mEnvCubemapHandle,roughness, true);
            }
        }


        //-------------------------------------------------------------------------------------------
        // Restore default
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void convoluteBRDF(ShaderManager shader)
    {
        final int[] brdfLutTexture = new int[1];

        GLES30.glGenFramebuffers(1, captureFBO, 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);

        // Create a texture to store new cubemap with an empty texture bind
        GLES30.glGenTextures(1, brdfLutTexture, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, brdfLutTexture[0]);

        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGB16F,
                    BRDF_RESOLUTION, BRDF_RESOLUTION, 0,  GLES30.GL_RGB, GLES30.GL_FLOAT, null);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        
        GLES30.glGenTextures(1, captureRBO, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, captureRBO[0]);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, BRDF_RESOLUTION , BRDF_RESOLUTION);
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, captureRBO[0]);

        // Check if buffer was created
        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)!= GLES30.GL_FRAMEBUFFER_COMPLETE)
        {        Log.e("FrameBuffer"," Framebuffer wasn't created in skybox");       }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

        mBRDFLUTTextureHandle = brdfLutTexture[0];

        //-------------------------
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, captureFBO[0]);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, captureRBO[0]);
        GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT24, BRDF_RESOLUTION, BRDF_RESOLUTION);
        GLES30.glViewport(0, 0, BRDF_RESOLUTION, BRDF_RESOLUTION);

        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, mBRDFLUTTextureHandle, 0);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        shader.setBRDFCalcShader();
        mQuad.drawForBRDFcalc();

        // Restore default
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    }

    private void drawToFramebuffer(final int program, float[] mvpMatrix, final int map, float roughness, boolean isCubemap)
    {
        GLES30.glDepthMask( false);
            mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
            textureHandle = GLES30.glGetUniformLocation(program, "uCubeTexture");
            mRoughnessHandle = GLES30.glGetUniformLocation(program, "roughness");
            int resolutionHandle = GLES30.glGetUniformLocation(program, "resolution");


            if(isCubemap)
            {
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, map);
                GLES30.glUniform1i(textureHandle, 0);
            }else {
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, map);
                GLES30.glUniform1i(textureHandle, 0);
            }
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBOHandle);
            GLES30.glEnableVertexAttribArray(0);
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glUniform1f(mRoughnessHandle, roughness);
            GLES30.glUniform1f(resolutionHandle, (float)CUBEMAP_RESOLUTION);

            GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, VERTEX_COUNT);
            GLES30.glDisableVertexAttribArray(0);


        GLES30.glDepthMask(true);
    }



    public void draw(float deltaX, float deltaY, final int program, float[] vpMatrix, RotationStateEnum state, boolean isDraw)
    {
        GLES30.glDepthMask( false);
        if(isFirstCall) {
            mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
            textureHandle = GLES30.glGetUniformLocation(program, "uCubeTexture");

            isFirstCall = false;
        }

        rotateSky(deltaX, deltaY, state);
        Matrix.multiplyMM(mModelMatrix, 0, mModelInitMatrix, 0, mModelRotationMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, vpMatrix, 0, mModelMatrix, 0);

        if(isDraw) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, mEnvCubemapHandle);
            GLES30.glUniform1i(textureHandle, 0);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBOHandle);
            GLES30.glEnableVertexAttribArray(0);
            GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, VERTEX_COUNT);
            GLES30.glDisableVertexAttribArray(0);
        }

        GLES30.glDepthMask(true);

        if(mIsChangeHDR){
            loadHDR();
        }
        if(!mIsIrradHandleReady){
            if(mIsEnvHandleReady){
            convoluteIrradiance(mShaderManager); mIsIrradHandleReady = true; return;}
        }
        if(!mIsSpecHandleReady){
            if(mIsEnvHandleReady){
            convoluteSpecularCubemap(mShaderManager); mIsSpecHandleReady = true; return;}
        }
        if(!mIsBRDFLUTHandleReady){ convoluteBRDF(mShaderManager); mIsBRDFLUTHandleReady = true; return;}

        }

    public final int getEnvironmentCubemap(){ if(mIsEnvHandleReady) {return mEnvCubemapHandle;} else {return mTempEnvHandle;}}

    public final int getIrradianceCubemap(){if(mIsIrradHandleReady){return mIrradianceCubemapHandle;} else {return mTempIrradHandle;}}

    public final int getSpecularCubemap() {if(mIsSpecHandleReady){return mSpecularCubemapHandle;} else {return mTempSpecHandle;}}

    public final int getBRDFLUTTexture() {if(mIsBRDFLUTHandleReady){return mBRDFLUTTextureHandle;} else { return mTempBRDFLUTHandle;}}


    private void loadHDR(final int resourceID)
    {
        release();
        equirectMapToCubemap(mShaderManager, mContext,resourceID);
        mIsEnvHandleReady = true;
    }
    private void loadHDR()
    {
        release();
        equirectMapToCubemap(mShaderManager, mContext,mResourceList[mHDRToChange]);
        mIsEnvHandleReady = true;
        mIsChangeHDR = false;
    }

    public void SetHDR(final int number){
        if(mCurrentHDR != number){
            mIsChangeHDR = true;
            mHDRToChange = number;
            mCurrentHDR = number;
        }
    }


    public void release()
    {
        final int[] texturesToDelete = new int[]{mEnvCubemapHandle, mIrradianceCubemapHandle, mSpecularCubemapHandle, mBRDFLUTTextureHandle};
        GLES30.glDeleteTextures(texturesToDelete.length, texturesToDelete, 0);
        setIsMapsReady(false);
    }
    private void setIsMapsReady(boolean isReady)
    {
        mIsBRDFLUTHandleReady = isReady;
        mIsSpecHandleReady = isReady;
        mIsIrradHandleReady = isReady;
        mIsEnvHandleReady = isReady;
    }

    private void rotateSky(float deltaX, float deltaY, RotationStateEnum state)
    {
        if(state == RotationStateEnum.ROTATE_ENVIRONMENT)
        {
            if(mRotationHorizontal >= 6.28319f) { mRotationHorizontal += MathHelper.degToRad(deltaX) - 6.28319f;}
            else if( mRotationHorizontal <= -6.28319f) { mRotationHorizontal += MathHelper.degToRad(deltaX) + 6.28319f;}
            else{ mRotationHorizontal += MathHelper.degToRad(deltaX);}

            if(mRotationVertical >= 6.28319f) { mRotationVertical += MathHelper.degToRad(deltaY) - 6.28319f;}
            else if( mRotationVertical <= -6.28319f) { mRotationVertical += MathHelper.degToRad(deltaY) + 6.28319f;}
            else{ mRotationVertical += MathHelper.degToRad(deltaY);}

            MathHelper.quaternionFromAxisAngle( mRotationHorizontalQuaternion, 0.0f, 1.0f, 0.0f, mRotationHorizontal);
            MathHelper.quaternionFromAxisAngle(mRotationVerticalQuaternion, 1.0f, 0.0f, 0.0f, mRotationVertical);
            //MathHelper.quaternionMultyply(mRotationHorizontalQuaternion, mRotationVerticalQuaternion, mRotationQuaternion);

            MathHelper.quaternionToMatrix(mRotationHorizontalQuaternion, mModelRotationMatrix);
        }
    }

    public float[] GetRotationMatrix()    { return mModelRotationMatrix; }

    public void ResetRotation()
    {
        mRotationVertical = 0.0f;
        mRotationHorizontal = 0.0f;
        rotateSky(0.0f, 0.0f, RotationStateEnum.ROTATE_ENVIRONMENT);
    }
}
