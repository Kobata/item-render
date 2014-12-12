package itemrender.client.rendering;

import itemrender.ItemRenderMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;

public final class FBOHelper {
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
        checkGlErrors("FBO Begin Init");

        // Remember current framebuffer.
        lastFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        // Render to our texture
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);

        // Remember viewport info.
        lastViewport = GLAllocation.createDirectIntBuffer(16);
        GL11.glGetInteger(GL11.GL_VIEWPORT, lastViewport);
        GL11.glViewport(0, 0, renderTextureSize, renderTextureSize);

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        // Remember current texture.
        lastTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        GlStateManager.clearColor(0, 0, 0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();
        //GlStateManager.depthFunc(GL11.GL_ALWAYS);
        GlStateManager.enableLighting();
        GlStateManager.enableRescaleNormal();

        checkGlErrors("FBO Begin Final");
    }

    public void end() {
        checkGlErrors("FBO End Init");

        //GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();

        // Revert to last viewport
        GL11.glViewport(lastViewport.get(0), lastViewport.get(1), lastViewport.get(2), lastViewport.get(3));

        // Revert to default framebuffer
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastFramebuffer);

        // Revert to last texture
        GlStateManager.func_179144_i(lastTexture);

        checkGlErrors("FBO End Final");
    }

    public void bind() {
        GlStateManager.func_179144_i(textureID);
    }

    // This is only a separate function because the texture gets messed with after you're done rendering to read the FBO
    public void restoreTexture() {
        GlStateManager.func_179144_i(lastTexture);
    }

    public void saveToFile(File file) {
        // Bind framebuffer texture
        GlStateManager.func_179144_i(textureID);

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

        AffineTransform flip = AffineTransform.getScaleInstance(1, -1);
        flip.translate(0, -renderTextureSize);
        BufferedImage flipped = new AffineTransformOp(flip, null).filter(image, null);

        file.mkdirs();
        try {
            ImageIO.write(flipped, "png", file);
        } catch(Exception e) {
            //Do nothing
        }
    }

    private void createFramebuffer() {
        framebufferID = GL30.glGenFramebuffers();
        textureID = GL11.glGenTextures();
        int currentFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
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
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, currentFramebuffer);
    }

    private void deleteFramebuffer() {
        GL30.glDeleteFramebuffers(framebufferID);
        GL11.glDeleteTextures(textureID);
        GL30.glDeleteRenderbuffers(depthbufferID);
    }

    public static void checkGlErrors(String message)
    {
        int error = GL11.glGetError();

        if (error != 0)
        {
            String error_name = GLU.gluErrorString(error);
            ItemRenderMod.instance.log.error("########## GL ERROR ##########");
            ItemRenderMod.instance.log.error("@ " + message);
            ItemRenderMod.instance.log.error(error + ": " + error_name);
        }
    }

}
