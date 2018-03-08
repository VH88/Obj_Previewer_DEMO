package vh.objpreviewerdemo.Utils;

import android.opengl.Matrix;

import java.io.IOException;

/**
 * Created by User on 2/18/2018.
 */

//TODO: read about programming intrinsics to write more optimized code
public class MathHelper {

    public static float degToRad( float deg)
    {        return  deg * (float)Math.PI / 180.0f;  }

    public static float radToDeg(float rad)
    { return  rad * 180.0f / (float)Math.PI;}

    public static void quaternionToMatrix(final float[] quaternion, final float[] matrix)
    {
        if(quaternion.length != 4){ throw new RuntimeException("Quaternion float array is not the right size");}
        else if(matrix.length != 16){ throw new RuntimeException("Matrix float array is not the right size");}


        final float xx = quaternion[0] * quaternion[0];
        final float xy = quaternion[0] * quaternion[1];
        final float xz = quaternion[0] * quaternion[2];
        final float xw = quaternion[0] * quaternion[3];
        final float yy = quaternion[1] * quaternion[1];
        final float yz = quaternion[1] * quaternion[2];
        final float yw = quaternion[1] * quaternion[3];
        final float zz = quaternion[2] * quaternion[2];
        final float zw = quaternion[2] * quaternion[3];

        matrix[0] = 1.0f - 2.0f * (yy + zz);
        matrix[1] = 2.0f * (xy - zw);
        matrix[2] = 2.0f * (xz + yw);
        matrix[3] = 0.0f;

        matrix[4] = 2.0f * (xy + zw);
        matrix[5] = 1.0f - 2.0f * (xx + zz);
        matrix[6] = 2.0f * (yz - xw);
        matrix[7] = 0.0f;

        matrix[8] = 2.0f * (xz - yw);
        matrix[9] = 2.0f * (yz + xw);
        matrix[10] = 1.0f - 2.0f * (xx + yy);
        matrix[11] = 0.0f;

        matrix[12] = 0.0f;
        matrix[13] = 0.0f;
        matrix[14] = 0.0f;
        matrix[15] = 1.0f;

    }

    public static void quaternionFromAxisAngle(final float[] quaternion, float x, float y ,float z, float w)
    {   // w must be in radians
        if(quaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}
        float sinW = (float)Math.sin(w * 0.5f);
        quaternion[0] = sinW* x;
        quaternion[1] = sinW * y;
        quaternion[2] = sinW * z;
        quaternion[3] = (float)Math.cos(w * 0.5f);
    }

    public static void quaternionIdentity(final  float[] quaternion)
    {
        if(quaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}

        quaternion[0] = 0.0f;
        quaternion[1] = 0.0f;
        quaternion[2] = 0.0f;
        quaternion[3] = 1.0f;
    }

    public static void quaternionMultyply( final float[] thisQuaternion, final float[] otherQuaternion, final float[] newQuaternion)
    {
        if(thisQuaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}
        else if(otherQuaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}
        else if(newQuaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}


        float qT, qX, qY, qZ;
        float aT, aX, aY, aZ;
        float bT, bX, bY, bZ;

        aT = thisQuaternion[3];
        aX = thisQuaternion[0];
        aY = thisQuaternion[1];
        aZ = thisQuaternion[2];

        bT = otherQuaternion[3];
        bX = otherQuaternion[0];
        bY = otherQuaternion[1];
        bZ = otherQuaternion[2];

        qT = (aT * bT) - (aX * bX) - (aY * bY) - (aZ * bZ);
        qX = (aT * bX) + (aX * bT) + (aY * bZ) - (aZ * bY);
        qY = (aT * bY) - (aX * bZ) + (aY * bT) + (aZ * bX);
        qZ = (aT * bZ) + (aX * bY) - (aY * bX) + (aZ * bT);

        newQuaternion[0] = qX;
        newQuaternion[1] = qY;
        newQuaternion[2] = qZ;
        newQuaternion[3] = qT;

    }

