package itemrender.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import itemrender.client.rendering.FBOHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.io.File;

@SideOnly(Side.CLIENT)
public final class KeybindRenderInventoryBlock implements KeybindHandler {
    public FBOHelper fbo;

    private String filenameSuffix = "";

    private RenderItem itemRenderer = new RenderItem();

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

                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glOrtho(0, 16, 0, 16, -100.0, 100.0);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);

                RenderHelper.enableGUIStandardItemLighting();

                RenderBlocks renderBlocks = ReflectionHelper.getPrivateValue(Render.class, itemRenderer,
                        "field_147909_c",
                        "field_147909_c");
                if(!ForgeHooksClient
                        .renderInventoryItem(renderBlocks, minecraft.renderEngine, current, true, 0.0f,
                                (float) 0, (float) 0)) {
                    itemRenderer.renderItemIntoGUI(null, minecraft.renderEngine, current, 0, 0);
                }

                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glPopMatrix();

                RenderHelper.disableStandardItemLighting();

                fbo.end();

                fbo.saveToFile(new File(minecraft.mcDataDir,
                        String.format("rendered/item_%s_%d%s.png", GameData.getItemRegistry().getNameForObject(current.getItem()).replace(':', '_'), current.getItemDamage(),
                                filenameSuffix)));

                fbo.restoreTexture();
            }
        }
    }
}
