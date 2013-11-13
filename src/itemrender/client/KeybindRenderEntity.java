package itemrender.client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import itemrender.client.rendering.FBOHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumMovingObjectType;
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
public class KeybindRenderEntity extends KeyBindingRegistry.KeyHandler {
    private static KeyBinding[] keyBindings =
            new KeyBinding[]{new KeyBinding("Render Entity", Keyboard.KEY_L)};
    private static boolean[] repeatings = new boolean[]{false};

    public FBOHelper fbo;

    private String filenameSuffix = "";

    public KeybindRenderEntity(int textureSize, String filename_suffix) {
        super(keyBindings, repeatings);

        fbo = new FBOHelper(textureSize);
        filenameSuffix = filename_suffix;
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        if(!tickEnd) return;
        Minecraft minecraft = FMLClientHandler.instance().getClient();
        if(minecraft.pointedEntityLiving != null) {
            EntityLivingBase current = minecraft.pointedEntityLiving;
            fbo.begin();

            AxisAlignedBB aabb = current.boundingBox;
            double minX = aabb.minX - current.posX;
            double maxX = aabb.maxX - current.posX;
            double minY = aabb.minY - current.posY;
            double maxY = aabb.maxY - current.posY;
            double minZ = aabb.minZ - current.posZ;
            double maxZ = aabb.maxZ - current.posZ;

            double minBound = Math.min(minX, Math.min(minY, minZ));
            double maxBound = Math.max(maxX, Math.max(maxY, maxZ));

            double boundLimit = Math.max(Math.abs(minBound), Math.abs(maxBound));

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glOrtho(-boundLimit*0.75, boundLimit*0.75, -boundLimit*1.25, boundLimit*0.25, -100.0, 100.0);

            GL11.glMatrixMode(GL11.GL_MODELVIEW);

            renderEntity(current);
            //GuiInventory.func_110423_a(0, 0, 1, 1, 1, current);

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPopMatrix();

            fbo.end();

            fbo.saveToFile(new File(minecraft.mcDataDir,
                                    String.format("rendered/entity_%s%s.png", EntityList.getEntityString(current), filenameSuffix)));

            fbo.restoreTexture();
        }
    }

    private void renderEntity(EntityLivingBase entity) {

        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glScalef((float)(-1), (float)1, (float)1);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f2 = entity.renderYawOffset;
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        float f5 = entity.prevRotationYawHead;
        float f6 = entity.rotationYawHead;
        GL11.glRotatef(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);

        GL11.glRotatef((float)Math.toDegrees(Math.asin(Math.tan(Math.toRadians(30)))), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(-45, 0.0F, 1.0F, 0.0F);

        entity.renderYawOffset = (float)Math.atan((double)(1 / 40.0F)) * 20.0F;
        entity.rotationYaw = (float)Math.atan((double)(1 / 40.0F)) * 40.0F;
        entity.rotationPitch = -((float)Math.atan((double)(1 / 40.0F))) * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;
        RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
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
