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


import java.nio.FloatBuffer;

import org.lwjgl.opengl.GLContext;

import org.jogamp.glg2d.impl.AbstractTesselatorVisitor;

public class GL2ES2TesselatingVisitor extends AbstractTesselatorVisitor implements ShaderPathVisitor {
  protected GLContext context;
  protected UniformBufferObject uniforms;

  protected AnyModePipeline pipeline;

  public GL2ES2TesselatingVisitor() {
    this(new AnyModePipeline());
  }

  public GL2ES2TesselatingVisitor(AnyModePipeline pipeline) {
    this.pipeline = pipeline;
  }

  @Override
  public void setGLContext(GLContext ctx) {
    context = ctx;

    if (!pipeline.isSetup()) {
      pipeline.setup();
    }
  }

  @Override
  public void setGLContext(GLContext glContext, UniformBufferObject uniforms) {
    setGLContext(glContext);
    this.uniforms = uniforms;
  }

  @Override
  public void beginPoly(int windingRule) {
    pipeline.use(true);

    super.beginPoly(windingRule);

    pipeline.setColor(uniforms.colorHook.getRGBA());
    pipeline.setTransform(uniforms.transformHook.getGLMatrixData());
  }

  @Override
  public void endPoly() {
    super.endPoly();

    pipeline.use(false);
  }

  @Override
  protected void endTess() {
    FloatBuffer buf = vBuffer.getBuffer();
    buf.flip();

    pipeline.draw(drawMode, buf);
  }
}
