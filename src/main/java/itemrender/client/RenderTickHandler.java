package itemrender.client;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
                GL11.glTexCoord2f(0, 1);
                GL11.glVertex2i(0, 0);
                GL11.glTexCoord2f(0, 0);
                GL11.glVertex2i(0, 128);
                GL11.glTexCoord2f(1, 0);
                GL11.glVertex2i(128, 128);
                GL11.glTexCoord2f(1, 1);
                GL11.glVertex2i(128, 0);
                GL11.glEnd();

                // Restore old texture
                GlStateManager.func_179144_i(originalTexture);
            }
        }
    }
}