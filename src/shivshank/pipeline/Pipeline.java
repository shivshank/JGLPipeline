package shivshank.pipeline;

import java.util.List;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

    /**
     * JGLPipeline's central class. Used to setup, render, and cleanup.
     * <p/>
     * <code>Pipeline</code> records all state needed to render. It does not
     * store much of the actual data, like vertices and uniforms; Pipeline
     * only stores references.
     * <p/>
     * In terms of OpenGL, <code>Pipeline</code> couples a ProgramPipeline (or
     * OpenGL Program, if not supported) with VAOs (or their related state, if
     * not supported).
     * <p/>
     * NOTE: Separable programs and VAOs not yet supported.
     */
public class Pipeline {
    
    public static boolean checkGLError() {
        return checkGLError(null);
    }
    
    public static boolean checkGLError(String message) {
        // TODO: Should use actual logging
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
         * @throws PipelineException if shader allocation or compilation fails.
         */
	public static class Shader {
		protected int glName;
        private int glShaderType;
        private boolean autoDelete;
        
        public Shader(int shaderType) {
            this(shaderType, true);
        }
        
        public Shader(int shaderType, boolean autoDelete) {
            glShaderType = shaderType;
            this.autoDelete = autoDelete;
        }
        
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
    private Shader[] shaders;
    
    public Pipeline(Shader ... shaders) {
        this.shaders = shaders;
    }
    
    public void create() {
        glName = glCreateProgram();
        
        if (glName == 0) {
            throw new PipelineException("ERROR: OpenGL failed to create "
                                      + "Program.");
        }
        
        for (Shader c : shaders) {
            if (!c.isCreated()) {
                throw new PipelineException("ERROR: Shader must be created "
                                          + "before program creation!");
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
        shaders = null;
    }
    
    public void draw(Model m) {
        // TODO
    }
}
