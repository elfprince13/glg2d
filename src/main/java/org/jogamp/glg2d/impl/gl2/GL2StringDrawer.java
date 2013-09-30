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


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import org.jogamp.glg2d.impl.AbstractTextDrawer;

import org.newdawn.slick.TrueTypeFont;

import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

/**
 * Draws text for the {@code GLGraphics2D} class.
 */
public class GL2StringDrawer extends AbstractTextDrawer {
  protected FontRenderCache cache = new FontRenderCache();
  protected FloatBuffer intcolor = BufferUtils.createFloatBuffer(4);

  @Override
  public void dispose() {
    cache.dispose();
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    drawString(iterator, (int) x, (int) y);
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    StringBuilder builder = new StringBuilder(iterator.getEndIndex() - iterator.getBeginIndex());
    while (iterator.next() != AttributedCharacterIterator.DONE) {
      builder.append(iterator.current());
    }

    drawString(builder.toString(), x, y);
  }

  @Override
  public void drawString(String string, float x, float y) {
    drawString(string, (int) x, (int) y);
  }

  @Override
  public void drawString(String string, int x, int y) {
    TrueTypeFont renderer = getRenderer(getFont());

    begin(renderer);
    intcolor.rewind();
    renderer.drawString((float)x, (float)(g2d.getCanvasHeight() - y), string, new org.newdawn.slick.Color(intcolor));
    end(renderer);
  }

  protected TrueTypeFont getRenderer(Font font) {
    return cache.getRenderer(font, stack.peek().antiAlias);
  }

  /**
   * Sets the font color, respecting the AlphaComposite if it wants to
   * pre-multiply an alpha.
   */
  protected void setTextColorRespectComposite(TrueTypeFont renderer) {
    Color color = g2d.getColor();
    if (g2d.getComposite() instanceof AlphaComposite) {
      float alpha = ((AlphaComposite) g2d.getComposite()).getAlpha();
      if (alpha < 1) {
        float[] rgba = color.getRGBComponents(null);
        color = new Color(rgba[0], rgba[1], rgba[2], alpha * rgba[3]);
      }
    }

    float[] cArray = { color.getRed() / 255f,color.getGreen() / 255f,color.getBlue() / 255f,color.getAlpha() / 255f};
	intcolor.clear();
	intcolor.put(cArray);
  }

  protected void begin(TrueTypeFont renderer) {
    setTextColorRespectComposite(renderer);

    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPushMatrix();
    GL11.glScalef(1, -1, 1);
    GL11.glTranslatef(0, -g2d.getCanvasHeight(), 0);

  }

  protected void end(TrueTypeFont renderer) {
    GL11.glPopMatrix();
  }

  @SuppressWarnings("serial")
  public static class FontRenderCache extends HashMap<Font, TrueTypeFont[]> {
    public TrueTypeFont getRenderer(Font font, boolean antiAlias) {
      TrueTypeFont[] renderers = get(font);
      if (renderers == null) {
        renderers = new TrueTypeFont[2];
        put(font, renderers);
      }

      TrueTypeFont renderer = renderers[antiAlias ? 1 : 0];

      if (renderer == null) {
        renderer = new TrueTypeFont(font, antiAlias);
        renderers[antiAlias ? 1 : 0] = renderer;
      }

      return renderer;
    }

    public void dispose() {
      
    }
  }
}
