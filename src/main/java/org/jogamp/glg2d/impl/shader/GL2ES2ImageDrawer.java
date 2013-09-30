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


import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;


import org.jogamp.glg2d.GLGraphics2D;
import org.jogamp.glg2d.impl.AbstractImageHelper;

import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;

public class GL2ES2ImageDrawer extends AbstractImageHelper {
  protected GLShaderGraphics2D g2d;
  protected GLContext context;

  protected FloatBuffer vertTexCoords = BufferUtils.createFloatBuffer(16);
  protected GL2ES2ImagePipeline shader;

  private float[] white_static = new float[] { 1, 1, 1, 1 };
  private FloatBuffer white = BufferUtils.createFloatBuffer(4);

  public GL2ES2ImageDrawer() {
    this(new GL2ES2ImagePipeline());
  }

  public GL2ES2ImageDrawer(GL2ES2ImagePipeline shader) {
    this.shader = shader;
  }

  @Override
  public void setG2D(GLGraphics2D g2d) {
    super.setG2D(g2d);

    if (g2d instanceof GLShaderGraphics2D) {
      this.g2d = (GLShaderGraphics2D) g2d;
    } else {
      throw new IllegalArgumentException(GLGraphics2D.class.getName() + " implementation must be instance of "
          + GLShaderGraphics2D.class.getSimpleName());
    }

    context = g2d.getGLContext();
    if (!shader.isSetup()) {
      shader.setup();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    shader.delete();
  }

  @Override
  protected void begin(Texture texture, AffineTransform xform, Color bgcolor) {
    /*
     * FIXME This is unexpected since we never disable blending, but in some
     * cases it interacts poorly with multiple split panes, scroll panes and the
     * text renderer to disable blending.
     */
    g2d.setComposite(g2d.getComposite());

    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    texture.bind();

    shader.use(true);

    if (bgcolor == null) {
    	white.clear();
    	white.put(white_static,0,3);
    	white.put(g2d.getUniformsObject().colorHook.getAlpha());
      shader.setColor(white);
    } else {
      FloatBuffer rgba = g2d.getUniformsObject().colorHook.getRGBA();
      shader.setColor(rgba);
    }

    if (xform == null) {
      shader.setTransform(g2d.getUniformsObject().transformHook.getGLMatrixData());
    } else {
      shader.setTransform(g2d.getUniformsObject().transformHook.getGLMatrixData(xform));
    }

    shader.setTextureUnit(0);
  }

  @Override
  protected void applyTexture(Texture texture, float dx1, float dy1, float dx2, float dy2, float sx1, float sy1, float sx2, float sy2) {
    vertTexCoords.rewind();

    // interleave vertex and texture coordinates
    vertTexCoords.put(dx1);
    vertTexCoords.put(dy1);
    vertTexCoords.put(sx1);
    vertTexCoords.put(sy1);

    vertTexCoords.put(dx1);
    vertTexCoords.put(dy2);
    vertTexCoords.put(sx1);
    vertTexCoords.put(sy2);

    vertTexCoords.put(dx2);
    vertTexCoords.put(dy1);
    vertTexCoords.put(sx2);
    vertTexCoords.put(sy1);

    vertTexCoords.put(dx2);
    vertTexCoords.put(dy2);
    vertTexCoords.put(sx2);
    vertTexCoords.put(sy2);

    vertTexCoords.flip();
    shader.draw(vertTexCoords);
  }

  @Override
  protected void end(Texture texture) {
    shader.use(false);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
  }
}
