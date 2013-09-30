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
import java.nio.ByteBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public abstract class AbstractShaderPipeline implements ShaderPipeline {
  protected int vertexShaderId = 0;
  protected int geometryShaderId = 0;
  protected int fragmentShaderId = 0;

  protected String vertexShaderFileName;
  protected String geometryShaderFileName;
  protected String fragmentShaderFileName;

  protected int programId = 0;
  protected int transformLocation = -1;
  protected int colorLocation = -1;

  public AbstractShaderPipeline(String vertexShaderFileName, String geometryShaderFileName, String fragmentShaderFileName) {
    this.vertexShaderFileName = vertexShaderFileName;
    this.geometryShaderFileName = geometryShaderFileName;
    this.fragmentShaderFileName = fragmentShaderFileName;
  }

  @Override
  public void setup() {
    createProgramAndAttach();
    setupUniformsAndAttributes();
  }

  @Override
  public boolean isSetup() {
    return programId > 0;
  }

  public void setColor(FloatBuffer rgba) {
    if (colorLocation >= 0) {
    	GL20.glUniform4(colorLocation, rgba);
    }
  }

  public void setTransform(FloatBuffer glMatrixData) {
    if (transformLocation >= 0) {
    	GL20.glUniformMatrix4(transformLocation, false, glMatrixData);
    }
  }

  protected void createProgramAndAttach() {
    if (GL20.glIsProgram(programId)) {
      delete();
    }

    programId = GL20.glCreateProgram();

    attachShaders();

    GL20.glLinkProgram(programId);
    checkProgramThrowException(programId, GL20.GL_LINK_STATUS);
  }

  protected void setupUniformsAndAttributes() {
    // nop
  }

  protected void attachShaders() {
    if (vertexShaderFileName != null) {
      vertexShaderId = compileShader(GL20.GL_VERTEX_SHADER, getClass(), vertexShaderFileName);
      GL20.glAttachShader(programId, vertexShaderId);
    }

    if (geometryShaderFileName != null) {
      geometryShaderId = compileShader(GL32.GL_GEOMETRY_SHADER, getClass(), geometryShaderFileName);
      GL20.glAttachShader(programId, geometryShaderId);
    }

    if (fragmentShaderFileName != null) {
      fragmentShaderId = compileShader(GL20.GL_FRAGMENT_SHADER, getClass(), fragmentShaderFileName);
      GL20.glAttachShader(programId, fragmentShaderId);
    }
  }

  @Override
  public void use( boolean use) {
    GL20.glUseProgram(use ? programId : 0);
  }

  @Override
  public void delete() {
    GL20.glDeleteProgram(programId);
    deleteShaders();

    programId = 0;
  }

  protected void deleteShaders() {
    if (GL20.glIsShader(vertexShaderId)) {
      GL20.glDeleteShader(vertexShaderId);
      vertexShaderId = 0;
    }
    if (GL20.glIsShader(geometryShaderId)) {
      GL20.glDeleteShader(geometryShaderId);
      geometryShaderId = 0;
    }
    if (GL20.glIsShader(fragmentShaderId)) {
      GL20.glDeleteShader(fragmentShaderId);
      fragmentShaderId = 0;
    }
  }

  protected int compileShader(int type, Class<?> context, String name) throws ShaderException {
    ByteBuffer source = readShader(context, name);
    int id = compileShader(type, source);
    checkShaderThrowException(id);
    return id;
  }

  protected int compileShader(int type, ByteBuffer src) throws ShaderException {
    int id = GL20.glCreateShader(type);

    GL20.glShaderSource(id, src);
    int err = GL11.glGetError();
    if (err != GL11.GL_NO_ERROR) {
      throw new ShaderException("Shader source failed, GL Error: 0x" + Integer.toHexString(err));
    }

    GL20.glCompileShader(id);
    err = GL11.glGetError();
    if (err != GL11.GL_NO_ERROR) {
      throw new ShaderException("Compile failed, GL Error: 0x" + Integer.toHexString(err));
    }

    return id;
  }

  protected ByteBuffer readShader(Class<?> context, String name) throws ShaderException {
    try {
      InputStream stream = null;
      if (context != null) {
        stream = context.getResourceAsStream(name);
      }
      
      if (stream == null) {
        stream = AbstractShaderPipeline.class.getResourceAsStream(name);
      }

      if (stream == null) {
        stream = AbstractShaderPipeline.class.getClassLoader().getResourceAsStream(name);
      }

      if (stream == null) {
        throw new NullPointerException("InputStream for " + name + " is null");
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      StringBuilder data = new StringBuilder(stream.available());
      String line;
      while ((line = reader.readLine()) != null) {
        data.append(line);
        data.append('\n');
      }
      byte[] fullsrc = data.toString().getBytes();
      ByteBuffer outsrc = BufferUtils.createByteBuffer(fullsrc.length);
      outsrc.clear();
      outsrc.put(fullsrc);

      stream.close();
      return outsrc;
    } catch (IOException e) {
      throw new ShaderException("Error reading from stream", e);
    }
  }

  protected void checkShaderThrowException(int shaderId) {
	  int result = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
	  if (result == GL11.GL_TRUE) {
		  return;
	  }

	  int loglen = GL20.glGetShaderi(shaderId, GL20.GL_INFO_LOG_LENGTH);
	  String error = GL20.glGetShaderInfoLog(shaderId, loglen);

    
	  throw new ShaderException(error);
  }

  protected void checkProgramThrowException(int programId, int statusFlag) {
    int result = GL20.glGetProgrami(programId, statusFlag);
    if (result == GL11.GL_TRUE) {
      return;
    }

    int loglen = GL20.glGetShaderi(programId, GL20.GL_INFO_LOG_LENGTH);
	String error = GL20.glGetShaderInfoLog(programId, loglen);
	throw new ShaderException(error);
  }
}
