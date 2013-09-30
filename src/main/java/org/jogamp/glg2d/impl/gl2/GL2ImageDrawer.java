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

import java.awt.Color;
import java.awt.geom.AffineTransform;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.newdawn.slick.opengl.Texture;


import org.jogamp.glg2d.GLGraphics2D;
import org.jogamp.glg2d.impl.AbstractImageHelper;

public class GL2ImageDrawer extends AbstractImageHelper {
  protected GLContext context;

  protected AffineTransform savedTransform;

  @Override
  public void setG2D(GLGraphics2D g2d) {
    super.setG2D(g2d);
    context = g2d.getGLContext();
  }

  @Override
  protected void begin(Texture texture, AffineTransform xform, Color bgcolor) {
    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
    GL11.glTexParameterf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_BLEND);

    /*
     * FIXME This is unexpected since we never disable blending, but in some
     * cases it interacts poorly with multiple split panes, scroll panes and the
     * text renderer to disable blending.
     */
    g2d.setComposite(g2d.getComposite());

    GL11.glEnable(GL11.GL_TEXTURE_2D);
    texture.bind();

    savedTransform = null;
    if (xform != null && !xform.isIdentity()) {
      savedTransform = g2d.getTransform();
      g2d.transform(xform);
    }

    g2d.getColorHelper().setColorRespectComposite(bgcolor == null ? Color.white : bgcolor);
  }

  @Override
  protected void end(Texture texture) {
    if (savedTransform != null) {
      g2d.setTransform(savedTransform);
    }

    GL11.glDisable(GL11.GL_TEXTURE_2D);
    g2d.getColorHelper().setColorRespectComposite(g2d.getColor());
  }

  @Override
  protected void applyTexture(Texture texture, float dx1, float dy1, float dx2, float dy2, float sx1, float sy1, float sx2, float sy2) {
    GL11.glBegin(GL11.GL_QUADS);

    // SW
    GL11.glTexCoord2f(sx1, sy2);
    GL11.glVertex2f(dx1, dy2);
    // SE
    GL11.glTexCoord2f(sx2, sy2);
    GL11.glVertex2f(dx2, dy2);
    // NE
    GL11.glTexCoord2f(sx2, sy1);
    GL11.glVertex2f(dx2, dy1);
    // NW
    GL11.glTexCoord2f(sx1, sy1);
    GL11.glVertex2f(dx1, dy1);

    GL11.glEnd();
  }
}
