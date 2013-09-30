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


import java.awt.BasicStroke;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL41;

import org.jogamp.glg2d.GLG2DUtils;

import org.lwjgl.BufferUtils;

public class GeometryShaderStrokePipeline extends AbstractShaderPipeline {
  public static final int DRAW_END_NONE = 0;
  public static final int DRAW_END_FIRST = -1;
  public static final int DRAW_END_LAST = 1;
  public static final int DRAW_END_BOTH = 2;

  protected FloatBuffer vBuffer = BufferUtils.createFloatBuffer(500);

  protected int maxVerticesOut = 32;

  protected int vertCoordLocation;
  protected int vertBeforeLocation;
  protected int vertAfterLocation;
  protected int vertCoordBuffer;

  protected int lineWidthLocation;
  protected int miterLimitLocation;
  protected int joinTypeLocation;
  protected int capTypeLocation;
  protected int drawEndLocation;

  public GeometryShaderStrokePipeline() {
    this("StrokeShader.v", "StrokeShader.g", "StrokeShader.f");
  }

  public GeometryShaderStrokePipeline(String vertexShaderFileName, String geometryShaderFileName, String fragmentShaderFileName) {
    super(vertexShaderFileName, geometryShaderFileName, fragmentShaderFileName);
  }

  public void setStroke(BasicStroke stroke) {
    if (lineWidthLocation >= 0) {
      GL20.glUniform1f(lineWidthLocation, stroke.getLineWidth());
    }

    if (miterLimitLocation >= 0) {
      GL20.glUniform1f(miterLimitLocation, stroke.getMiterLimit());
    }

    if (joinTypeLocation >= 0) {
      GL20.glUniform1i(joinTypeLocation, stroke.getLineJoin());
    }

    if (capTypeLocation >= 0) {
      GL20.glUniform1i(capTypeLocation, stroke.getEndCap());
    }
  }

  protected void setDrawEnd(int drawType) {
    if (drawEndLocation >= 0) {
      GL20.glUniform1i(drawEndLocation, drawType);
    }
  }

  protected void bindBuffer(FloatBuffer vertexBuffer) {
    GL20.glEnableVertexAttribArray(vertCoordLocation);
    GL20.glEnableVertexAttribArray(vertBeforeLocation);
    GL20.glEnableVertexAttribArray(vertAfterLocation);

    if (GL15.glIsBuffer(vertCoordBuffer)) {
      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertCoordBuffer);
      GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertexBuffer);
    } else {
      vertCoordBuffer = GLG2DUtils.genBufferId();

      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertCoordBuffer);
      GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STREAM_DRAW);
    }

    GL20.glVertexAttribPointer(vertCoordLocation, 2, GL11.GL_FLOAT, false, 0, 2 * (Float.SIZE / Byte.SIZE));
    GL20.glVertexAttribPointer(vertBeforeLocation, 2, GL11.GL_FLOAT, false, 0, 0);
    GL20.glVertexAttribPointer(vertAfterLocation, 2, GL11.GL_FLOAT, false, 0, 4 * (Float.SIZE / Byte.SIZE));
  }

  public void draw(FloatBuffer vertexBuffer, boolean close) {
    int pos = vertexBuffer.position();
    int lim = vertexBuffer.limit();
    int numPts = (lim - pos) / 2;

    if (numPts * 2 + 6 > vBuffer.capacity()) {
      vBuffer = BufferUtils.createFloatBuffer(numPts * 2 + 4);
    }

    vBuffer.clear();

    if (close) {
      vBuffer.put(vertexBuffer.get(lim - 2));
      vBuffer.put(vertexBuffer.get(lim - 1));
      vBuffer.put(vertexBuffer);
      vBuffer.put(vertexBuffer.get(pos));
      vBuffer.put(vertexBuffer.get(pos + 1));
      vBuffer.put(vertexBuffer.get(pos + 2));
      vBuffer.put(vertexBuffer.get(pos + 3));
    } else {
      vBuffer.put(0);
      vBuffer.put(0);
      vBuffer.put(vertexBuffer);
      vBuffer.put(0);
      vBuffer.put(0);
    }

    vBuffer.flip();

    bindBuffer(vBuffer);

    if (close) {
      setDrawEnd(DRAW_END_NONE);
      GL11.glDrawArrays(GL11.GL_LINES, 0, numPts + 1);
      GL11.glDrawArrays(GL11.GL_LINES, 1, numPts);
    } else if (numPts == 2) {
      setDrawEnd(DRAW_END_BOTH);
      GL11.glDrawArrays(GL11.GL_LINES, 0, 2);
    } else {
      setDrawEnd(DRAW_END_NONE);
      GL11.glDrawArrays(GL11.GL_LINES, 1, numPts - 2);
      GL11.glDrawArrays(GL11.GL_LINES, 2, numPts - 3);

      setDrawEnd(DRAW_END_FIRST);
      GL11.glDrawArrays(GL11.GL_LINES, 0, 2);

      setDrawEnd(DRAW_END_LAST);
      GL11.glDrawArrays(GL11.GL_LINES, numPts - 2, 2);
    }

    GL20.glDisableVertexAttribArray(vertCoordLocation);
    GL20.glDisableVertexAttribArray(vertBeforeLocation);
    GL20.glDisableVertexAttribArray(vertAfterLocation);

    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
  }

  @Override
  protected void setupUniformsAndAttributes() {
    super.setupUniformsAndAttributes();

    transformLocation = GL20.glGetUniformLocation(programId, "u_transform");
    colorLocation = GL20.glGetUniformLocation(programId, "u_color");
    lineWidthLocation = GL20.glGetUniformLocation(programId, "u_lineWidth");
    miterLimitLocation = GL20.glGetUniformLocation(programId, "u_miterLimit");
    joinTypeLocation = GL20.glGetUniformLocation(programId, "u_joinType");
    drawEndLocation = GL20.glGetUniformLocation(programId, "u_drawEnd");
    capTypeLocation = GL20.glGetUniformLocation(programId, "u_capType");

    vertCoordLocation = GL20.glGetAttribLocation(programId, "a_vertCoord");
    vertBeforeLocation = GL20.glGetAttribLocation(programId, "a_vertBefore");
    vertAfterLocation = GL20.glGetAttribLocation(programId, "a_vertAfter");
  }

  @Override
  protected void attachShaders() {
    super.attachShaders();

    GL41.glProgramParameteri(programId, GL32.GL_GEOMETRY_INPUT_TYPE, GL11.GL_LINES);
    GL41.glProgramParameteri(programId, GL32.GL_GEOMETRY_OUTPUT_TYPE, GL11.GL_TRIANGLE_STRIP);
    GL41.glProgramParameteri(programId, GL32.GL_GEOMETRY_VERTICES_OUT, maxVerticesOut);
  }

  @Override
  public void delete() {
    super.delete();

    GL15.glDeleteBuffers(vertCoordBuffer);
  }
}
