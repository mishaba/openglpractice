package ri.blog.opengl008;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

import com.airhockey.android.programs.TextureShaderProgram;
import com.airhockey.android.util.TextureHelper;


public class GLRenderer implements Renderer {
    private static final String TAG = "GLRenderer";
    // Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];

    
    public MusicSheet musicsheet;
    
    public TextManager tm;
    // Our screenresolution
    float	mScreenWidth = 1280;
    float	mScreenHeight = 768;
    float 	ssu = 1.0f;
    float 	ssx = 1.0f;
    float 	ssy = 1.0f;
    float 	swp = 320.0f;
    float 	shp = 480.0f;

    // Application coordinates
    private final float xAppRes = 1280.0f;
    private final float yAppRes = 768.0f;

    // Misc
    Context mContext;
    long mLastTime;
    int mProgram;

    // Text movement hack
    float mTextX = 10f;
    float mTextY = 10f;

    // Programs and such
    TextureShaderProgram mImageProgram;
    TextureShaderProgram mText2DProgram;

    long countElapsed = 0;
    long totalElapsed = 0;

    public GLRenderer(Context c)
    {
        mContext = c;
   
        
        musicsheet = new MusicSheet();
        mLastTime = System.currentTimeMillis() + 100;
    }

    public void onPause()
    {
        /* Do stuff to pause the renderer */
    }

    public void onResume()
    {
        /* Do stuff to resume the renderer */
        mLastTime = System.currentTimeMillis();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
       
        // Get the current time
        long now = System.currentTimeMillis();

        // We should make sure we are valid and sane
        if (mLastTime > now) return;

        // Get the amount of time the last frame took.
        long elapsed = now - mLastTime;

        // Update our example
        //UpdateSprite();

        // Render our example
        Render(mtrxProjectionAndView);

        // Render the text
        /*
        if(tm!=null)
            tm.Draw(mtrxProjectionAndView);
*/
        // Save the current time to see how long it took :).
        mLastTime = now;
        
        countElapsed++;
        totalElapsed+= elapsed;
        
        Log.w(TAG,"Elapsed "+elapsed + " Average: "+(totalElapsed/countElapsed));
    }

    private void Render(float[] m) {

        musicsheet.Draw(m, mImageProgram);
  

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        // We need to know the current width and height.
        mScreenWidth = width;
        mScreenHeight = height;

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);

        // Clear our matrices
        for(int i=0;i<16;i++)     {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);

        // Setup our scaling system
        SetupScaling();
        
        mLastTime = System.currentTimeMillis() + 100;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
Log.w(TAG,"on Surface Created");

        mImageProgram = new TextureShaderProgram(mContext,
            R.raw.image_vertex_shader, R.raw.image_fragment_shader,
            GL_TEXTURE0);

        mText2DProgram = new TextureShaderProgram(mContext, R.raw.text2d_vertex_shader,
            R.raw.text2d_fragment_shader,
            GL_TEXTURE1);

        // Set our shader programm
        mImageProgram.useProgram();


        // Setup our scaling system
        SetupScaling();
        // Create the triangles
        musicsheet.Setup(10000, swp, shp, ssu);


        // Retrieve our image from resources.
        int id = mContext.getResources().getIdentifier("drawable/textureatlas", 
            null, mContext.getPackageName());
        int  idText = mContext.getResources().getIdentifier("drawable/font",
            null, mContext.getPackageName());

        int textureImage = TextureHelper.loadTexture(mContext, id, GL_TEXTURE0); 
        int textureText = TextureHelper.loadTexture(mContext, idText, GL_TEXTURE1);  


        // Create our texts
        SetupText(mText2DProgram); 

        // Set the clear color to black
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1);   

        GLES20.glEnable(GL_BLEND);
        GLES20.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
Log.w(TAG,"Done on SurfaceCreated");
    }

    public void SetupText(TextureShaderProgram program) 	{
        // Create our text manager
        tm = new TextManager(program);

        // Pass the uniform scale
        tm.setUniformscale(ssu);

        // Create our new textobject
        TextObject txt = new TextObject("hello AshHB", mTextX, mTextY);

        // Add it to our manager
        tm.addText(txt);

        // Prepare the text for rendering
        tm.PrepareDraw();
    }

    // Misha's ugly hack: 
    public void UpdateText() 	    {

        TextObject txt = new TextObject("hello AshHA", mTextX, mTextY);

        tm.updateText(txt);

        // Prepare the text for rendering
        tm.PrepareDraw();
    }

    public void SetupScaling()
    {
        // The screen resolutions
        swp = (int) (mContext.getResources().getDisplayMetrics().widthPixels);
        shp = (int) (mContext.getResources().getDisplayMetrics().heightPixels);

        // Orientation is assumed portrait
        ssx = swp / xAppRes;
        ssy = shp / yAppRes;

        // Get our uniform scaler
        if(ssx > ssy)
            ssu = ssy;
        else
            ssu = ssx;
        Log.w(TAG,"SSX:"+ssx+" SSY:"+ssy+" SWP:"+swp+" SHP:"+shp);
    }
    public void handleTouchPress(float eventX, float eventY) {


        // Get the half of screen value
        int screenhalf = (int) (mScreenWidth / 2);
        int screenheightpart = (int) (mScreenHeight / 3);
        if(eventX<screenhalf) 	{
            Log.w(TAG,"Left ");
            mTextX += 10;
        }
        else {
            Log.w(TAG,"Right ");
            mTextX -= 10;
        }
        // Left screen touch
        if(eventY < screenheightpart)  {
            Log.w(TAG," Bottom");
            mTextY += 10;
        }

        else if(eventY < (screenheightpart*2)) {
            Log.w(TAG," Middle");
        }
        else {
            Log.w(TAG," Top");
            mTextY -= 10;
        }

        if (mTextX < 0) { // clamp min
            mTextX = 0;
        }
        if (mTextY < 0) { // clamp min
            mTextY = 0;
        }
        UpdateText(); 
    }




}
