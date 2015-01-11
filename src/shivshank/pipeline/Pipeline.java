package shivshank.pipeline;

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
         * Used to reference the layout and storage of vertex shader inputs.
         * <p/>
         * Shader inputs are also known as vertex attributes. A ShaderInput 
         * is independent of its source. Buffer bindings are created at capture
         * time.
         * <p/>
         * The state of a <code>ShaderInput</code> is typically used for calls to
         * glVertexAttribPointer.
         */
	public static class ShaderInput {
		
        private final int comps;
        private final int type;
        private final int offset;
        private final int stride;
        private final boolean normalized;
        private int index;
        
            /**
             * Densely packed ShaderInput constructor.
             */
        public ShaderInput(int numComponents, int compType) {
            this(numComponents, compType, 0, 0, false);
        }
        
            /**
             * Custom Shader Input source layout
             */
        public ShaderInput(int numComponents, int compType, int offset,
                           int stride, boolean normalized) {
            comps = numComponents;
            type = compType;
            this.offset = offset;
            this.stride = stride;
            this.normalized = normalized;
        }
        
            /**
             * Assign an index; prepare this ShaderInput for capture.
             * <p/>
             * This must be called before capture.
             *
             * @return this for assignment convenience
             */
        public ShaderInput create(int index) {
            this.index = index;
            return this;
        }
        
            /**
             * Invalidate this object for capture; set index to 0.
             */
        public void destroy() {
            index = 0;
        }
        
            /**
             * Enable relevant shader input in OpenGL.
             */
        public void enable() {
            glEnableVertexAttribArray(index);
            glVertexAttribPointer(index, comps, type,
                                       normalized, offset, stride);
        }
        
            /**
             * Disable relevant shader input in OpenGL.
             */
        public void disable() {
            glDisableVertexAttribArray(index);
        }
        
        public boolean isCreated() {
            return index != 0;
        }
	}
    
    
	public static class Shader {
		
	}
	
}
