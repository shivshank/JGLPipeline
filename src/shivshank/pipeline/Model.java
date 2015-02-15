package shivshank.pipeline;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * A Model relates ShaderInputs to Buffers, and Textures to Uniforms.
 * <p>
 * {@link Pipeline} uses a Model to render state. Simply create the necessary
 * {@link GLTexture}s and {@link GLBuffer}s and then use a Model
 * object to capture them.
 * <p>
 * Capturing objects establishes their relationships. To relate a ShaderInput
 * (which is the vertex shader's attribute) to (part of) an OpenGL Buffer, use
 * {@link capture(ShaderInput, GLBuffer)}. ShaderInputs have stride and offset
 * parameters, meaning that one GLBuffer can store several different attributes.
 * <p>
 * When this model is rendered, the captures tell GLSL where to find its data.
 * <p>
 * Textures bind differently. GLSL uses uniform samplers to represent textures.
 * Instead of binding a client side texture directly to a sampler, OpenGL binds
 * textures to texture units. A sampler is then associated with a texture unit.
 * A sampler accesses a texture via its binding to a texture unit.
 * <p>
 * A Model is similar to a VAO but also manages textures.
 */
public class Model {

    /**
     * References the layout and storage of vertex shader inputs.
     * <p>
     * Shader inputs are also known as vertex attributes. A ShaderInput 
     * is independent of its source. Buffer bindings are created at capture
     * time.
     * <p>
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
        boolean created;
        
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
         * <p>
         * This must be called before capture.
         *
         * @return this for assignment convenience
         */
        public ShaderInput create(int index) {
            this.index = index;
            created = true;
            return this;
        }
    
        /**
         * Invalidate this object for capture.
         */
        public void destroy() {
            created = false;
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
        
        public String toString() {
            return "vattrib " + index + " o>> " + offset + " s__ " + stride;
        }
    }
    
    /**
     * Used to store sampler locations and relevant texture unit.
     */
    private static class TextureInput {
    
        int texUnit;
        int samplerPos;
        
        TextureInput(int unit, int sampler) {
            texUnit = unit;
            samplerPos = sampler;
        }
    }
    
    private HashMap<ShaderInput, GLBuffer> vboCaptures;
    private HashMap<GLTexture, TextureInput> texCaptures;
    private int count;
    private GLBuffer elements;

    /**
     * Create a new model ready for capturing.
     */
    public Model() {
        vboCaptures = new HashMap<ShaderInput, GLBuffer>();
        texCaptures = new HashMap<GLTexture, TextureInput>();
    }
    
    /**
     * Exists for consistency and forward compatibility. Does nothing.
     */
    public void create() {
    }
    
    /**
     * Relate a ShaderInput to a GLBuffer.
     * <p>
     * The render call will use the ShaderInput interpret the data in the
     * GLBuffer.
     */
    public void capture(ShaderInput in, GLBuffer buffer) {
        if (in.created == false) {
            throw new PipelineException("Shader input was not created: "
                                   + in.toString());
        }

        vboCaptures.put(in, buffer);
    }
    
    /**
     * Relate a texture and sampler to a texture unit.
     */
    public void capture(GLTexture tex, int texUnit, int samplerPos) {
        texCaptures.put(tex, new TextureInput(texUnit, samplerPos));
    }
    
    /**
     * Set an element array buffer for this Model.
     * <p>
     * Does nothing by default. This method is meant to be used
     * by a custom overriden draw method in a Pipeline.
     */
    public void setElementBuffer(GLBuffer elements) {
        this.elements = elements;
    }
    
    /**
     * Bind an Element Array Buffer.
     */
    public void enableElementBuffer() {
        if (elements == null)
            throw new PipelineException("Model has no element buffer (null).");
        elements.bind();
    }
    
    /**
     * Unbind an Element Array Buffer.
     */
    public void disableElementBuffer() {
        if (elements == null)
            throw new PipelineException("Model has no element buffer (null).");
        GLBuffer.unbind(GL_ELEMENT_ARRAY_BUFFER);
    }
    
    /**
     * Set the number of vertices.
     * <p>
     * This value is used in the draw call.
     */
    public void setCount(int c) {
        count = c;
    }
    
    /**
     * Get the number of vertices.
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Exists for consistency and forward compatibility. Does nothing.
     */
    public void destroy() {
    }
    
    /**
     * Ready the associated textures, samplers, and buffers for rendering.
     */
    protected void enable() {
        for (Map.Entry<ShaderInput, GLBuffer> e : vboCaptures.entrySet()) {
            e.getValue().bind();
            e.getKey().enable();
        }
        for (Map.Entry<GLTexture, TextureInput> e : texCaptures.entrySet()) {
            TextureInput i = e.getValue();
            e.getKey().enable(i.texUnit, i.samplerPos);
        }
        GLBuffer.unbind(GL_ARRAY_BUFFER);
    }
    
    /**
     * Cleanup the associated textures, samplers, and buffers after rendering.
     */
    protected void disable() {
        for (Map.Entry<ShaderInput, GLBuffer> e : vboCaptures.entrySet()) {
            e.getKey().disable();
        }
        for (Map.Entry<GLTexture, TextureInput> e : texCaptures.entrySet()) {
            e.getKey().disable();
        }
    }
}
