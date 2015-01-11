package shivshank.pipeline;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;

/**
 * Represents an OpenGL Buffer Object.
 * </p>
 * Stores the associated name and target.
 */
public class GLBuffer {
	// TODO: Should GLBuffer store type data and convert offsets to bytes for the user?
	
	private int glName;
	private int glTarget;
	private int glUsage;
	
	public GLBuffer(int target) {
		this(target, GL_STATIC_DRAW);
	}
	
	public GLBuffer(int target, int usage) {
		glTarget = target;
		glName = 0;
		glUsage = usage;
	}
	
	/**
	 * Create an OpenGL Buffer object. Does not allocate storage.
	 *  
	 * @return true if OpenGL object creation succeeded.
	 */
	public boolean create() {
		glName = glGenBuffers();
		return glName != 0;
	}
	
	/**
	 * Reallocate storage for the buffer and push <code>data</code> to the GPU.
	 * 
	 * @param data
	 */
	public void push(ByteBuffer data) {
		glBufferData(glTarget, data, glUsage);
	}
	
	/**
	 * Update buffer storage without reallocating.
	 * 
	 * @param offset The offset into the storage (in bytes)
	 * @param data The raw data
	 */
	public void update(long byteOffset, ByteBuffer data) {
		glBufferSubData(glTarget, byteOffset, data);
	}
	
	/**
	 * Free the storage on the GPU.
	 * <p/>
	 * If you are just loading new data, don't delete. Instead, reuse the
	 * buffer with {@link #push(ByteBuffer)}.
	 * 
	 */
	public void destroy() {
		glDeleteBuffers(glName);
		glName = 0;
	}
	
	void bind() {
		glBindBuffer(glTarget, glName);
	}
	
	/**
	 * Unbind the buffer bound to target.
	 * <p/>
	 * Binding a buffer to a target is a static/global state change.
	 *  
	 * @param target An integer constant such as GL_ARRAY_BUFFER
	 */
	static void unbind(int target) {
		glBindBuffer(target, 0);
		// TODO: Technically its local to the thread's context... does that
		// 		 need to be handled differently?
	}
}
