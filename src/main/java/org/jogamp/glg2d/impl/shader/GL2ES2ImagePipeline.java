/*
 * Copyright 2013 Brandon Borkholder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jogamp.glg2d.impl.shader;

import static org.jogamp.glg2d.GLG2DUtils.ensureIsGLBuffer;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class GL2ES2ImagePipeline extends AbstractShaderPipeline {
  protected int vertexBufferId = -1;

  protected int textureLocation = -1;
  protected int vertCoordLocation = -1;
  protected int texCoordLocation = -1;

  public GL2ES2ImagePipeline() {
    this("TextureShader.v", "TextureShader.f");
  }

  public GL2ES2ImagePipeline(String vertexShaderFileName, String fragmentShaderFileName) {
    super(vertexShaderFileName, null, fragmentShaderFileName);
  }

  public void setTextureUnit(int unit) {
    if (textureLocation >= 0) {
      GL20.glUniform1i(textureLocation, unit);
    }
  }

  protected void bufferData(FloatBuffer buffer) {
    vertexBufferId = ensureIsGLBuffer(vertexBufferId);

    GL20.glEnableVertexAttribArray(vertCoordLocation);
    GL20.glEnableVertexAttribArray(texCoordLocation);

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferId);
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

    GL20.glVertexAttribPointer(vertCoordLocation, 2, GL11.GL_FLOAT, false, 4 * (Float.SIZE / Byte.SIZE), 0);
    GL20.glVertexAttribPointer(texCoordLocation, 2, GL11.GL_FLOAT, false, 4 * (Float.SIZE / Byte.SIZE), 2 * (Float.SIZE / Byte.SIZE));
  }

  public void draw(FloatBuffer interleavedVertTexBuffer) {
    bufferData(interleavedVertTexBuffer);

    GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);

    GL20.glDisableVertexAttribArray(vertCoordLocation);
    GL20.glDisableVertexAttribArray(texCoordLocation);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }

  @Override
  protected void setupUniformsAndAttributes() {
    super.setupUniformsAndAttributes();

    transformLocation = GL20.glGetUniformLocation(programId, "u_transform");
    colorLocation = GL20.glGetUniformLocation(programId, "u_color");
    textureLocation = GL20.glGetUniformLocation(programId, "u_tex");

    vertCoordLocation = GL20.glGetAttribLocation(programId, "a_vertCoord");
    texCoordLocation = GL20.glGetAttribLocation(programId, "a_texCoord");
  }

  @Override
  public void delete() {
    super.delete();

    if (GL15.glIsBuffer(vertexBufferId)) {
      GL15.glDeleteBuffers(vertexBufferId);
    }
  }
}
