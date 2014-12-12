package itemrender.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public final class RenderTickHandler {
    public static boolean renderPreview = false;
    public static KeybindRenderInventoryBlock keybindToRender;

    public RenderTickHandler() {
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent tick) {
        if(tick.phase == TickEvent.Phase.END) {
            if(keybindToRender != null && renderPreview) {
                int originalTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

                // Bind framebuffer texture
                keybindToRender.fbo.bind();
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glTexCoord2f(0, 0);
                GL11.glVertex2i(0, 0);
                GL11.glTexCoord2f(0, 1);
                GL11.glVertex2i(0, 128);
                GL11.glTexCoord2f(1, 1);
                GL11.glVertex2i(128, 128);
                GL11.glTexCoord2f(1, 0);
                GL11.glVertex2i(128, 0);
                GL11.glEnd();

                // Restore old texture
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, originalTexture);
            }
        }
    }
}