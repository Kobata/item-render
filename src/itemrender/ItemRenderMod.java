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
import net.minecraftforge.common.Configuration;
import org.lwjgl.opengl.GLContext;

import java.util.logging.Logger;

@Mod(modid = "@MODID@")
public class ItemRenderMod {
    @Mod.Instance("@MODID@")
    public static ItemRenderMod instance;

    public static boolean gl32_enabled = false;

    public static final int DEFAULT_MAIN_TEXTURE_SIZE = 128;
    public static final int DEFAULT_GRID_TEXTURE_SIZE = 32;

    private int mainTextureSize;
    private int gridTextureSize;

    public Logger log;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        log = e.getModLog();
        gl32_enabled = GLContext.getCapabilities().OpenGL32;

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        mainTextureSize = config.get(Configuration.CATEGORY_GENERAL, "mainTextureSize", DEFAULT_MAIN_TEXTURE_SIZE).getInt();
        gridTextureSize = config.get(Configuration.CATEGORY_GENERAL, "gridTextureSize", DEFAULT_GRID_TEXTURE_SIZE).getInt();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        if(gl32_enabled) {
            TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
            KeybindRenderInventoryBlock defaultRender = new KeybindRenderInventoryBlock(mainTextureSize, "");
            RenderTickHandler.keybindToRender = defaultRender;
            KeyBindingRegistry.registerKeyBinding(defaultRender);
            KeyBindingRegistry.registerKeyBinding(new KeybindRenderInventoryBlock(gridTextureSize, "_grid"));
            KeyBindingRegistry.registerKeyBinding(new KeybindToggleRender());
        } else {
            log.severe("OpenGL 3.2 not detected, mod will not work!");
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    }
}

