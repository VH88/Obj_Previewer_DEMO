package vh.objpreviewerdemo.Shader;

import android.opengl.GLES30;
import android.opengl.Matrix;

import vh.objpreviewerdemo.Utils.MathHelper;

/**
 * Created by User on 2/14/2018.
 */

public class Light {
    // Color represented in RGB * Watts. Should not be 0.0f

    private float mRotationHorizontal;
    private float mRotationVertical;
    private float mDistance;

    private final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInEyeSpace = new float[4];

    private final float[] mRotateHorizontalQuaternion = new float[4];
    private final float[] mRotateVerticalQuaternion = new float[4];
    private final float[] mRotationQuaternion = new float[4];
    private final float[] mLightColor = new float[4];

    private final float[] mLightModelMatrix = new float[16];
    private final float[] mLightRotationMatrix = new float[16];
    private final float[] mLightTranslationMatrix = new float[16];
    private final float[] mLightMVPMatrix = new float[16];

    public Light(float rotaHorizontalInDeg, float rotVerticalInDeg, float distance)
    {
        mRotationHorizontal = MathHelper.degToRad(rotaHorizontalInDeg);
        mRotationVertical = MathHelper.degToRad(rotVerticalInDeg);
        mDistance = distance;


        MathHelper.quaternionFromAxisAngle(mRotateHorizontalQuaternion, 0.0f, 1.0f, 0.0f, mRotationHorizontal);
        MathHelper.quaternionFromAxisAngle(mRotateVerticalQuaternion, 1.0f, 0.0f, 0.0f, mRotationVertical);
        MathHelper.quaternionMultyply(mRotateVerticalQuaternion, mRotateHorizontalQuaternion, mRotationQuaternion);
        MathHelper.quaternionToMatrix(mRotationQuaternion, mLightRotationMatrix);

        Matrix.setIdentityM(mLightTranslationMatrix, 0);
        Matrix.translateM(mLightTranslationMatrix, 0, 0.0f, 0.0f, mDistance);

        Matrix.multiplyMM(mLightModelMatrix, 0, mLightRotationMatrix, 0, mLightTranslationMatrix, 0);

    }
    public void draw(final int program,final float[] mvpMatrix)
    {
        final int pointMVPMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
/*
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 2.0f, -2.0f);
        Matrix.rotateM(mLightModelMatrix, 0, 10.7f, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 1.0f, 7.0f);
        Matrix.multiplyMM(mLightMVPMatrix,0, mvpMatrix, 0, mLightModelMatrix, 0);
*/
        Matrix.multiplyMM(mLightMVPMatrix,0, mvpMatrix, 0, mLightModelMatrix, 0);

        GLES30.glVertexAttrib3f(0, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2] );
        GLES30.glDisableVertexAttribArray(0);

        GLES30.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mLightMVPMatrix, 0);
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, 1);
    }

    public void UpdateLightPosInEyeSpace(  float[] viewMatrix)
    {
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, viewMatrix, 0, mLightPosInWorldSpace, 0);

    }

    public float[] GetLighPosInEysSpace()    {
        return mLightPosInEyeSpace;
    }

    public float[] GetLightColor()    {
        return mLightColor;
    }

    public void SetColor(final float[] color)    {
        System.arraycopy(color, 0, mLightColor, 0, 4);
    }


}
