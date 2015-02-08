# JGLPipeline

JGLPipeline is a simple OpenGL Java wrapper. The library is designed to abstract away some of the OpenGL API, so it can be used even if you have minimal knowledge. In addition, it can be used in tandem with custom LWJGL code and the library is easily extended.

JGLPipeline is built on LWJGL 3 and requires OpenGL 2.1 or greater. Immediate mode is not used in favor of more modern practices.

## OpenGL (What you need to know)

Modern OpenGL uses shaders. Instead of drawing directly to the screen, you tell OpenGL where your data is and how to use it.

To use this library, you need to be able to write shaders in GLSL, OpenGLs shading language (which looks a lot like C). The two main types of shaders that you need to get started are the fragment shader and the vertex shader.

Vertex shaders can take a series of inputs, known as vertex attributes or more generally as shader inputs. The job of the vertex shader is to output a vertex position in Clip Space.

TODO: Talk about Fragment Shader, Uniforms, Textures, and Buffers.

## Design

The library has two main classes: Pipeline and Model.

The Pipeline class manages all of your Shaders. You use it to render Model objects. Pipeline is not an abstract class; by default it comes ready to use with a simple glDrawArrays(GL_TRIANGLES) call (which draws everything associated with the model).
