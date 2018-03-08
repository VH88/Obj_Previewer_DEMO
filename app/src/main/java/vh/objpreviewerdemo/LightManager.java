package vh.objpreviewerdemo;

import vh.objpreviewerdemo.Shader.Light;

/**
 * Created by User on 2/22/2018.
 */

public class LightManager {
    /**
     *  Light data is packed in a single array that holds positions and colors
     */
    // 3 floats per light XYZ
    private final int LIGHT_DATA_POSITIONS = 4;
    private final int LIGHT_DATA_COLORS = 4;
    private final int LIGHT_DATA_LENGTH;

    private Light[] mLights;
    private final int mLightCount;
    private final float[] mLightData;

    public LightManager(int lightCount)
    {
        mLights = new Light[lightCount];
        for(int i = 0; i < lightCount; i++){ mLights[i] = null; }
        mLightCount = lightCount;
        LIGHT_DATA_LENGTH = LIGHT_DATA_POSITIONS + LIGHT_DATA_COLORS ;
        mLightData = new float[LIGHT_DATA_LENGTH * lightCount];
    }

    /** Initialize after SetLight() */
    public void Initialize()
    {
        // Generate Arrays with uniform light data.
        int count = 0;
        for (int i = 0; i < mLightCount; i++)
        {
            System.arraycopy(mLights[i].GetLighPosInEysSpace(),0, mLightData, i* LIGHT_DATA_LENGTH, LIGHT_DATA_POSITIONS);
            System.arraycopy(mLights[i].GetLightColor(),0, mLightData, i* LIGHT_DATA_LENGTH + LIGHT_DATA_POSITIONS, LIGHT_DATA_COLORS);
        }
    }


    public void draw(final int program, final float[] mvpMatrix){
        for(int i = 0; i < mLightCount; i++){
            if(mLights[i] != null) {
                mLights[i].draw(program, mvpMatrix);
            }
        }
    }

    /** Set Light Before Initializa()*/
    public void SetLight(int number,float horizontalRotation, float verticalRotation,float distance, final float[] color)    {
        mLights[number] = new Light(horizontalRotation, verticalRotation, distance);
        mLights[number].SetColor(color);
    }

    public int GetLightCount(){ return mLightCount; }

    public float[] GetLightUniformArray()    {
        return mLightData;
    }

    public void UpdateLightPosInEyeSpace(final float[] viewMatrix)    {
        int count = 0;
        for(int i = 0; i < mLightCount; i++)        {
            mLights[i].UpdateLightPosInEyeSpace(viewMatrix);
            System.arraycopy(mLights[i].GetLighPosInEysSpace(),0, mLightData, i* LIGHT_DATA_LENGTH, LIGHT_DATA_POSITIONS);
        }
    }
}
