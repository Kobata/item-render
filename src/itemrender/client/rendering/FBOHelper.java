package itemrender.client.rendering;

import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;

public class FBOHelper {
    public int renderTextureSize = 128;
    public int framebufferID = -1;
    public int depthbufferID = -1;
    public int textureID = -1;

    private IntBuffer lastViewport;
    private int lastTexture;
    private int lastFramebuffer;

    public FBOHelper(int textureSize) {
        renderTextureSize = textureSize;

        createFramebuffer();
    }

    public void resize(int newSize) {
        deleteFramebuffer();
        renderTextureSize = newSize;
        createFramebuffer();
    }

    public void begin() {
        // Remember current framebuffer.
        lastFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        // Render to our texture
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);

        // Remember viewport info.
        lastViewport = GLAllocation.createDirectIntBuffer(16);
        GL11.glGetInteger(GL11.GL_VIEWPORT, lastViewport);
        GL11.glViewport(0, 0, renderTextureSize, renderTextureSize);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        // Remember current texture.
        lastTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        GL11.glClearColor(0, 0, 0, 0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glCullFace(GL11.GL_FRONT);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    public void end() {
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        // Revert to last viewport
        GL11.glViewport(lastViewport.get(0), lastViewport.get(1), lastViewport.get(2), lastViewport.get(3));

        // Revert to default framebuffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFramebuffer);

        // Revert to last texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lastTexture);
    }

    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
    }

    // This is only a separate function because the texture gets messed with after you're done rendering to read the FBO
    public void restoreTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lastTexture);
    }

    public void saveToFile(File file) {
        // Bind framebuffer texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        IntBuffer texture = BufferUtils.createIntBuffer(width * height);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, texture);

        int[] texture_array = new int[width * height];
        texture.get(texture_array);

        BufferedImage image =
                new BufferedImage(renderTextureSize, renderTextureSize, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, renderTextureSize, renderTextureSize, texture_array, 0, width);

        file.mkdirs();
        try {
            ImageIO.write(image, "png", file);
        } catch(Exception e) {
            //Do nothing
        }
    }

    private void createFramebuffer() {
        framebufferID = GL30.glGenFramebuffers();
        textureID = GL11.glGenTextures();
        int currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);

        // Set our texture up, empty.
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, renderTextureSize, renderTextureSize, 0, GL12.GL_BGRA,
                          GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);

        // Restore old texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);

        // Create depth buffer
        depthbufferID = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthbufferID);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, renderTextureSize, renderTextureSize);

        // Bind depth buffer to the framebuffer
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthbufferID);

        // Bind our texture to the framebuffer
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, textureID, 0);

        // Revert to default framebuffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    private void deleteFramebuffer() {
        GL30.glDeleteFramebuffers(framebufferID);
        GL11.glDeleteTextures(textureID);
        GL30.glDeleteRenderbuffers(depthbufferID);
    }
}