    public static void quaternionRotateVector(final float[] q, final float[] vector, final float[] result)
    {
        if(q.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}
        else if(vector.length != 4) {throw new RuntimeException("vector float array is not the right size");}
        else if(result.length != 4) {throw new RuntimeException("vector float array is not the right size");}

        float[] m = new float[16];
        MathHelper.quaternionToMatrix(q, m);
        Matrix.multiplyMV(result, 0, m,0, vector, 0);
    }

    public static void quaternionCopy(final float[] sourceQuaternion, final float[] destinationQuaternion)
    {
        if(sourceQuaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}
        else if(destinationQuaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}

        destinationQuaternion[0] = sourceQuaternion[0];
        destinationQuaternion[1] = sourceQuaternion[1];
        destinationQuaternion[2] = sourceQuaternion[2];
        destinationQuaternion[3] = sourceQuaternion[3];

    }

    public static void quaternionNormalize(final float[] sourceQuaternion, final float[] destinationQuaternion)
    {
        if(sourceQuaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}
        else if(destinationQuaternion.length != 4) {throw new RuntimeException("Quaternion float array is not the right size");}

        final float magnitude =  (float)Math.sqrt( sourceQuaternion[0] * sourceQuaternion[0] + sourceQuaternion[1] * sourceQuaternion[1]+
                sourceQuaternion[2] * sourceQuaternion[2]  + sourceQuaternion[3] * sourceQuaternion[3]);

        if(magnitude !=0)
        {
            final float _magnitude = 1.0f / magnitude;
            destinationQuaternion[0] = sourceQuaternion[0] * _magnitude;
            destinationQuaternion[1] = sourceQuaternion[1] * _magnitude;
            destinationQuaternion[2] = sourceQuaternion[2] * _magnitude;
            destinationQuaternion[3] = sourceQuaternion[3] * _magnitude;
        }
        //If magnitude is 0.0f, to avoid division by zero set to identity quaternion and preserve sign
        else
        {
            destinationQuaternion[0] = 0.0f;
            destinationQuaternion[1] = 0.0f;
            destinationQuaternion[2] = 0.0f;
            if(sourceQuaternion[3] >0.0f) { destinationQuaternion[3] = 1.0f;}
                                        else{destinationQuaternion[3] = -1.0f;}
        }

    }

    public static float vectorDot(final float[] v1, final float[] v2)
    {
        if(v1.length != 4) {throw new RuntimeException("Vector float array is not the right size");}
        else if(v2.length != 4) {throw new RuntimeException("Vector float array is not the right size");}

        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }
    public static float vectorDot( float v1x,  float v1y,  float v1z,  float v2x,  float v2y,  float v2z)
    {
        return v1x * v2x + v1y * v2y + v1z * v2z;
    }

    public static float[] vectorCross(float v1x, float v1y, float v1z, float v2x, float v2y, float v2z)
    {
        final float[] cross = new float[4];

        cross[0] = v1y * v2z - v1z * v2y;
        cross[1] = v1x * v2z - v1z * v2x;
        cross[2] = v1x * v2y - v1y * v2x;
        cross[3] = 0.0f;
        return cross;
    }

    public static float vectorMagnitude( final float[] v)
    {
        return (float)Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
    public static float[] vectorCross(final float[] v1, final float[] v2)
    {
        final float[] cross = new float[4];

        cross[0] = v1[1] * v2[2] - v1[2] * v2[1];
        cross[1] = v1[0] * v2[2] - v1[2] * v2[0];
        cross[2] = v1[0] * v2[1] - v1[1] * v2[0];
        cross[3] = v1[3];
        return cross;
    }

    public static void vectorNormalize(final float[] v)
    {
        float mag = MathHelper.vectorMagnitude(v);
        mag = 1.0f / mag;
        v[0] *= mag;
        v[1] *= mag;
        v[2] *= mag;
    }
}

// Quaternion vector multiply
// v' = q * v * conjugate(q)

// Author: Fabian Giesen
//t = 2 * cross(q.xyz, v)
//v' = v + q.w * t + cross(q.xyz, t)
