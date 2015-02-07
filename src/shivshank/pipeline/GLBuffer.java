package shivshank.pipeline;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL15.*;

/**
 * Represents an OpenGL Buffer Object.
 * <p>
 * Stores the associated name and target.
 * <p>
 * Use GL_ARRAY_BUFFER target for
 * {@link shivshank.pipeline.Model.ShaderInput ShaderInput} data.
 */
public class GLBuffer {
	// TODO: Should GLBuffer store type data?
	
	protected int glName;
	protected int glTarget;
	protected int glUsage;
    
	/**
	 * Unbind the buffer bound to target.
	 * <p>
	 * Binding a buffer to a target is a static/global state change.
	 *  
	 * @param target The target to unbind, such as GL_ARRAY_BUFFER
	 */
	public static void unbind(int target) {
		glBindBuffer(target, 0);
		// TODO: Technically its local to the thread's context... does that
		// 		 need to be handled differently?
	}
    
    /**
     * Create a new GLBuffer.
     *
     * @target The target this object binds to
     * @usage How this object will typically be used by OpenGL
     */
    public GLBuffer(int target, int usage) {
		glTarget = target;
		glName = 0;
		glUsage = usage;
	}
    
    /**
     * Simple version of {@link #GLBuffer(int, int) of constructor}.
     */
	public GLBuffer(int target) {
		this(target, GL_STATIC_DRAW);
	}
    
	/**
	 * Create an OpenGL Buffer object.
	 *  
	 * @throws PipelineException if creation failed
	 */
	public void create() {
		glName = glGenBuffers();
		if (glName == 0)
            throw new PipelineException("Buffer creation failed.");
	}
	
    /**
     * Push and create version of {@link #create()}.
     * 
     * @throws PipelineException if creation failed
     */
    public void create(ByteBuffer data) {
        create();
        push(data);
    }
    
	/**
	 * Reallocate storage for the buffer and push <code>data</code> to the GPU.
	 * 
	 * @param data
	 */
	public void push(ByteBuffer data) {
        bind();
		glBufferData(glTarget, data, glUsage);
        GLBuffer.unbind(glTarget);
	}
	
    /**
     * Float array version of {@link #push(ByteBuffer)}.
     */
    public void push(float[] data) {
        ByteBuffer b = BufferUtils.createByteBuffer(data.length * 4);
        // backing arrays are the same; do not flip the buffer
        b.asFloatBuffer().put(data);
        push(b);
    }

	/**
	 * Update buffer storage without reallocating.
	 * 
	 * @param byteOffset The offset into the storage (in bytes)
	 * @param data The raw data
	 */
	public void update(long byteOffset, ByteBuffer data) {
        bind();
		glBufferSubData(glTarget, byteOffset, data);
        GLBuffer.unbind(glTarget);
	}
	
    /**
     * Float array version of {@link #push(ByteBuffer)}.
     */
    public void update(long byteOffset, float[] data) {
        ByteBuffer b = BufferUtils.createByteBuffer(data.length * 4);
        // backing arrays are the same; do not flip the buffer
        b.asFloatBuffer().put(data);
        update(byteOffset, b);
    }
    
	/**
	 * Free the storage on the GPU.
	 * <p>
	 * If you are just loading new data, don't delete. Instead, reuse the
	 * buffer with {@link #push(ByteBuffer)}.
	 * 
	 */
	public void destroy() {
		glDeleteBuffers(glName);
		glName = 0;
	}
	
    /**
     * Bind the OpenGL object.
     */
	protected void bind() {
		glBindBuffer(glTarget, glName);
	}
}
