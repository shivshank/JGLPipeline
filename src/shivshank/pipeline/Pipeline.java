package shivshank.pipeline;

import java.util.List;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * JGLPipeline's central class. Used to setup, render, and cleanup.
 * <p>
 * <code>Pipeline</code> records all state needed to render. It does not
 * store much of the actual data, like vertices and uniforms; Pipeline
 * only stores references.
 * <p>
 * In terms of OpenGL, <code>Pipeline</code> couples a ProgramPipeline (or
 * OpenGL Program, if not supported) with VAOs (or their related state, if
 * not supported).
 * <p>
 * NOTE: Separable programs and VAOs not yet supported.
 */
public class Pipeline {
    
    public static boolean checkGLError() {
        return checkGLError(null);
    }
    
    public static boolean checkGLError(String message) {
        // TODO: Should use actual logging?
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
    
	public static class Shader {
		protected int glName;
        private int glShaderType;
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
            
            if (glName == 0)
                throw new PipelineException("ERROR: OpenGL failed to create "
                                           + "shader.");

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
	
    private int glName;
    
    public Pipeline() {
    }
    
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
    
    public void destroy() {
        glDeleteProgram(glName);
        glName = 0;
    }
    
    public void render(Model m) {
        glUseProgram(glName);
        m.enable();
        
        draw(m);
        
        m.disable();
        glUseProgram(0);
    }
    
    public void draw(Model m) {
        glDrawArrays(GL_TRIANGLES, 0, m.getCount());
    }
}
