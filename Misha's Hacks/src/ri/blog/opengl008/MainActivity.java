package ri.blog.opengl008;


import com.airhockey.android.AirHockeyRenderer;

import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    /**
     * Hold a reference to our GLSurfaceView
     */
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	 
	    super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);

        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager =  (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 =
            configurationInfo.reqGlEsVersion >= 0x20000
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                 && (Build.FINGERPRINT.startsWith("generic")
                  || Build.FINGERPRINT.startsWith("unknown")
                  || Build.MODEL.contains("google_sdk")
                  || Build.MODEL.contains("Emulator")
                  || Build.MODEL.contains("Android SDK built for x86")));

        final GLRenderer glRenderer = new GLRenderer(this);
        
        if (!supportsEs2) {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                Toast.LENGTH_LONG).show();
            return;
        }
        // ...
        // Request an OpenGL ES 2.0 compatible context.
        glSurfaceView.setEGLContextClientVersion(2);

        // Assign our renderer.
        glSurfaceView.setRenderer(glRenderer);
        rendererSet = true;

        glSurfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event == null) {    
                    return false;    
                }   
                final float eventX = event.getX();
                final float eventY = event.getY();
                // Convert touch coordinates into normalized device
                // coordinates, keeping in mind that Android's Y
                // coordinates are inverted.
                final float normalizedX =  (eventX / (float) v.getWidth()) * 2 - 1;
                final float normalizedY =  -((eventY / (float) v.getHeight()) * 2 - 1);

                
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                           // glRenderer.handleTouchPress(normalizedX, normalizedY);
                             glRenderer.handleTouchPress(eventX, eventY);
                        }
                    });
                } 
                /*
                else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            glRenderer.handleTouchDrag(
                                normalizedX, normalizedY);
                        }
                    });
                }                    
*/
                return true;                    

            }
        });

        //setContentView(glSurfaceView);
	    //================================== ORIGINAL CODE
	    
		// Turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
 
		
		// Fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  
        // We create our Surfaceview for our OpenGL here.
        //glSurfaceView = new GLSurf(this);
        
        // Set our view.	
	setContentView(R.layout.activity_main); 
		
		// Retrieve our Relative layout from our main layout we just set to our view.
       RelativeLayout layout = (RelativeLayout) findViewById(R.id.gamelayout); 
        
        // Attach our surfaceview to our relative layout from our main layout.
        RelativeLayout.LayoutParams glParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
      layout.addView(glSurfaceView, glParams); 
      
     
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (rendererSet) {
		glSurfaceView.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (rendererSet) {
		glSurfaceView.onResume();}
		
	}

}
