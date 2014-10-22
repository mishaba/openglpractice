/***
 * Excerpted from "OpenGL ES for Android",
***/
package com.airhockey.android;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class AirHockeyActivity extends Activity {
    /**
     * Hold a reference to our GLSurfaceView
     */
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new GLSurfaceView(this);

        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager = 
            (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager
            .getDeviceConfigurationInfo();
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

        final AirHockeyRenderer airHockeyRenderer = new AirHockeyRenderer(this);
        
        if (!supportsEs2) {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                Toast.LENGTH_LONG).show();
            return;
        }
        // ...
        // Request an OpenGL ES 2.0 compatible context.
        glSurfaceView.setEGLContextClientVersion(2);

        // Assign our renderer.
        glSurfaceView.setRenderer(airHockeyRenderer);
        rendererSet = true;

        glSurfaceView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event == null) {    
                    return false;    
                }   
                // Convert touch coordinates into normalized device
                // coordinates, keeping in mind that Android's Y
                // coordinates are inverted.
                final float normalizedX = 
                    (event.getX() / (float) v.getWidth()) * 2 - 1;
                final float normalizedY = 
                    -((event.getY() / (float) v.getHeight()) * 2 - 1);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            airHockeyRenderer.handleTouchPress(
                                normalizedX, normalizedY);
                        }
                    });
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            airHockeyRenderer.handleTouchDrag(
                                normalizedX, normalizedY);
                        }
                    });
                }                    

                return true;                    

            }
        });

        setContentView(glSurfaceView);
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
            glSurfaceView.onResume();
        }
    }
}