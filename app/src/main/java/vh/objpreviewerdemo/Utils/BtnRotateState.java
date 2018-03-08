package vh.objpreviewerdemo.Utils;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.Toast;

import vh.objpreviewerdemo.OpenGLMain;
import vh.objpreviewerdemo.R;

/**
 * Created by User on 2/21/2018.
 */
/** Reference used:
 *  https://stackoverflow.com/questions/4823851/multi-state-toggle-button */
public class BtnRotateState extends AppCompatButton {

    private int _state;

    private static final int[] STATE_ONE_SET = {R.attr.state_rot_object};
    private static final int[] STATE_TWO_SET = {R.attr.state_rot_env};
    private static final int[] STATE_THREE_SET = {R.attr.state_rot_scene};

    public BtnRotateState(Context context)
    {
        super(context);
        _state = 0;
        this.setBackgroundResource(R.drawable.btn_rotation_toggle_scene);
    }
    public BtnRotateState(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        _state = 0;
        this.setBackgroundResource(R.drawable.btn_rotation_toggle_scene);
    }
    public BtnRotateState(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        _state = 0;
        this.setBackgroundResource(R.drawable.btn_rotation_toggle_scene);
    }

    @Override
    public boolean performClick()
    {
        nextState();
        return super.performClick();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);
        if(_state == 0)
        {
            mergeDrawableStates(drawableState, STATE_ONE_SET);
        }
        else if(_state == 1)
        {
            mergeDrawableStates(drawableState, STATE_TWO_SET);
        }
        else if(_state == 2)
        {
            mergeDrawableStates(drawableState, STATE_THREE_SET);
        }
        return drawableState;
    }

    public void setState(int state)
    {
        if((state > -1) && (state < 3))
        {
            _state = state;
            setButtonDrawable(_state);
            showShortToast();
        }
    }
    public int getState()
    {
        return _state;
    }

    public void nextState()
    {
        _state++;

        if(_state > 2)
        {
            _state = 0;
        }

        setButtonDrawable(_state);
        showShortToast();
    }

    public void prevState()
    {
        _state--;
        if(_state < 0)
        {
            _state = 2;
        }
        setButtonDrawable(_state);
        showShortToast();
    }

    private void setButtonDrawable(int s)
    {
        switch (s)
        {
            case 0: this.setBackgroundResource(R.drawable.btn_rotation_toggle_scene);
                break;
            case 1: this.setBackgroundResource(R.drawable.btn_rotation_toggle_env);
                break;
            case 2: this.setBackgroundResource(R.drawable.btn_rotation_toggle_obj);
                break;
        }
    }

    private void showShortToast()
    {
        switch (_state)
        {
            case 0: Toast.makeText(this.getContext(), "Rotate Scene", Toast.LENGTH_SHORT).show();
                break;
            case 1: Toast.makeText(this.getContext(), "Rotate Environment", Toast.LENGTH_SHORT).show();
                break;
            case 2: Toast.makeText(this.getContext(), "Rotate Object", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
