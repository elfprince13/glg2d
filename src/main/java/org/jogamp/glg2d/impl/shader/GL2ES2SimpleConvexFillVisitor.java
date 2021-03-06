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
import org.lwjgl.opengl.GLContext;

import org.jogamp.glg2d.VertexBuffer;
import org.jogamp.glg2d.impl.SimplePathVisitor;

public class GL2ES2SimpleConvexFillVisitor extends SimplePathVisitor implements ShaderPathVisitor {
  protected GLContext context;
  protected UniformBufferObject uniforms;

  protected VertexBuffer vBuffer = new VertexBuffer(1024);

  protected AnyModePipeline pipeline;

  public GL2ES2SimpleConvexFillVisitor() {
    this(new AnyModePipeline());
  }

  public GL2ES2SimpleConvexFillVisitor(AnyModePipeline pipeline) {
    this.pipeline = pipeline;
  }

  @Override
  public void setGLContext(GLContext glContext, UniformBufferObject uniforms) {
    setGLContext(glContext);

    this.uniforms = uniforms;
  }

  @Override
  public void setGLContext(GLContext ctx) {
    context = ctx;
    if (!pipeline.isSetup()) {
      pipeline.setup();
    }
  }

  @Override
  public void setStroke(BasicStroke stroke) {
    // nop
  }

  @Override
  public void beginPoly(int windingRule) {
    // do we need to care about winding rule?
    pipeline.use(true);

    pipeline.setColor(uniforms.colorHook.getRGBA());
    pipeline.setTransform(uniforms.transformHook.getGLMatrixData());

    vBuffer.clear();
    vBuffer.addVertex(0, 0);
  }

  @Override
  public void moveTo(float[] vertex) {
    draw();

    vBuffer.addVertex(vertex[0], vertex[1]);
  }

  @Override
  public void lineTo(float[] vertex) {
    vBuffer.addVertex(vertex[0], vertex[1]);
  }

  @Override
  public void closeLine() {
    FloatBuffer buf = vBuffer.getBuffer();
    float x = buf.get(2);
    float y = buf.get(3);
    vBuffer.addVertex(x, y);
  }

  @Override
  public void endPoly() {
    draw();
    pipeline.use(false);
  }

  protected void draw() {
    FloatBuffer buf = vBuffer.getBuffer();
    if (buf.position() <= 2) {
      buf.position(2);
      return;
    }

    buf.flip();

    setupCentroid(buf);

    pipeline.draw(GL11.GL_TRIANGLE_FAN, buf);

    vBuffer.clear();
    vBuffer.addVertex(0, 0);
  }

  protected void setupCentroid(FloatBuffer vertexBuffer) {
    float x = 0;
    float y = 0;

    vertexBuffer.position(2);
    int numPts = 0;
    while (vertexBuffer.position() < vertexBuffer.limit()) {
      x += vertexBuffer.get();
      y += vertexBuffer.get();
      numPts++;
    }

    vertexBuffer.rewind();
    vertexBuffer.put(x / numPts);
    vertexBuffer.put(y / numPts);

    vertexBuffer.rewind();
  }
}
