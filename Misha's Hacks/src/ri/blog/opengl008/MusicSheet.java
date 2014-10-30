/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package ri.blog.opengl008;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.airhockey.android.Constants.BYTES_PER_FLOAT;
import static com.airhockey.android.Constants.BYTES_PER_SHORT;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;


import android.opengl.GLES20;
import android.util.Log;

import com.airhockey.android.data.VertexArray;
import com.airhockey.android.programs.TextureShaderProgram;

public class MusicSheet {            
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int TOTAL_COMPONENT_COUNT = POSITION_COMPONENT_COUNT+TEXTURE_COORDINATES_COMPONENT_COUNT;
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    
    private static final int VERTICES_PER_GLYPH  = 4;
    private static final int TRIANGLES_PER_GLYPH  = 2;
    // private  final float[] VERTEX_DATA;
    // Order of coordinates: X, Y, U, V

    public static float vertices[];
    public static short indices[];
    public FloatBuffer vertexBuffer;
    public ShortBuffer indexBuffer;

    private static final String TAG = "MusicSheet";

    private static int numGlyphs = 0;

    //   private final VertexArray vertexArray;

    public MusicSheet() {
        Log.w(TAG,"empty constructor");
        //   vertexArray = new VertexArray(VERTEX_DATA);
    }
    /*
    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
            0, 
            textureProgram.getPositionAttributeLocation(), 
            POSITION_COMPONENT_COUNT,
            STRIDE);

        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT, 
            textureProgram.getTextureCoordinatesAttributeLocation(),
            TEXTURE_COORDINATES_COMPONENT_COUNT, 
            STRIDE);
    }
     */
    /*
    public void draw() {                                
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }    
     */
    // operate in device space
    // add a glyph to the internal vertices array. Glyph is composed of two triangles
    //
    // 4 points:
    // 0: upper left
    // 1: lower left
    // 2: lower right
    // 3: upper right
    
    private void AddGlyph(int x, int y, float ssu, float llu, float llv, float uDist, float vDist) {
         int index = numGlyphs * VERTICES_PER_GLYPH * TOTAL_COMPONENT_COUNT;
         
         final float glyphHeight = 30f;
         final float glyphWidth = 30f;
         
         vertices[index++] = x;
         vertices[index++] = y + (glyphHeight*ssu);
         vertices[index++] = llu;
         vertices[index++] = llv;
         
         vertices[index++] = x;
         vertices[index++] = y;
         vertices[index++] = llu;
         vertices[index++] = llv + uDist;
         
         vertices[index++] = x + (glyphWidth*ssu);
         vertices[index++] = y;
         vertices[index++] = llu + uDist;
         vertices[index++]= llv + vDist;
       
         vertices[index++] = x + (glyphWidth*ssu);
         vertices[index++] = y + (glyphHeight*ssu);
         vertices[index++] = llu + uDist;
         vertices[index++] = llv;  
         
         numGlyphs++;
      }
  
    public void Setup(int nPoints, float swp, float shp, float ssu)
    {
         // We will need a randomizer
        Random rnd = new Random();

        // Our collection of vertices
        vertices = new float[nPoints*VERTICES_PER_GLYPH*TOTAL_COMPONENT_COUNT];

        // Create the vertex data
        for(int i=0;i<nPoints;i++)         {
            int offset_x = rnd.nextInt((int)swp);
            int offset_y = rnd.nextInt((int)shp);

            float llu = rnd.nextInt(2) * 0.5f;
            float llv = rnd.nextInt(2) * 0.5f;
  
            
            AddGlyph(offset_x, offset_y, ssu, llu, llv, 0.5f, 0.5f);
        }

        // The indices for all textured quads
        indices = new short[nPoints*3*TRIANGLES_PER_GLYPH]; 
        int last = 0;
        for(int i=0;i<nPoints;i++)         {
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
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * BYTES_PER_SHORT);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

    }

    public void Draw(float[] m, TextureShaderProgram mImageProgram) {

        mImageProgram.useProgram();

        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // get handle to vertex shader's aPosition member and add vertices
        int mPositionHandle = mImageProgram.getPositionAttributeLocation();
        vertexBuffer.position(0);
        glVertexAttribPointer(mPositionHandle, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(mPositionHandle);
     
        // Get handle to texture coordinates location and load the texture uvs
        int mTexCoordLoc = mImageProgram.getTextureCoordinatesAttributeLocation();
        vertexBuffer.position(POSITION_COMPONENT_COUNT); // start at UV coordinates
        glVertexAttribPointer ( mTexCoordLoc, TEXTURE_COORDINATES_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray ( mTexCoordLoc );

        // Get handle to shape's transformation matrix and add our matrix
        int mtrxhandle = mImageProgram.getMatrixUniformLocation();
        glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        // Get handle to textures locations
        int mSamplerLoc = mImageProgram.getTextureUnitUniformLocation();

        // Set the sampler texture unit to 0, where we have saved the texture.
        glUniform1i ( mSamplerLoc, mImageProgram.mTextureUnitIndex);

        // Draw the triangle
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        glDisableVertexAttribArray(mPositionHandle);
        glDisableVertexAttribArray(mTexCoordLoc);

    }
}
