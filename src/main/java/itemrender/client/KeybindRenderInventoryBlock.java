package itemrender.client;

import itemrender.ItemRenderMod;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import itemrender.client.rendering.FBOHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public final class KeybindRenderInventoryBlock implements KeybindHandler {
    public FBOHelper fbo;

    private String filenameSuffix = "";

    private RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

    public KeybindRenderInventoryBlock(int textureSize, String filename_suffix) {
        fbo = new FBOHelper(textureSize);
        filenameSuffix = filename_suffix;
    }

    @Override
    public void onKeypress() {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        if(minecraft.thePlayer != null) {
            ItemStack current = minecraft.thePlayer.getCurrentEquippedItem();
            if(current != null && current.getItem() != null) {

                fbo.begin();

                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.ortho(0, 16, 16, 0, -100000.0, 100000.0);

                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                FloatBuffer matrix = GLAllocation.createDirectFloatBuffer(16);

                matrix.clear();

                matrix.put(new float[] {
                        1f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f,
                        0f, 0f, -1f, 0f,
                        0f, 0f, 0f, 1f});

                matrix.rewind();

                //GlStateManager.multMatrix(matrix);

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableColorMaterial();
                GlStateManager.enableLighting();

                itemRenderer.func_175042_a(current, 0, 0);

                GlStateManager.disableLighting();
                RenderHelper.disableStandardItemLighting();

                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.popMatrix();

                fbo.end();

                fbo.saveToFile(new File(minecraft.mcDataDir,
                        String.format("rendered/item_%s_%d%s.png", GameData.getItemRegistry().getNameForObject(current.getItem()).toString().replace(':', '_'), current.getItemDamage(),
                                filenameSuffix)));

                fbo.restoreTexture();
            }
        }
    }
}
