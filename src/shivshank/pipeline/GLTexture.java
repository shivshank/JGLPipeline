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
 * the proper amount, otherwise the texture is incomplete and cannot be
 * rendered.
 * <p>
 * This class is designed for use with 2D textures. It is recommended to 
 */
public class GLTexture {
    private int glName;
    private int glTarget;
    private boolean configuring = false;
    
    public GLTexture(int target) {
        glTarget = target;
    }
    
    /**
     * Create the OpenGL Texture Object.
     * <p>
     * Must be called before configuring the texture.
     */
    public void create(ByteBuffer pixels, int width, int height,
                       int glPixelType, int gl) {
        int name = glGenTextures();
    }
    
    public static void configurePixelAlignment(int a) {
        glPixelStorei(GL_UNPACK_ALIGNMENT, a);
    }
    
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
     */
    public void push(ByteBuffer pixels, int width, int height, int mipmapLevel,
                     int glType, int glPixelFormat) {
        configureBegin();
        glTexImage2D(glTarget, mipmapLevel, GL_RGBA8,
                     width, height, 0, glPixelFormat, glType, pixels);
        configureEnd();
    }
    
    /**
     * Unnecessary if texture is pushed directly after configuration.
     * <p>
     * Unbinds the texture if it was bound before.
     *
     * @see #push(ByteBuffer, int, int, int, int, int)
     */
    public void configureEnd() {
        if (configuring) {
            GLTexture.unbind(glTarget);
            configuring = false;
        }
    }
    
    public void enable(int textureUnit, int samplerLocation) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        bind();
        glUniform1i(samplerLocation, textureUnit);
    }
    
    public void disable() {
        // TODO: How to properly unbind after binding?
        // This setup seemingly allows for targets to be left bound on
        // different texture units. Does it *really* matter?
        GLTexture.unbind(glTarget);
    }
    
    public void destroy() {
        glDeleteTextures(glName);
        glName = 0;
    }
    
    protected void bind() {
        glBindTexture(glTarget, glName);
    }
    
    protected static void unbind(int target) {
        glBindTexture(target, 0);
    }
    
    protected void configureBegin() {
        if (!configuring) {
            bind();
            configuring = true;
        }
    }
}
