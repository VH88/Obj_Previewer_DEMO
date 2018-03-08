package vh.objpreviewerdemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import vh.objpreviewerdemo.Utils.BtnRotateState;

/**
 * Created by User on 2/13/2018.
 */

enum RotationStateEnum
{
    ROTATE_OBJECT,
    ROTATE_ENVIRONMENT,
    ROTATE_SCENE
}

public class OpenGLMain extends Activity{
    private MyGLSurfaceView mGLView;
    private MyGLRenderer mRenderer;

    private LinearLayout mLayout;

    private BtnRotateState switchRotationMode;
    private ToggleButton toggle_show_env;
    private ImageButton reset_scene;
    private ImageButton prev_scene;
    private ImageButton next_scene;
    private ToggleButton settings;
    private LinearLayout settings_layout;
    private LinearLayout environment_layout;
    private TextView select_environment;

    private TextView hdr_gravel_plaze;
    private TextView hdr_charles_river;
    private TextView hdr_ice_lake;
    private TextView hdr_old_industrial;
    private TextView hdr_popcorn_lobby;
    private TextView hdr_tropical_forest;
    private TextView hdr_winter_forest;




    //--------------------- ON CREATE ----------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        //------------------------------------------------------------------------------
        //---------------- INITIALIZE --------------------------------------------------
        //--------------------------------------------------------------------------------
        super.onCreate(savedInstanceState);

        // Make full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        // Set Context ----------------------------------------
        setContentView(R.layout.main_activity);
        mGLView = (MyGLSurfaceView) findViewById(R.id.OpenGL);

        mGLView.setEGLContextClientVersion(3);


        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        final int width = displayMetrics.widthPixels;
        final int height = displayMetrics.heightPixels;


        mRenderer = new MyGLRenderer(this, this, mGLView, width, height);
        mGLView.setRenderer(mRenderer, this, displayMetrics.density);

        //------------------------------------------------------------------------------
        //---------------- SET BUTTONS --------------------------------------------------
        //--------------------------------------------------------------------------------
        settings_layout = (LinearLayout)findViewById(R.id.settings_layout);
        environment_layout = (LinearLayout)findViewById(R.id.environment_selection_layout);

        mLayout = (LinearLayout) findViewById(R.id.layout);
 /*       mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Hide menus
                settings_layout.setVisibility(View.GONE);
                return false;
            }
        });*/



        // Reset Button -------------------------------------------------------------------
        reset_scene = (ImageButton)findViewById(R.id.reset_rotation);
        reset_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.ResetScene();
                mGLView.ResetScale();
                Toast.makeText(OpenGLMain.this, "Reset Scene", Toast.LENGTH_SHORT).show();
            }
        });

        // Show Environment toggle -------------------------------------------------------------
        toggle_show_env = (ToggleButton)findViewById(R.id.toggleEnvShow);
        toggle_show_env.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    mRenderer.SetShowSkybox(b);
                    Toast.makeText(OpenGLMain.this, "Show Environment", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mRenderer.SetShowSkybox(b);
                    Toast.makeText(OpenGLMain.this, "Hide Environment", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Rotate Mode -------------------------------------------------------------------------
        switchRotationMode = (BtnRotateState)findViewById(R.id.rotate_toggle_state);
        switchRotationMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = switchRotationMode.getState();

                try
                {
                    switch (state)
                    {
                        case 0: {
                            mRenderer.SetRotationState(RotationStateEnum.ROTATE_SCENE);
                            break;
                        }
                        case 1: {
                            mRenderer.SetRotationState(RotationStateEnum.ROTATE_ENVIRONMENT);
                            break;
                        }
                        case 2: {
                            mRenderer.SetRotationState(RotationStateEnum.ROTATE_OBJECT);
                            break;
                        }
                        // should never occur
                        default: break;
                    }
                }
                catch (Exception e)
                {
                    Log.e("OpenGLMain", "ERROR:onCreate:BtnRotateState:onClick()::  " + e.getMessage());
                }
            }
        });

        // Prev Scene --------------------------------------------------------------------------
        prev_scene = (ImageButton)findViewById(R.id.btn_left_arrow);
        prev_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.PrevScene();
            }
        });

        // Next Scene --------------------------------------------------------------------------
        next_scene = (ImageButton)findViewById(R.id.btn_right_arrow);
        next_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.NextScene();
            }
        });



        // Settings Button ------------------------------------------------------
        settings = (ToggleButton)findViewById(R.id.btn_settings);
        settings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    settings_layout.setVisibility(View.VISIBLE);
                }
                else {
                    settings_layout.setVisibility(View.GONE);
                    environment_layout.setVisibility(View.GONE);
                }
            }
        });

        select_environment = (TextView)findViewById(R.id.select_environment);
        select_environment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                environment_layout.setVisibility(View.VISIBLE);
            }
        });

        //----------------------------------------------
        // Set HDR

        hdr_gravel_plaze = (TextView) findViewById(R.id.gravel_plaza);
        hdr_gravel_plaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.SetHDR(0);
            }
        });

        hdr_charles_river = (TextView) findViewById(R.id.charles_river);
        hdr_charles_river.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.SetHDR(1);
            }
        });

        hdr_ice_lake = (TextView) findViewById(R.id.ice_lake);
        hdr_ice_lake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.SetHDR(2);
            }
        });


        hdr_old_industrial = (TextView) findViewById(R.id.old_industrial);
        hdr_old_industrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.SetHDR(3);
            }
        });

        hdr_popcorn_lobby = (TextView) findViewById(R.id.popcorn_lobby);
        hdr_popcorn_lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.SetHDR(4);
            }
        });

        hdr_tropical_forest = (TextView) findViewById(R.id.tropical_forest);
        hdr_tropical_forest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.SetHDR(5);
            }
        });

        hdr_winter_forest = (TextView) findViewById(R.id.winter_forest);
        hdr_winter_forest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRenderer.SetHDR(6);
            }
        });

    }






    @Override
    protected void onResume() {
        // The activity must call the GL surface view's onResume() on activity
        // onResume().
        super.onResume();
        mGLView.onResume();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    @Override
    protected void onPause() {
        // The activity must call the GL surface view's onPause() on activity
        // onPause().
        super.onPause();
        mGLView.onPause();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }
}
