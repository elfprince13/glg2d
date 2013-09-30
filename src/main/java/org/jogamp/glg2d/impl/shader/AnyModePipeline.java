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

public class AnyModePipeline extends AbstractShaderPipeline {
  protected int vertCoordBuffer = -1;
  protected int vertCoordLocation = -1;

  public AnyModePipeline() {
    this("FixedFuncShader.v", "FixedFuncShader.f");
  }

  public AnyModePipeline(String vertexShaderFileName, String fragmentShaderFileName) {
    super(vertexShaderFileName, null, fragmentShaderFileName);
  }

  public void bindBuffer() {
    GL20.glEnableVertexAttribArray(vertCoordLocation);
    vertCoordBuffer = ensureIsGLBuffer(vertCoordBuffer);

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertCoordBuffer);
    GL20.glVertexAttribPointer(vertCoordLocation, 2, GL11.GL_FLOAT, false, 0, 0);
  }

  public void bindBufferData(FloatBuffer vertexBuffer) {
    bindBuffer();

    int count = vertexBuffer.limit() - vertexBuffer.position();
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STREAM_DRAW);
  }

  public void unbindBuffer() {
    GL20.glDisableVertexAttribArray(vertCoordLocation);
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }

  public void draw(int mode, FloatBuffer vertexBuffer) {
    bindBufferData(vertexBuffer);

    int numPts = (vertexBuffer.limit() - vertexBuffer.position()) / 2;
    GL11.glDrawArrays(mode, 0, numPts);

    unbindBuffer();
  }

  @Override
  protected void setupUniformsAndAttributes() {
    super.setupUniformsAndAttributes();

    transformLocation = GL20.glGetUniformLocation(programId, "u_transform");
    colorLocation = GL20.glGetUniformLocation(programId, "u_color");

    vertCoordLocation = GL20.glGetAttribLocation(programId, "a_vertCoord");
  }

  @Override
  public void delete() {
    super.delete();

    if (GL15.glIsBuffer(vertCoordBuffer)) {
      GL15.glDeleteBuffers(vertCoordBuffer);
    }
  }
}
