package itemrender.client;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.EnumSet;

@SideOnly(Side.CLIENT)
public class KeybindToggleRender extends KeyBindingRegistry.KeyHandler {
    private static KeyBinding[] keyBindings =
            new KeyBinding[]{new KeyBinding("Toggle Render Preview", Keyboard.KEY_O)};
    private static boolean[] repeatings = new boolean[]{false};

    public KeybindToggleRender() {
        super(keyBindings, repeatings);

    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        RenderTickHandler.renderPreview = !RenderTickHandler.renderPreview;
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
