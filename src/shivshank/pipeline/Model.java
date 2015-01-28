package shivshank.pipeline;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Model {

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
             * Enable relevant shader input in OpenGL and bind that input
             * to the current GL_ARRAY_BUFFER.
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
    
    private HashMap<GLBuffer, ShaderInput> captures;
    private int count;
    
    public Model() {
        captures = new HashMap<GLBuffer, ShaderInput>();
    }
    
    public void create() {
    }
    
    public void capture(GLBuffer buffer, ShaderInput in) {
        captures.put(buffer, in);
    }
    
    public void setCount(int c) {
        count = c;
    }
    
    public int getCount() {
        return count;
    }
    
    public void destroy() {
    }
    
    protected void enable() {
        for (Map.Entry<GLBuffer, ShaderInput> e : captures.entrySet()) {
            e.getKey().bind();
            e.getValue().enable();
        }
        GLBuffer.unbind(GL_ARRAY_BUFFER);
    }
    
    protected void disable() {
        for (Map.Entry<GLBuffer, ShaderInput> e : captures.entrySet()) {
            e.getValue().disable();
        }
    }
}