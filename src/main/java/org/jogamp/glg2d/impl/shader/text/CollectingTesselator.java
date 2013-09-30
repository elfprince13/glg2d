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
package org.jogamp.glg2d.impl.shader.text;


import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import org.jogamp.glg2d.impl.AbstractTesselatorVisitor;

import org.lwjgl.BufferUtils;

public class CollectingTesselator extends AbstractTesselatorVisitor {
  @Override
  public void setGLContext(GLContext context) {
    // nop
  }

  @Override
  public void beginPoly(int windingRule) {
    super.beginPoly(windingRule);

    vBuffer.clear();
  }

  @Override
  protected void configureTesselator(int windingRule) {
    super.configureTesselator(windingRule);

    tesselator.gluTessCallback(GLU.GLU_TESS_EDGE_FLAG_DATA, callback);
  }

  @Override
  protected void beginTess(int type) {
    // don't clear the vertex buffer
  }

  @Override
  protected void endTess() {
    // nothing to do
  }

  public Triangles getTesselated() {
    FloatBuffer buf = vBuffer.getBuffer();
    buf.flip();
    return new Triangles(buf);
  }

  public static class Triangles {
    private FloatBuffer triangles;

    public Triangles(FloatBuffer vertexBuffer) {
      int numVertices = vertexBuffer.limit() - vertexBuffer.position();

      triangles = BufferUtils.createFloatBuffer(numVertices);
      triangles.put(vertexBuffer);

      triangles.flip();
    }

    public void draw() {
      int numFloats = triangles.limit();
      GL15.glBufferData(GL15.GL_ARRAY_BUFFER, triangles, GL15.GL_STREAM_DRAW);
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numFloats / 2);
    }
  }
}
