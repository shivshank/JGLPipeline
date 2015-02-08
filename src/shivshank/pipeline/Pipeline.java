package shivshank.pipeline;

import java.util.List;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * JGLPipeline's central class. Used to setup, render, and cleanup.
 * <p>
 * <code>Pipeline</code> represents an OpenGL Program.
 * <p>
 * Before calling create, prepare the Pipeline by binding ShaderInputs to
 * locations, see {@link #prepareInputLocation(String, int)}.
 * <p>
 * To render with a Pipeline, the Pipeline needs to be linked with shaders. Link
 * with shaders by calling {@link #create(Shader ...) create}.
 * <p>
 * The default draw call used by Pipeline is simply glDrawArrays. For more
 * elaborate draw calls, override the method {@link #draw(Model)}, and possibly
 * {@link #render(Model)}.
 */
public class Pipeline {
    
    /**
     * Message-less version of {@link #checkGLError(String)}.
     */
    public static boolean checkGLError() {
        return checkGLError(null);
    }
    
    /**
     * Check if there are OpenGL errors and print them along with
     * <code>message</code>.
     * <p>
     * Note: LWJGL contains a method with the same name, but uses exceptions.
     *
     * @return true if at least one error occured.
     */
    public static boolean checkGLError(String message) {
        // LWJGL also contains a method for this...
        if (message == null)
            message = "OPENGL ERROR: ";

        boolean occured = false;
        int error;        
        while ( (error = glGetError()) != GL_NO_ERROR ) {
            occured = true;
            switch (error) {
                case GL_INVALID_ENUM:
                    System.err.println(message + "Invalid Enum");
                    break;
                case GL_INVALID_VALUE:
                    System.err.println(message + "Invalid Value");
                    break;
                case GL_INVALID_OPERATION:
                    System.err.println(message + "Invalid Operation");
                    break;
                case GL_STACK_OVERFLOW:
                    System.err.println(message + "Stack Overflow");
                    break;
                case GL_STACK_UNDERFLOW:
                    System.err.println(message + "Stack Underflow");
                    break;
                case GL_OUT_OF_MEMORY:
                    System.err.println(message + "Out of Memory");
                    break;
                default:
                    System.err.println(message + "Unknown.");
            }
        }
        return occured;
    }
    
    /**
     * Represents an OpenGL Shader.
     * <p>
     * The location of an input (also known as an "attribute" for vertex shaders
     * and a "varrying" for fragment shaders) is determined either in the shader
     * source, a call to {@link Pipeline#prepareInputLocation(String, int)}
     * or by the driver.
     */
	public static class Shader {
		protected int glName;
        protected int glShaderType;
        private boolean autoDelete;
        
        /**
         * Create a new shader that will only be used in one program.
         * <p>
         * After program creation, this shader will be automatically
         * invalidated. Continuing to use this object will result in undefined
         * behavior.
         * <p>
         * Pass false as second parameter to disable this behavior.
         *
         * @param shaderType the OpenGL shader type, such as GL_VERTEX_SHADER
         */
        public Shader(int shaderType) {
            this(shaderType, true);
        }
        
        /**
         * Create a shader object that can be used with many programs.
         * <p>
         * To use this with many programs, the second argument must be false.
         *
         * @param shaderType the OpenGL shader type, such as GL_VERTEX_SHADER
         * @param autoDelete enable/disable auto invalidation after linking
         */
        public Shader(int shaderType, boolean autoDelete) {
            glShaderType = shaderType;
            this.autoDelete = autoDelete;
        }
        
        /**
         * Create the OpenGL portion of the shader and upload the source code.
         *
         * @throws PipelineException if shader allocation or compilation fails.
         */
        public void create(String source) {
            glName = glCreateShader(glShaderType);
            
            if (glName == 0) {
                throw new PipelineException("ERROR: OpenGL failed to create "
                                           + "shader.");
            }

            glShaderSource(glName, source);
            glCompileShader(glName);
            if (glGetShaderi(glName, GL_COMPILE_STATUS) != GL_TRUE) {
                String infolog = glGetShaderInfoLog(glName);
                throw new PipelineException("ERROR: OpenGL failed to compile "
                    + "shader:\n" + infolog);
            }
        }
        
        public boolean isCreated() {
            return glName != 0;
        }
            
        /**
         * Free the shader.
         */
        public void destroy() {
            glDeleteShader(glName);
        }
        
        boolean autoDestroy() {
            return autoDelete;
        }
	}
	
    protected int glName;
    
    /**
     * Create a new Pipeline.
     */
    public Pipeline() {
    }
    
    /**
     * Bind a ShaderInput to a user defined location/index.
     * <p>
     * This must be called before create. Calling this method contrasts with
     * using {@link #getInputLocation(String) getInputLocation} to query
     * the locations automatically assigned by the driver.
     * 
     * @param inputName the ShaderInput name to use this index
     * @param index the location GLSL should use for in/attribute inputName
     * @see shivshank.pipeline.Model.ShaderInput
     */
    public void prepareInputLocation(String inputName, int index) {
        glBindAttribLocation(glName, index, inputName);
    }
    
    /**
     * Create an OpenGL program and attach and link Shaders to it.
     * <p>
     * Automatically deletes qualifying Shaders, see
     * {@link Shader#Shader(int, boolean)}.
     */
    public void create(Shader ... shaders) {
        glName = glCreateProgram();
        
        if (glName == 0) {
            throw new PipelineException("ERROR: OpenGL failed to create "
                                      + "Program.");
        }
        
        for (Shader c : shaders) {
            if (!c.isCreated()) {
                throw new PipelineException("ERROR: Shader must be created "
                                          + "before program creation! "
                                          + "If you are reusing a shader, make "
                                          + "sure to set autoDelete parameter "
                                          + "on construction.");
            }
            glAttachShader(glName, c.glName);
            if (c.autoDestroy())
                c.destroy();
        }
        
        glLinkProgram(glName);
        int status = glGetProgrami(glName, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String log = glGetProgramInfoLog(glName);
            destroy();
            throw new PipelineException("ERROR: Program linking failed:\n"
                         + log);
        }
        
        for (Shader c : shaders) {
            glDetachShader(glName, c.glName);
        }
    }
    
    public int getUniformLocation(String uniform) {
        return glGetUniformLocation(glName, uniform);
    }
    
    public int getInputLocation(String inputName) {
        return glGetAttribLocation(glName, inputName);
    }
    
    /**
     * Render a Model.
     *
     * @param m the Model to be rendered using its associated textures and
     *          buffers.
     */
    public void render(Model m) {
        glUseProgram(glName);
        m.enable();
        
        draw(m);
        
        m.disable();
        glUseProgram(0);
    }
    
    /**
     * Override this method to customize the draw call.
     * <p>
     * The default draw call is glDrawArrays.
     *
     * @param m the model that needs to be drawn
     */
    protected void draw(Model m) {
        glDrawArrays(GL_TRIANGLES, 0, m.getCount());
    }
    
    /**
     * Destroys the OpenGL program.
     */
    public void destroy() {
        glDeleteProgram(glName);
        glName = 0;
    }
}
