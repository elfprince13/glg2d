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
package org.jogamp.glg2d.impl;


import java.awt.BasicStroke;
import java.awt.geom.PathIterator;

import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallback;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;

import org.jogamp.glg2d.VertexBuffer;

/**
 * Fills a shape by tesselating it with the GLU library. This is a slower
 * implementation and {@code FillNonintersectingPolygonVisitor} should be used
 * when possible.
 */
public abstract class AbstractTesselatorVisitor extends SimplePathVisitor {
  protected GLUtessellator tesselator;

  protected GLUtessellatorCallback callback;

  protected boolean contourClosed = true;

  protected int drawMode;
  protected VertexBuffer vBuffer = new VertexBuffer(1024);

  public AbstractTesselatorVisitor() {
    callback = new TessellatorCallback();
  }

  @Override
  public void setStroke(BasicStroke stroke) {
    // nop
  }

  @Override
  public void beginPoly(int windingRule) {
    tesselator = GLU.gluNewTess();
    configureTesselator(windingRule);

    tesselator.gluTessBeginPolygon(null);
  }

  protected void configureTesselator(int windingRule) {
    switch (windingRule) {
    case PathIterator.WIND_EVEN_ODD:
      tesselator.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
      break;

    case PathIterator.WIND_NON_ZERO:
    	tesselator.gluTessProperty(GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_NONZERO);
      break;
    }

    tesselator.gluTessCallback(GLU.GLU_TESS_VERTEX, callback);
    tesselator.gluTessCallback(GLU.GLU_TESS_BEGIN, callback);
    tesselator.gluTessCallback(GLU.GLU_TESS_END, callback);
    tesselator.gluTessCallback(GLU.GLU_TESS_ERROR, callback);
    tesselator.gluTessCallback(GLU.GLU_TESS_COMBINE, callback);
    tesselator.gluTessNormal(0, 0, -1);

  }

  @Override
  public void moveTo(float[] vertex) {
    tesselator.gluTessBeginContour();
    double[] v = new double[] { vertex[0], vertex[1], 0 };
    tesselator.gluTessVertex(v, 0, v);
    contourClosed = false;
  }

  @Override
  public void lineTo(float[] vertex) {
    double[] v = new double[] { vertex[0], vertex[1], 0 };
    tesselator.gluTessVertex(v, 0, v);
  }

  @Override
  public void closeLine() {
    tesselator.gluTessEndContour();
    contourClosed = true;
  }

  @Override
  public void endPoly() {
    // shapes may just end on the starting point without calling closeLine
    if (!contourClosed) {
      closeLine();
    }

    tesselator.gluTessEndPolygon();
    tesselator.gluDeleteTess();
  }

  protected void beginTess(int type) {
    drawMode = type;
    vBuffer.clear();
  }

  protected void addTessVertex(double[] vertex) {
    vBuffer.addVertex((float) vertex[0], (float) vertex[1]);
  }

  protected abstract void endTess();

  protected class TessellatorCallback extends GLUtessellatorCallbackAdapter {
    @Override
    public void begin(int type) {
      beginTess(type);
    }

    @Override
    public void end() {
      endTess();
    }

    @Override
    public void vertex(Object vertexData) {
      assert vertexData instanceof double[] : "Invalid assumption";
      addTessVertex((double[]) vertexData);
    }

    @Override
    public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
      outData[0] = coords;
    }

    @Override
    public void error(int errnum) {
      throw new OpenGLException("Tesselation Error: " + GLU.gluErrorString(errnum));
    }
  }
}
