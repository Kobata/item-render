package itemrender.client;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class KeybindToggleRender implements KeybindHandler {
    @Override
    public void onKeypress() {
        RenderTickHandler.renderPreview = !RenderTickHandler.renderPreview;
    }
}
