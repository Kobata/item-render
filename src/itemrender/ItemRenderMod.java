package itemrender;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import itemrender.client.KeybindRenderInventoryBlock;
import itemrender.client.KeybindToggleRender;
import itemrender.client.RenderTickHandler;
import org.lwjgl.opengl.GLContext;

@Mod(modid = "@MODID@")
public class ItemRenderMod {
    @Mod.Instance("@MODID@")
    public static ItemRenderMod instance;

    public static boolean gl32_enabled = false;

    public static final int DEFAULT_TEXTURE_SIZE = 128;
    public static final int GRID_TEXTURE_SIZE = 32;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        gl32_enabled = GLContext.getCapabilities().OpenGL32;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        if(gl32_enabled) {
            TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
            KeybindRenderInventoryBlock defaultRender = new KeybindRenderInventoryBlock(DEFAULT_TEXTURE_SIZE, "");
            RenderTickHandler.keybindToRender = defaultRender;
            KeyBindingRegistry.registerKeyBinding(defaultRender);
            KeyBindingRegistry.registerKeyBinding(new KeybindRenderInventoryBlock(GRID_TEXTURE_SIZE, "_grid"));
            KeyBindingRegistry.registerKeyBinding(new KeybindToggleRender());
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    }
}

