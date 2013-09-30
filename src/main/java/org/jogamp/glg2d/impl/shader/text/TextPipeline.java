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


import org.lwjgl.opengl.GL20;

import org.jogamp.glg2d.impl.shader.AnyModePipeline;


public class TextPipeline extends AnyModePipeline {
  protected int xOffsetLocation = -1;
  protected int yOffsetLocation = -1;

  public TextPipeline() {
    this("TextShader.v", "FixedFuncShader.f");
  }

  public TextPipeline(String vertexShaderFilename, String fragmentShaderFilename) {
    super(vertexShaderFilename, fragmentShaderFilename);
  }

  public void setLocation(float x, float y) {
    if (xOffsetLocation >= 0) {
      GL20.glUniform1f(xOffsetLocation, x);
    }

    if (yOffsetLocation >= 0) {
      GL20.glUniform1f(yOffsetLocation, y);
    }
  }

  @Override
  protected void setupUniformsAndAttributes() {
    super.setupUniformsAndAttributes();

    xOffsetLocation = GL20.glGetUniformLocation(programId, "u_xoffset");
    yOffsetLocation = GL20.glGetUniformLocation(programId, "u_yoffset");
  }
}
