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
    private static final int STRIDE = (POSITION_COMPONENT_COUNT 
        + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    
    private static final int VERTICES_PER_GLYPH  = 4;
    private static final int DIMS_PER_VERTEX  = 3;
    private static final int TRIANGLES_PER_GLYPH  = 2;
    // private  final float[] VERTEX_DATA;
    // Order of coordinates: X, Y, S, T

    public static float vertices[];
    public static short indices[];
    public static float uvs[];
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;
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
    
    private void AddGlyph(int x, int y, float ssu) {
         int index = numGlyphs * VERTICES_PER_GLYPH * DIMS_PER_VERTEX;
         numGlyphs++;
         final float glyphHeight = 20f;
         final float glyphWidth = 10f;
         
         vertices[index + 0] = x;
         vertices[index + 1] = y + (glyphHeight*ssu);
         vertices[index + 2] = 0f;
         
         vertices[index + 3] = x;
         vertices[index + 4] = y;
         vertices[index + 5] = 0f;
         
         vertices[index + 6] = x + (glyphWidth*ssu);
         vertices[index + 7] = y;
         vertices[index + 8] = 0f;
         
         vertices[index + 9] = x + (glyphWidth*ssu);
         vertices[index + 10] = y + (glyphHeight*ssu);
         vertices[index + 11] = 0f;
      }
  
    public void Setup(int nPoints, float swp, float shp, float ssu)
    {
        Log.w(TAG,"Setup Entry");
        // We will need a randomizer
        Random rnd = new Random();

        // Our collection of vertices
        vertices = new float[nPoints*VERTICES_PER_GLYPH*DIMS_PER_VERTEX];

        // Create the vertex data
        for(int i=0;i<nPoints;i++)         {
            int offset_x = rnd.nextInt((int)swp);
            int offset_y = rnd.nextInt((int)shp);

            AddGlyph(offset_x, offset_y, ssu);
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
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);


        // We will use a randomizer for randomizing the textures from texture atlas.
        // This is strictly optional as it only effects the output of our app,
        // Not the actual knowledge.
        //   Random rnd = new Random();

        // nPoints imageobjects times 4 vertices times (u and v)
        uvs = new float[nPoints*4*2];

        // We will make nPoints randomly textures objects
        for(int i=0; i<nPoints; i++)         {
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

        ByteBuffer bb2 = ByteBuffer.allocateDirect(uvs.length * BYTES_PER_FLOAT);
        bb2.order(ByteOrder.nativeOrder());
        uvBuffer = bb2.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
    }

    public void Draw(float[] m, TextureShaderProgram mImageProgram) {

        mImageProgram.useProgram();

        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // get handle to vertex shader's aPosition member and add vertices
        int mPositionHandle = mImageProgram.getPositionAttributeLocation();
        glVertexAttribPointer(mPositionHandle, 3, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(mPositionHandle);

        // Get handle to texture coordinates location and load the texture uvs
        int mTexCoordLoc = mImageProgram.getTextureCoordinatesAttributeLocation();
        glVertexAttribPointer ( mTexCoordLoc, 2, GL_FLOAT, false, 0, uvBuffer);
        glEnableVertexAttribArray ( mTexCoordLoc );

        // Get handle to shape's transformation matrix and add our matrix
        int mtrxhandle = mImageProgram.getMatrixUniformLocation();
        glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        // Get handle to textures locations
        int mSamplerLoc = mImageProgram.getTextureUnitUniformLocation();

        // Set the sampler texture unit to 0, where we have saved the texture.
        glUniform1i ( mSamplerLoc, mImageProgram.mTextureUnitIndex);

        // Draw the triangle
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        glDisableVertexAttribArray(mPositionHandle);
        glDisableVertexAttribArray(mTexCoordLoc);

    }
}
