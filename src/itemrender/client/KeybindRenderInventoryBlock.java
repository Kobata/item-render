package itemrender.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import itemrender.client.rendering.FBOHelper;
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

    public FBOHelper fbo;

    private String filenameSuffix = "";

    private RenderItem itemRenderer = new RenderItem();

    public KeybindRenderInventoryBlock(int textureSize, String filename_suffix) {
        super(keyBindings, repeatings);

        fbo = new FBOHelper(textureSize);
        filenameSuffix = filename_suffix;
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        if(!tickEnd) return;
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        if(minecraft.thePlayer != null) {
            ItemStack current = minecraft.thePlayer.getCurrentEquippedItem();
            if(current != null && current.getItem() != null) {

                fbo.begin();

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

                fbo.end();

                fbo.saveToFile(new File(minecraft.mcDataDir,
                                        String.format("rendered/item_%d_%d%s.png", current.getItem().itemID, current.getItemDamage(), filenameSuffix)));

                fbo.restoreTexture();
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
