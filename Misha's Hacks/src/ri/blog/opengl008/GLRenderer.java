package ri.blog.opengl008;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.airhockey.android.util.LoggerConfig;
import com.airhockey.android.util.TextResourceReader;
import com.airhockey.android.util.TextureHelper;
import com.airhockey.android.programs.TextureShaderProgram;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import android.util.Log;


public class GLRenderer implements Renderer {
    private static final String TAG = "GLRenderer";
	// Our matrices
	private final float[] mtrxProjection = new float[16];
	private final float[] mtrxView = new float[16];
	private final float[] mtrxProjectionAndView = new float[16];
	
	// Geometric variables
	public static float vertices[];
	public static short indices[];
	public static float uvs[];
	public FloatBuffer vertexBuffer;
	public ShortBuffer drawListBuffer;
	public FloatBuffer uvBuffer;
	
	public TextManager tm;
	// Our screenresolution
	float	mScreenWidth = 1280;
	float	mScreenHeight = 768;
	float 	ssu = 1.0f;
	float 	ssx = 1.0f;
	float 	ssy = 1.0f;
	float 	swp = 320.0f;
	float 	shp = 480.0f;

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
	
   
	public GLRenderer(Context c)
	{
		mContext = c;
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
		if(tm!=null)
			tm.Draw(mtrxProjectionAndView);
		
		// Save the current time to see how long it took :).
        mLastTime = now;
		
	}
	
	private void Render(float[] m) {
		
		// GLES20.glUseProgram(mImageProgram.program);
		 mImageProgram.useProgram();
		 
		// clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // get handle to vertex shader's vPosition member and add vertices
	    int mPositionHandle = GLES20.glGetAttribLocation(mImageProgram.program, "vPosition");
	    GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	    
	    // Get handle to texture coordinates location and load the texture uvs
	    int mTexCoordLoc = GLES20.glGetAttribLocation(mImageProgram.program, "a_texCoord" );
	    GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);
	    GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
	    
