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

import org.jogamp.glg2d.impl.BasicStrokeLineVisitor;

public class GL2ES2StrokeLineVisitor extends BasicStrokeLineVisitor implements ShaderPathVisitor {
  protected GLContext context;
  protected UniformBufferObject uniforms;

  protected AnyModePipeline pipeline;

  public GL2ES2StrokeLineVisitor() {
    this(new AnyModePipeline());
  }

  public GL2ES2StrokeLineVisitor(AnyModePipeline pipeline) {
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
    super.setStroke(stroke);
  }

  @Override
  public void beginPoly(int windingRule) {
    pipeline.use(true);
    pipeline.setTransform(uniforms.transformHook.getGLMatrixData());
    pipeline.setColor(uniforms.colorHook.getRGBA());

    super.beginPoly(windingRule);
  }

  @Override
  public void endPoly() {
    super.endPoly();

    pipeline.use(false);
  }

  @Override
  protected void drawBuffer() {
    FloatBuffer buf = vBuffer.getBuffer();
    if (buf.position() == 0) {
      return;
    }

    buf.flip();

    pipeline.draw(GL11.GL_TRIANGLE_STRIP, buf);

    vBuffer.clear();
  }
}
