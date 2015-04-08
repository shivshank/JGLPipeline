# JGLPipeline

>
> **NOTE**: In general, I've given up on this for now. Using and testing this library was a complete failure. It only increased code indirection and debugging frustration. To actually be of use, the library needs to be restructured to be more high level. Until then, I wouldn't reccomend actually using it.
>

JGLPipeline is a simple OpenGL Java wrapper. The library is designed to abstract away some of the OpenGL API, so it can be used even if you have minimal knowledge. In addition, it can be used in tandem with custom LWJGL code and the library is easily extended.

JGLPipeline is built on LWJGL 3 and requires OpenGL 2.1 or greater. Immediate mode is not used in favor of more modern practices. JGLPipeline depends on LWJGL alone, but you will also need to use some things (such as OpenGL constant "enums") directly though LWJGL.

## High Level Overview (I've written this almost like a OpenGL tutorial ;D)

OpenGL is a graphics API for 2D and 3D drawing. It is a C-based API, so mapping it to an Object Oriented Framework can be difficult. As a result, the library does its best to avoid coupling, but it can be a bit clunky in some places.

**JGLPipeline** attempts to provide you with the high level tools to get going with rendering quickly, easily, and efficiently.

Modern OpenGL primarily uses Buffers to communicate to the graphics card (**GLBuffer** in JGLP). Buffers are basically big (nondynamic) byte arrays that live on the GPU; your job is to explain to the renderer how to interpret those bytes.

OpenGL uses the (creatively named) OpenGL Shading Language (**GLSL**) to interpret the Buffers in programs known as shaders. There are several different types of programs that comprise the whole rendering pipeline, the two most important ones being the Vertex Shader and the Fragment Shader.

So far, you as the programmer already have several responsibilities: organize and format your data into big byte arrays (buffers); tell the shaders about those byte arrays; and create shaders that can interpret the buffers.

In abstract psuedo code:
- Create the buffers
- Create the shaders
- Specify the buffer format for the shaders such that your shaders can understand the buffers

Following the OpenGL object paradigm, almost every JGLP object has prepare, create, configure, push, update, and destroy methods (amongst two important others: enable and disable).

JGLPipeline's lowest level class so far is GLBuffer. OpenGL buffers have "targets", which vaguely tell OpenGL how to use them. Being low level, they are very simple. All you need to do is call the new buffers create method, and push it some data in the form of a nio.ByteBuffer (or several convience formats, like float[]).

> NOTE: Use LWJGL's BufferUtils to create native ByteBuffers (see example in JGLP in GLBuffer).

Now you need something that can recieve the Buffers, this being a **Pipeline.Shader**. ///// TODO

In effect, shaders draw models (groups of geometry), so JGLPipeline uses the **Model** and **Model.ShaderInput** classes to interface with Shaders.

///// TODO: Talk about textures, then talk about uniforms, then talk about extending

In summary, the whole process looks like this:

- Create the Pipeline
- Create the Shaders
- Setup the GLBuffers
- Setup the Textures
- Setup the Uniforms
- Create the Model, format it

## Design

The library has two main classes: Pipeline and Model. These are built around GLTexture and GLBuffer.

Classes are designed to represent the API as best as possible, and so avoid keeping around a lot of state, since most of it can be quiered through OpenGL. Only state that a user reasonably needs access to (for speed and necessity) should be kept around by JGLP.
