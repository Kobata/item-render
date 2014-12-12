package itemrender.client;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import itemrender.client.rendering.FBOHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.File;

@SideOnly(Side.CLIENT)
public final class KeybindRenderEntity implements KeybindHandler {
    public FBOHelper fbo;

    private String filenameSuffix = "";

    public KeybindRenderEntity(int textureSize, String filename_suffix) {
        fbo = new FBOHelper(textureSize);
        filenameSuffix = filename_suffix;
    }

    @Override
    public void onKeypress() {
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        if(minecraft.pointedEntity != null && (minecraft.pointedEntity instanceof EntityLivingBase)) {
            EntityLivingBase current = (EntityLivingBase) minecraft.pointedEntity;
            fbo.begin();

            AxisAlignedBB aabb = current.getEntityBoundingBox();
            double minX = aabb.minX - current.posX;
            double maxX = aabb.maxX - current.posX;
            double minY = aabb.minY - current.posY;
            double maxY = aabb.maxY - current.posY;
            double minZ = aabb.minZ - current.posZ;
            double maxZ = aabb.maxZ - current.posZ;

            double minBound = Math.min(minX, Math.min(minY, minZ));
            double maxBound = Math.max(maxX, Math.max(maxY, maxZ));

            double boundLimit = Math.max(Math.abs(minBound), Math.abs(maxBound));

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.ortho(-boundLimit * 0.75, boundLimit * 0.75, boundLimit * 0.25, -boundLimit * 1.25, -100.0, 100.0);

            GlStateManager.matrixMode(GL11.GL_MODELVIEW);

            renderEntity(current);
            //GuiInventory.drawEntityOnScreen(0, 0, 1, 1, 1, current);

            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();

            fbo.end();

            fbo.saveToFile(new File(minecraft.mcDataDir,
                    String.format("rendered/entity_%s%s.png", EntityList.getEntityString(current), filenameSuffix)));

            fbo.restoreTexture();
        }
    }

    private void renderEntity(EntityLivingBase entity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 50.0F);
        GlStateManager.scale(-1.0f, 1.0f, 1.0f);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.rotate((float) Math.toDegrees(Math.asin(Math.tan(Math.toRadians(30)))), 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-45, 0.0F, 1.0F, 0.0F);

        //GlStateManager.rotate(-((float) Math.atan((double) (1 / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        entity.renderYawOffset = (float)Math.atan((double)(1 / 40.0F)) * 20.0F;
        entity.rotationYaw = (float)Math.atan((double)(1 / 40.0F)) * 40.0F;
        entity.rotationPitch = -((float)Math.atan((double)(1 / 40.0F))) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.func_178631_a(180.0F);
        rendermanager.func_178633_a(false);
        rendermanager.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.func_178633_a(true);
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.func_179090_x();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
