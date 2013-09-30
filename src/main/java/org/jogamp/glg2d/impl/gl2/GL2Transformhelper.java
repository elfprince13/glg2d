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
package org.jogamp.glg2d.impl.gl2;

import java.awt.geom.AffineTransform;


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import org.jogamp.glg2d.GLGraphics2D;
import org.jogamp.glg2d.impl.AbstractMatrixHelper;

import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

public class GL2Transformhelper extends AbstractMatrixHelper {
  protected GLContext context;

  private FloatBuffer matrixBuf = BufferUtils.createFloatBuffer(16);

  @Override
  public void setG2D(GLGraphics2D g2d) {
    super.setG2D(g2d);
    context = g2d.getGLContext();

    setupGLView();
    flushTransformToOpenGL();
  }

  protected void setupGLView() {
    IntBuffer viewportDimensions = BufferUtils.createIntBuffer(16);
    GL11.glGetInteger(GL11.GL_VIEWPORT, viewportDimensions);
    int width = viewportDimensions.get(2);
    int height = viewportDimensions.get(3);

    // setup projection
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glLoadIdentity();
    GL11.glOrtho(0, width, 0, height, -1, 1);

    // the MODELVIEW matrix will get adjusted later

    GL11.glMatrixMode(GL11.GL_TEXTURE);
    GL11.glLoadIdentity();
  }

  /**
   * Sends the {@code AffineTransform} that's on top of the stack to the video
   * card.
   */
  protected void flushTransformToOpenGL() {
    FloatBuffer matrix = getGLMatrix(stack.peek());

    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glLoadMatrix(matrix);
  }

  /**
   * Gets the GL matrix for the {@code AffineTransform} with the change of
   * coordinates inlined. Since Java2D uses the upper-left as 0,0 and OpenGL
   * uses the lower-left as 0,0, we have to pre-multiply the matrix before
   * loading it onto the video card.
   */
  protected FloatBuffer getGLMatrix(AffineTransform transform) {
	matrixBuf.clear();
	BufferUtils.zeroBuffer(matrixBuf);
    matrixBuf.put(0, (float) transform.getScaleX());
    matrixBuf.put(1, -(float) transform.getShearY());
    matrixBuf.put(4, (float) transform.getShearX());
    matrixBuf.put(5, -(float) transform.getScaleY());
    matrixBuf.put(10, 1);
    matrixBuf.put(12, (float) transform.getTranslateX());
    matrixBuf.put(13, g2d.getCanvasHeight() - (float) transform.getTranslateY());
    matrixBuf.put(15, 1);

    return matrixBuf;
  }
}
