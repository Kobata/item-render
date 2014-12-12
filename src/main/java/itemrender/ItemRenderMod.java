package itemrender;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import itemrender.client.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GLContext;

@Mod(modid = "itemrender", acceptableRemoteVersions="*", acceptableSaveVersions="*")
public final class ItemRenderMod {
    public static final KeyBinding KEYBINDING_RENDER_ITEM = new KeyBinding("itemrender.key.render_item", Keyboard.KEY_P, "itemrender.key.category");
    public static final KeyBinding KEYBINDING_RENDER_ENTITY = new KeyBinding("itemrender.key.render_entity", Keyboard.KEY_L, "itemrender.key.category");
    public static final KeyBinding KEYBINDING_TOGGLE_PREVIEW = new KeyBinding("itemrender.key.toggle_preview", Keyboard.KEY_O, "itemrender.key.category");
    public static final int DEFAULT_MAIN_TEXTURE_SIZE = 128;
    public static final int DEFAULT_GRID_TEXTURE_SIZE = 32;
    @Mod.Instance("itemrender")
    public static ItemRenderMod instance;
    public static boolean gl32_enabled = false;
    private final KeybindHelper keybindHelper = new KeybindHelper();
    public Logger log;
    private int mainTextureSize;
    private int gridTextureSize;

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
            FMLCommonHandler.instance().bus().register(new RenderTickHandler());
            FMLCommonHandler.instance().bus().register(keybindHelper);

            ClientRegistry.registerKeyBinding(KEYBINDING_RENDER_ITEM);
            ClientRegistry.registerKeyBinding(KEYBINDING_RENDER_ENTITY);
            ClientRegistry.registerKeyBinding(KEYBINDING_TOGGLE_PREVIEW);

            KeybindRenderInventoryBlock defaultRender = new KeybindRenderInventoryBlock(mainTextureSize, "");
            RenderTickHandler.keybindToRender = defaultRender;

            keybindHelper.register(KEYBINDING_RENDER_ITEM, defaultRender);
            keybindHelper.register(KEYBINDING_RENDER_ITEM, new KeybindRenderInventoryBlock(gridTextureSize, "_grid"));
            keybindHelper.register(KEYBINDING_RENDER_ENTITY, new KeybindRenderEntity(mainTextureSize, ""));
            keybindHelper.register(KEYBINDING_RENDER_ENTITY, new KeybindRenderEntity(gridTextureSize, "_grid"));
            keybindHelper.register(KEYBINDING_TOGGLE_PREVIEW, new KeybindToggleRender());
        } else {
            log.fatal("OpenGL 3.2 not detected, mod will not work!");
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    }
}