	    // Get handle to shape's transformation matrix and add our matrix
        int mtrxhandle = GLES20.glGetUniformLocation(mImageProgram.program, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);
        
        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (mImageProgram.program, "s_texture" );
        
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, mImageProgram.mTextureUnitIndex);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
        	
	}
	

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		// We need to know the current width and height.
		mScreenWidth = width;
		mScreenHeight = height;
		
		// Redo the Viewport, making it fullscreen.
		GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);
		
		// Clear our matrices
	    for(int i=0;i<16;i++)
	    {
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
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		

	    mImageProgram = new TextureShaderProgram(mContext,
	        R.raw.image_vertex_shader, R.raw.image_fragment_shader,
	        GL_TEXTURE0);

	    mText2DProgram = new TextureShaderProgram(mContext, R.raw.text2d_vertex_shader,
	        R.raw.text2d_fragment_shader,
	        GL_TEXTURE1);

	    // Set our shader programm
		GLES20.glUseProgram(mImageProgram.program);
		
		  // Setup our scaling system
        SetupScaling();
        // Create the triangles
        SetupTriangle();
        // Create the image information
        SetupImage();
        
        // Generate Textures, if more needed, alter these numbers.
        int[] textureObjectIds = new int[2];
        if (false) {
               GLES20.glGenTextures(2, textureObjectIds, 0);
        }
        
        // Retrieve our image from resources.
        int id = mContext.getResources().getIdentifier("drawable/textureatlas", 
                null, mContext.getPackageName());
        int  idText = mContext.getResources().getIdentifier("drawable/font",
            null, mContext.getPackageName());
        
        Log.w(TAG,"HELLO!");
        int textureImage = TextureHelper.loadTexture(mContext, id, GL_TEXTURE0); 
        int textureText = TextureHelper.loadTexture(mContext, idText, GL_TEXTURE1);  
        
        
        // Temporary create a bitmap
        if (false)
        {
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
        
        // Bind texture to texturename
        GLES20.glActiveTexture(mImageProgram.mTextureUnitEnum);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        
        // We are done using the bitmap so we should recycle it.
        bmp.recycle();
        }
        if (false) {
        // Again for the text texture
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), idText);
        GLES20.glActiveTexture(mText2DProgram.mTextureUnitEnum); // Uses 2nd texture unit
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[1]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();
        }
        
        // Create our texts
        SetupText(mText2DProgram); 
        Log.v(TAG,textureObjectIds.toString());
        // Set the clear color to black
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1);   
        
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
     
	}
	
	public void SetupText(TextureShaderProgram program) 	{
		// Create our text manager
		tm = new TextManager(program);
	
	//	tm.setTextureUnitIndex(program.mTextureUnitIndex); // fix to just reference internally
		
		// Pass the uniform scale
		tm.setUniformscale(ssu);
		
		// Create our new textobject
		TextObject txt = new TextObject("hello HayaA", mTextX, mTextY);
		
		// Add it to our manager
		tm.addText(txt);
		
		// Prepare the text for rendering
		tm.PrepareDraw();
	}

	// Misha's ugly hack: 
	public void UpdateText() 	    {

	    TextObject txt = new TextObject("hello HayaB", mTextX, mTextY);

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
		ssx = swp / 320.0f;
		ssy = shp / 480.0f;
		
		// Get our uniform scaler
		if(ssx > ssy)
    		ssu = ssy;
    	else
    		ssu = ssx;
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
	    UpdateText(); // TODO : Yuk! why the '1' here?
	}

	/*
	public void UpdateSprite()
	{
		// Get new transformed vertices
		//vertices = sprite.getTransformedVertices();
		
		// The vertex buffer.
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
	}
	*/
	
	public void SetupImage()
	{
		// We will use a randomizer for randomizing the textures from texture atlas.
		// This is strictly optional as it only effects the output of our app,
		// Not the actual knowledge.
		Random rnd = new Random();
		
		// 30 imageobjects times 4 vertices times (u and v)
		uvs = new float[30*4*2];
		
		// We will make 30 randomly textures objects
		for(int i=0; i<30; i++)
		{
			int random_u_offset = rnd.nextInt(2);
			int random_v_offset = rnd.nextInt(2);
			
			// Adding the UV's using the offsets
			uvs[(i*8) + 0] = random_u_offset * 0.5f;
			uvs[(i*8) + 1] = random_v_offset * 0.5f;
			uvs[(i*8) + 2] = random_u_offset * 0.5f;
			uvs[(i*8) + 3] = (random_v_offset+1) * 0.5f;
			uvs[(i*8) + 4] = (random_u_offset+1) * 0.5f;
			uvs[(i*8) + 5] = (random_v_offset+1) * 0.5f;
			uvs[(i*8) + 6] = (random_u_offset+1) * 0.5f;
			uvs[(i*8) + 7] = random_v_offset * 0.5f;	
		}
		
		// The texture buffer
		ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
		bb.order(ByteOrder.nativeOrder());
		uvBuffer = bb.asFloatBuffer();
		uvBuffer.put(uvs);
		uvBuffer.position(0);
		

	}
	
	public void SetupTriangle()
	{
		// We will need a randomizer
		Random rnd = new Random();
		
		// Our collection of vertices
		vertices = new float[30*4*3];
		
		// Create the vertex data
		for(int i=0;i<30;i++)
		{
			int offset_x = rnd.nextInt((int)swp);
			int offset_y = rnd.nextInt((int)shp);
			
			// Create the 2D parts of our 3D vertices, others are default 0.0f
			vertices[(i*12) + 0] = offset_x;
			vertices[(i*12) + 1] = offset_y + (30.0f*ssu);
			vertices[(i*12) + 2] = 0f;
			vertices[(i*12) + 3] = offset_x;
			vertices[(i*12) + 4] = offset_y;
			vertices[(i*12) + 5] = 0f;
			vertices[(i*12) + 6] = offset_x + (30.0f*ssu);
			vertices[(i*12) + 7] = offset_y;
			vertices[(i*12) + 8] = 0f;
			vertices[(i*12) + 9] = offset_x + (30.0f*ssu);
			vertices[(i*12) + 10] = offset_y + (30.0f*ssu);
			vertices[(i*12) + 11] = 0f;
		}
		
		// The indices for all textured quads
		indices = new short[30*6]; 
		int last = 0;
		for(int i=0;i<30;i++)
		{
			// We need to set the new indices for the new quad
			indices[(i*6) + 0] = (short) (last + 0);
			indices[(i*6) + 1] = (short) (last + 1);
			indices[(i*6) + 2] = (short) (last + 2);
			indices[(i*6) + 3] = (short) (last + 0);
			indices[(i*6) + 4] = (short) (last + 2);
			indices[(i*6) + 5] = (short) (last + 3);
			
			// Our indices are connected to the vertices so we need to keep them
			// in the correct order.
			// normal quad = 0,1,2,0,2,3 so the next one will be 4,5,6,4,6,7
			last = last + 4;
		}

		// The vertex buffer.
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(indices);
		drawListBuffer.position(0);
	}
	
	class Sprite
	{
		float angle;
		float scale;
		RectF base;
		PointF translation;
		
		public Sprite()
		{
			// Initialise our intital size around the 0,0 point
			base = new RectF(-50f*ssu,50f*ssu, 50f*ssu, -50f*ssu);
			
			// Initial translation
			translation = new PointF(50f*ssu,50f*ssu);
			
			// We start with our inital size
			scale = 1f;
			
			// We start in our inital angle
			angle = 0f;
		}
		
		
		public void translate(float deltax, float deltay)
		{
			// Update our location.
			translation.x += deltax;
			translation.y += deltay;
		}
		
		public void scale(float deltas)
		{
			scale += deltas;
		}
		
		public void rotate(float deltaa)
		{
			angle += deltaa;
		}
		
		public float[] getTransformedVertices()
		{
			// Start with scaling
			float x1 = base.left * scale;
			float x2 = base.right * scale;
			float y1 = base.bottom * scale;
			float y2 = base.top * scale;
			
			// We now detach from our Rect because when rotating, 
			// we need the seperate points, so we do so in opengl order
			PointF one = new PointF(x1, y2);
			PointF two = new PointF(x1, y1);
			PointF three = new PointF(x2, y1);
			PointF four = new PointF(x2, y2);
			
			// We create the sin and cos function once, 
			// so we do not have calculate them each time.
			float s = (float) Math.sin(angle);
			float c = (float) Math.cos(angle);
			
			// Then we rotate each point
			one.x = x1 * c - y2 * s;
			one.y = x1 * s + y2 * c;
			two.x = x1 * c - y1 * s;
			two.y = x1 * s + y1 * c;
			three.x = x2 * c - y1 * s;
			three.y = x2 * s + y1 * c;
			four.x = x2 * c - y2 * s;
			four.y = x2 * s + y2 * c;
			
			// Finally we translate the sprite to its correct position.
			one.x += translation.x;
			one.y += translation.y;
			two.x += translation.x;
			two.y += translation.y;
			three.x += translation.x;
			three.y += translation.y;
			four.x += translation.x;
			four.y += translation.y;
			
			// We now return our float array of vertices.
			return new float[]
	        {
					one.x, one.y, 0.0f,
	       			two.x, two.y, 0.0f,
	       			three.x, three.y, 0.0f,
	       			four.x, four.y, 0.0f,
	        };
		}
	}
}
