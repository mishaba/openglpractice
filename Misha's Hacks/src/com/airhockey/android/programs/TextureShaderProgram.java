/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.airhockey.android.programs;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import android.content.Context;

// import com.airhockey.android.R;
// import ri.blog.opengl008.R;

/* 
 * The book-provided code has been modified to support multiple texture units.  The current 
 * implementation does tie together a shader program and a texture unit. But that's still a step 
 * toward using multiple texture units.
 * Not sure what the perf advantage of multiple shader units is.
 * 
 * But it does take care of the huge mismatch between texture unit ENUMs as required by glActiveTexture
 *  and texture unit INDEXes as required by glUniform 
 *  
 *  
 *  NOTE: some programs may not have these variables, in which case the returned values 
 *  will be -1, which means not found.
 *  That's OK, the calling code shouldn't be asking for program variables 
 *  that it doesn't know about.
 *  Error reporting code could be added to the getter functions.
 */

public class TextureShaderProgram extends ShaderProgram {
    // Uniform locations
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;
    
    // Attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;
    private final int aColorLocation;
    
    // Texture Unit IDs
    
    public int mTextureUnitEnum;
    public int mTextureUnitIndex;
    
    public TextureShaderProgram(Context context, int idVertex, int idFragment, int textureUnitEnum) {
        super(context, idVertex, idFragment);
    
        // Retrieve uniform locations for the shader program.
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        
        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
        
        // Save the texture unit information
        mTextureUnitEnum = textureUnitEnum;
        mTextureUnitIndex = textureUnitEnum - GL_TEXTURE0;
    }
    
    
    public void setUniforms(float[] matrix, int textureId) {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // Set the active texture unit to texture unit 0.
        glActiveTexture(mTextureUnitEnum);

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uTextureUnitLocation, mTextureUnitIndex);
    }
    
    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation() {
        return aTextureCoordinatesLocation;
    }
    
    public int getMatrixUniformLocation() {
        return uMatrixLocation;
    }


    public int getTextureUnitUniformLocation() {
        return uTextureUnitLocation;
  
    }


    public int getColorAttributeLocation() {
      return aColorLocation;
    }
}
