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
package org.jogamp.glg2d;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

public class GLG2DUtils {
  private static final Logger LOGGER = Logger.getLogger(GLG2DUtils.class.getName());

  public static void setColor(Color c, float preMultiplyAlpha) {
    int rgb = c.getRGB();
    GL11.glColor4ub((byte) (rgb >> 16 & 0xFF), (byte) (rgb >> 8 & 0xFF), (byte) (rgb & 0xFF), (byte) ((rgb >> 24 & 0xFF) * preMultiplyAlpha));
  }

  public static float[] getGLColor(Color c) {
    return c.getComponents(null);
  }

  public static int getViewportHeight() {
    IntBuffer viewportDimensions = BufferUtils.createIntBuffer(16);
    GL11.glGetInteger(GL11.GL_VIEWPORT, viewportDimensions);
    int canvasHeight = viewportDimensions.get(3);
    return canvasHeight;
  }

  public static void logGLError() {
    int error = GL11.glGetError();
    if (error != GL11.GL_NO_ERROR) {
      LOGGER.log(Level.SEVERE, "GL Error: code " + error);
    }
  }

  public static int ensureIsGLBuffer(int bufferId) {
    if (GL15.glIsBuffer(bufferId)) {
      return bufferId;
    } else {
      return genBufferId();
    }
  }

  public static int genBufferId() {
	IntBuffer ids = BufferUtils.createIntBuffer(1);
    GL15.glGenBuffers(ids);
    return ids.get(0);
  }
}
