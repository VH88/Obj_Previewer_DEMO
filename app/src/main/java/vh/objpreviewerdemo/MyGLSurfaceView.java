package vh.objpreviewerdemo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by User on 2/20/2018.
 */

class MyGLSurfaceView extends GLSurfaceView
{
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.0f;
    private boolean mIsScaling = false;

    private  MyGLRenderer mRenderer;
    private float mPrevX;
    private float mPrevY;
    private float mDensity;

    private Object lock = new Object();

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            mScaleFactor *= detector.getScaleFactor();
            mIsScaling = true;
            invalidate();
            return true;
        }
    }

    public MyGLSurfaceView(Context context )    {        super(context);    }
    public MyGLSurfaceView(Context context , AttributeSet attrs)    {      super(context, attrs);    }

    public void setRenderer(MyGLRenderer renderer, Context context, float density)
    {
        mRenderer = renderer;
        mDensity = density;
        super.setRenderer(renderer);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

    }


    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        mScaleDetector.onTouchEvent(e);

        //Log.e("Touch Event", Integer.toString(e.getAction()) );
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                if(mIsScaling)
                {
                    if(mScaleFactor < 0.26f){mScaleFactor = 0.26f;}
                    else if(mScaleFactor > 2.23f){mScaleFactor = 2.23f;}

                    mRenderer.SetScalingFactor(mScaleFactor, true);
                    //Log.e("Scale detector", Float.toString(mScaleFactor));
                }
                else
                {
                    float deltaX = (x - mPrevX) / mDensity * -0.5f;
                    float deltaY = (y - mPrevY) / mDensity * -0.5f;

                    mRenderer.UpdateDeltas(deltaX, deltaY);

                }
                // This works together with setRenderMode Dirty
                //requestRender();
                break;
            case MotionEvent.ACTION_UP:
                //mScaleFactor = 1.0f;
                mIsScaling = false;
                mRenderer.SetScalingFactor(mScaleFactor, false);
                //mRenderer.UpdateDeltas(0.0f, 0.0f);
                break;

            case MotionEvent.ACTION_DOWN:
                //mRenderer.onActionDown();
                break;

        }

        mPrevX = x;
        mPrevY = y;

        return  true;
    }

    public void ResetScale()
    {
        synchronized (lock)
        {
            mScaleFactor = 1.0f;
            mRenderer.SetScalingFactor(1.0f, true);
        }
    }
}
