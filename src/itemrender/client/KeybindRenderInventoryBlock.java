package itemrender.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.util.EnumSet;

@SideOnly(Side.CLIENT)
public class KeybindRenderInventoryBlock extends KeyBindingRegistry.KeyHandler {
    private static KeyBinding[] keyBindings =
            new KeyBinding[]{new KeyBinding("Render Inventory Block", Keyboard.KEY_P)};
    private static boolean[] repeatings = new boolean[]{false};

    private int renderTextureSize = 128;

    public int framebufferID = -1;
    public int depthbufferID = -1;
    public int textureID = -1;

    private String filenameSuffix = "";

    private RenderItem itemRenderer = new RenderItem();

    public KeybindRenderInventoryBlock(int textureSize, String filename_suffix) {
        super(keyBindings, repeatings);

        renderTextureSize = textureSize;
        filenameSuffix = filename_suffix;

        createFramebuffer();
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

    public void resizeFramebuffer(int newSize) {
        deleteFramebuffer();
        renderTextureSize = newSize;
        createFramebuffer();
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        if(!tickEnd) return;
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        if(minecraft.thePlayer != null) {
            ItemStack current = minecraft.thePlayer.getCurrentEquippedItem();
            if(current != null && current.getItem() != null) {

                // Render to our texture
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);
                // Remember viewport info.
                IntBuffer viewportInfo = GLAllocation.createDirectIntBuffer(16);
                GL11.glGetInteger(GL11.GL_VIEWPORT, viewportInfo);
                GL11.glViewport(0, 0, renderTextureSize, renderTextureSize);

                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glOrtho(0, 16, 0, 16, -100.0, 100.0);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();

                // Remember current texture.
                int originalTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

                GL11.glClearColor(0, 0, 0, 0);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                GL11.glCullFace(GL11.GL_FRONT);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);

                RenderHelper.enableGUIStandardItemLighting();

                RenderBlocks renderBlocks = ReflectionHelper.getPrivateValue(Render.class, itemRenderer,
                                                                             "field_76988_d",
                                                                             "renderBlocks");
                if(!ForgeHooksClient
                        .renderInventoryItem(renderBlocks, minecraft.renderEngine, current, true, 0.0f,
                                             (float) 0, (float) 0)) {
                    itemRenderer.renderItemIntoGUI(null, minecraft.renderEngine, current, 0, 0);
                }

                RenderHelper.disableStandardItemLighting();

                GL11.glCullFace(GL11.GL_BACK);
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GL11.glDisable(GL11.GL_LIGHTING);

                GL11.glPopMatrix();
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPopMatrix();

                // Revert to default viewport
                GL11.glViewport(viewportInfo.get(0), viewportInfo.get(1), viewportInfo.get(2), viewportInfo.get(3));
                // Revert to default framebuffer
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

                // Bind framebuffer texture
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

                GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

                int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

                IntBuffer texture = BufferUtils.createIntBuffer(width * height);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, texture);
                //GL11.glReadPixels(0, 0, renderTextureSize, renderTextureSize, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, texture);
                int[] texture_array = new int[width * height];
                texture.get(texture_array);

                BufferedImage image =
                        new BufferedImage(renderTextureSize, renderTextureSize, BufferedImage.TYPE_INT_ARGB);
                image.setRGB(0, 0, renderTextureSize, renderTextureSize, texture_array, 0, width);

                File file = new File(minecraft.mcDataDir,
                        String.format("rendered/item_%d_%d%s.png", current.getItem().itemID, current.getItemDamage(), filenameSuffix));
                file.mkdirs();
                try {
                    ImageIO.write(image, "png", file);
                } catch(Exception e) {
                    //Do nothing
                }

                // Restore old texture
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, originalTexture);

            }
        }
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.RENDER);
    }

    @Override
    public String getLabel() {
        return "KeyHandler";
    }
}
