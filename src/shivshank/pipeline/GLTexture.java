package shivshank.pipeline;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Represents an OpenGL 2D Texture.
 * <p>
 * Stores the associated name and target.
 * <p>
 * Before a texture is used it MUST be created and configured. Create the object
 * first and then call the appropriate configuration methods.
 * <p>
 * After configuration, push a texture image.
 * <p>
 * To use a GLTexture, capture it with a model or render manually by calling
 * {@link #enable(int, int)} and {@link #disable()}.
 * <p>
 * If you create a texture, be sure to specify the number of mipmaps and upload
 * the proper amount, otherwise the texture object is incomplete and cannot be
 * rendered.
 * <p>
 * This class is designed for use with 2D textures. It is recommended to extend
 * it if you want to support other targets.
 */
public class GLTexture {
    protected int glName;
    protected int glTarget;
    private boolean configuring = false;
    
    /**
	 * Unbind the buffer bound to target.
	 * <p>
	 * Binding a buffer to a target is a static/global state change.
	 *  
	 * @param target The target to unbind, such as GL_ARRAY_BUFFER
	 */
    public static void unbind(int target) {
        glBindTexture(target, 0);
    }
    
    /**
     * Create a new GLTexture.
     *
     * @param target the target to this texture will bind to; specifies
     *               dimensions
     */
    public GLTexture(int target) {
        glTarget = target;
    }
    
    /**
     * Create the OpenGL Texture Object.
     * <p>
     * Must be called before configuring the texture.
     */
    public void create() {
        int name = glGenTextures();
    }
    
    /**
     * Bind the texture if it isn't already bound.
     * <p>
     * Calling this function is optional. It is public for the sake of symmetry.
     * <p>
     * Beware, manually binding another texture while this texture is being
     * configured will invalidate the configuring flag on this object.
     */
    public void configureBegin() {
        if (!configuring) {
            bind();
            configuring = true;
        }
    }
    
    /**
     * Set how OpenGL interprets the pixels in uploaded textures.
     * <p>
     * Packed alignment means that all rows are consecutive, with no padding.
     * <p>
     * Pixel alignment does not affect padding between pixels. It affects
     * padding between rows. An alignment of 4 means that a rows is padded to
     * have a length divisible by 4 (length % 4 == 0).
     *
     * @param alignment 1 for packed, 2 for even, 4 for word, 8 for double-word
     */
    public static void configurePixelAlignment(int alignment) {
        glPixelStorei(GL_UNPACK_ALIGNMENT, alignment);
        glPixelStorei(GL_PACK_ALIGNMENT, alignment);
    }
    
    /**
     * Set how this texture's mipmaps should be filtered.
     */
    public void configureFiltering(int minFilter, int magFilter) {
        configureBegin();
        glTexParameteri(glTarget, GL_TEXTURE_MAG_FILTER, magFilter);
        glTexParameteri(glTarget, GL_TEXTURE_MIN_FILTER, minFilter);
    }
    
    /**
     * Set how many mipmaps this texture has, minLevel being the base level.
     *
     * @param minLevel the base level, inclusive
     * @param maxLevel the smallest mipmap, inclusive
     */
    public void configureMipMapLevels(int minLevel, int maxLevel) {
        configureBegin();
        glTexParameteri(glTarget, GL_TEXTURE_BASE_LEVEL, minLevel);
        glTexParameteri(glTarget, GL_TEXTURE_MAX_LEVEL, maxLevel);
    }
    
    /**
     * Configure the behavior of texture coordinates.
     * <p>
     * The horizontal texture coordinate axis may be called the x, s, or u axis
     * and the vertical axis may be called the y, t, or v axis.
     *
     * @param glWrapX a value such as GL_REPEAT
     * @param glWrapY see <code>glWrapX</code> above
     */
    public void configureCoordWrapping(int glWrapX, int glWrapY) {
        configureBegin();
        glTexParameteri(glTarget, GL_TEXTURE_WRAP_S, glWrapX);
        glTexParameteri(glTarget, GL_TEXTURE_WRAP_T, glWrapY);
    }
    
    /**
     * Generic OpenGL Texture configuration function.
     *
     * @param param the OpenGL parameter
     * @param value the integer value to set for that parameter
     */
    public void configureIntParam(int param, int value) {
        configureBegin();
        glTexParameteri(glTarget, param, value);
    }
    
    /**
     * Push a texture level stored in pixels to the GPU.
     *
     * @param pixels the pixel data
     * @param width the width of the texture, in pixels
     * @param height the height of the texture, in pixels
     * @param mipmapLevel the level to upload
     * @param glType the type of each component
     * @param glPixelFormat the layout of memory, such as BGR, RGB, etc
     */
    public void push(ByteBuffer pixels, int width, int height, int mipmapLevel,
                     int glType, int glPixelFormat) {
        configureBegin();
        glTexImage2D(glTarget, mipmapLevel, GL_RGBA8,
                     width, height, 0, glPixelFormat, glType, pixels);
        configureEnd();
    }
    
    /**
     * @return true if this texture is supposed to be bound
     */
    public boolean isConfiguring() {
        return configuring;
    }
    
    /**
     * Unbind the texture if a configure function was called previously.
     * <p>
     * Calling this is optional if this texture is pushed directly after
     * configuration.
     *
     * @see #push(ByteBuffer, int, int, int, int, int)
     */
    public void configureEnd() {
        if (configuring) {
            GLTexture.unbind(glTarget);
            configuring = false;
        }
    }
    
    /**
     * Bind this a sampler and this texture to a Texture Unit.
     */
    protected void enable(int textureUnit, int samplerLocation) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        bind();
        glUniform1i(samplerLocation, textureUnit);
    }
    
    /**
     * Unbind this texture.
     */
    protected void disable() {
        // TODO: How to properly unbind after binding?
        // This setup seemingly allows for targets to be left bound on
        // different texture units. Does it *really* matter?
        GLTexture.unbind(glTarget);
    }
    
    /**
     * Free this texture's OpenGL storage.
     */
    public void destroy() {
        glDeleteTextures(glName);
        glName = 0;
    }
    
    /**
     * Bind this texture.
     */
    protected void bind() {
        glBindTexture(glTarget, glName);
    }
}
