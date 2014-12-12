package itemrender.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class KeybindToggleRender implements KeybindHandler {
    @Override
    public void onKeypress() {
        RenderTickHandler.renderPreview = !RenderTickHandler.renderPreview;
    }
}
