package net.minecraftforge.client;

import static net.minecraftforge.common.ForgeVersion.Status.BETA;
import static net.minecraftforge.common.ForgeVersion.Status.BETA_OUTDATED;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IRegistry;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
//import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;
//import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.*;

public class ForgeHooksClient
{
    //private static final ResourceLocation ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    static TextureManager engine()
    {
        return FMLClientHandler.instance().getClient().field_71446_o;
    }

    public static String getArmorTexture(Entity entity, ItemStack armor, String _default, int slot, String type)
    {
        String result = armor.func_77973_b().getArmorTexture(armor, entity, slot, type);
        return result != null ? result : _default;
    }

    /*
     * Removed, Modders let me know if this is needed anymore.
    public static boolean renderEntityItem(EntityItem entity, ItemStack item, float bobing, float rotation, Random random, TextureManager engine, RenderBlocks renderBlocks, int count)
    {
        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(item, ENTITY);
        if (customRenderer == null)
        {
            return false;
        }

        if (customRenderer.shouldUseRenderHelper(ENTITY, item, ENTITY_ROTATION))
        {
            GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
        }
        if (!customRenderer.shouldUseRenderHelper(ENTITY, item, ENTITY_BOBBING))
        {
            GL11.glTranslatef(0.0F, -bobing, 0.0F);
        }
        boolean is3D = customRenderer.shouldUseRenderHelper(ENTITY, item, BLOCK_3D);

        engine.bindTexture(item.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
        Block block = item.getItem() instanceof ItemBlock ? Block.getBlockFromItem(item.getItem()) : null;
        if (is3D || (block != null && RenderBlocks.renderItemIn3d(block.getRenderType())))
        {
            int renderType = (block != null ? block.getRenderType() : 1);
            float scale = (renderType == 1 || renderType == 19 || renderType == 12 || renderType == 2 ? 0.5F : 0.25F);
            boolean blend = block != null && block.getRenderBlockPass() > 0;

            if (RenderItem.renderInFrame)
            {
                GL11.glScalef(1.25F, 1.25F, 1.25F);
                GL11.glTranslatef(0.0F, 0.05F, 0.0F);
                GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
            }

            if (blend)
            {
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            }

            GL11.glScalef(scale, scale, scale);

            for(int j = 0; j < count; j++)
            {
                GL11.glPushMatrix();
                if (j > 0)
                {
                    GL11.glTranslatef(
                        ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / scale,
                        ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / scale,
                        ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / scale);
                }
                customRenderer.renderItem(ENTITY, item, renderBlocks, entity);
                GL11.glPopMatrix();
            }

            if (blend)
            {
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
        else
        {
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            customRenderer.renderItem(ENTITY, item, renderBlocks, entity);
        }

        return true;
    }
    */

    /*
    public static boolean renderInventoryItem(RenderBlocks renderBlocks, TextureManager engine, ItemStack item, boolean inColor, float zLevel, float x, float y)
    {
        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(item, INVENTORY);
        if (customRenderer == null)
        {
            return false;
        }

        engine.bindTexture(item.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
        if (customRenderer.shouldUseRenderHelper(INVENTORY, item, INVENTORY_BLOCK))
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(x - 2, y + 3, -3.0F + zLevel);
            GL11.glScalef(10F, 10F, 10F);
            GL11.glTranslatef(1.0F, 0.5F, 1.0F);
            GL11.glScalef(1.0F, 1.0F, -1F);
            GL11.glRotatef(210F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);

            if(inColor)
            {
                int color = item.getItem().getColorFromItemStack(item, 0);
                float r = (float)(color >> 16 & 0xff) / 255F;
                float g = (float)(color >> 8 & 0xff) / 255F;
                float b = (float)(color & 0xff) / 255F;
                GL11.glColor4f(r, g, b, 1.0F);
            }

            GL11.glRotatef(-90F, 0.0F, 1.0F, 0.0F);
            renderBlocks.useInventoryTint = inColor;
            customRenderer.renderItem(INVENTORY, item, renderBlocks);
            renderBlocks.useInventoryTint = true;
            GL11.glPopMatrix();
        }
        else
        {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, -3.0F + zLevel);

            if (inColor)
            {
                int color = item.getItem().getColorFromItemStack(item, 0);
                float r = (float)(color >> 16 & 255) / 255.0F;
                float g = (float)(color >> 8 & 255) / 255.0F;
                float b = (float)(color & 255) / 255.0F;
                GL11.glColor4f(r, g, b, 1.0F);
            }

            customRenderer.renderItem(INVENTORY, item, renderBlocks);
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_LIGHTING);
        }

        return true;
    }

    public static void renderEffectOverlay(TextureManager manager, RenderItem render)
    {
    }

    public static void renderEquippedItem(ItemRenderType type, IItemRenderer customRenderer, RenderBlocks renderBlocks, EntityLivingBase entity, ItemStack item)
    {
        if (customRenderer.shouldUseRenderHelper(type, item, EQUIPPED_BLOCK))
        {
            GL11.glPushMatrix();
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            customRenderer.renderItem(type, item, renderBlocks, entity);
            GL11.glPopMatrix();
        }
        else
        {
            GL11.glPushMatrix();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glTranslatef(0.0F, -0.3F, 0.0F);
            GL11.glScalef(1.5F, 1.5F, 1.5F);
            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
            customRenderer.renderItem(type, item, renderBlocks, entity);
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
        }
    }*/

    //Optifine Helper Functions u.u, these are here specifically for Optifine
    //Note: When using Optfine, these methods are invoked using reflection, which
    //incurs a major performance penalty.
    public static void orientBedCamera(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity)
    {
        Block block = state.func_177230_c();

        if (block != null && block.isBed(world, pos, entity))
        {
            GL11.glRotatef((float)(block.getBedDirection(world, pos).func_176736_b() * 90), 0.0F, 1.0F, 0.0F);
        }
    }

    public static boolean onDrawBlockHighlight(RenderGlobal context, EntityPlayer player, MovingObjectPosition target, int subID, ItemStack currentItem, float partialTicks)
    {
        return MinecraftForge.EVENT_BUS.post(new DrawBlockHighlightEvent(context, player, target, subID, currentItem, partialTicks));
    }

    public static void dispatchRenderLast(RenderGlobal context, float partialTicks)
    {
        MinecraftForge.EVENT_BUS.post(new RenderWorldLastEvent(context, partialTicks));
    }

    public static boolean renderFirstPersonHand(RenderGlobal context, float partialTicks, int renderPass)
    {
        return MinecraftForge.EVENT_BUS.post(new RenderHandEvent(context, partialTicks, renderPass));
    }

    public static void onTextureStitchedPre(TextureMap map)
    {
        MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Pre(map));
        ModelLoader.White.instance.register(map);
    }

    public static void onTextureStitchedPost(TextureMap map)
    {
        MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Post(map));

        FluidRegistry.WATER.setIcons(map.func_110572_b("minecraft:blocks/water_still"), map.func_110572_b("minecraft:blocks/water_flow"));
        FluidRegistry.LAVA.setIcons(map.func_110572_b("minecraft:blocks/lava_still"), map.func_110572_b("minecraft:blocks/lava_flow"));
    }

    static int renderPass = -1;
    public static void setRenderPass(int pass)
    {
        renderPass = pass;
    }

    static final ThreadLocal<EnumWorldBlockLayer> renderLayer = new ThreadLocal<EnumWorldBlockLayer>()
    {
        protected EnumWorldBlockLayer initialValue()
        {
            return EnumWorldBlockLayer.SOLID;
        }
    };

    public static void setRenderLayer(EnumWorldBlockLayer layer)
    {
        renderLayer.set(layer);
    }

    public static ModelBase getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int slotID, ModelBase _default)
    {
        ModelBase modelbase = itemStack.func_77973_b().getArmorModel(entityLiving, itemStack, slotID);
        return modelbase == null ? _default : modelbase;
    }

    //This properly moves the domain, if provided, to the front of the string before concatenating
    public static String fixDomain(String base, String complex)
    {
        int idx = complex.indexOf(':');
        if (idx == -1)
        {
            return base + complex;
        }

        String name = complex.substring(idx + 1, complex.length());
        if (idx > 1)
        {
            String domain = complex.substring(0, idx);
            return domain + ':' + base + name;
        }
        else
        {
            return base + name;
        }
    }

    public static boolean postMouseEvent()
    {
        return MinecraftForge.EVENT_BUS.post(new MouseEvent());
    }

    public static float getOffsetFOV(EntityPlayer entity, float fov)
    {
        FOVUpdateEvent fovUpdateEvent = new FOVUpdateEvent(entity, fov);
        MinecraftForge.EVENT_BUS.post(fovUpdateEvent);
        return fovUpdateEvent.newfov;
    }

    private static int skyX, skyZ;

    private static boolean skyInit;
    private static int skyRGBMultiplier;

    public static int getSkyBlendColour(World world, BlockPos center)
    {
        if (center.func_177958_n() == skyX && center.func_177952_p() == skyZ && skyInit)
        {
            return skyRGBMultiplier;
        }
        skyInit = true;

        GameSettings settings = Minecraft.func_71410_x().field_71474_y;
        int[] ranges = ForgeModContainer.blendRanges;
        int distance = 0;
        if (settings.field_74347_j && settings.field_151451_c >= 0 && settings.field_151451_c < ranges.length)
        {
            distance = ranges[settings.field_151451_c];
        }

        int r = 0;
        int g = 0;
        int b = 0;

        int divider = 0;
        for (int x = -distance; x <= distance; ++x)
        {
            for (int z = -distance; z <= distance; ++z)
            {
                BlockPos pos = center.func_177982_a(x, 0, z);
                BiomeGenBase biome = world.func_180494_b(pos);
                int colour = biome.func_76731_a(biome.func_180626_a(pos));
                r += (colour & 0xFF0000) >> 16;
                g += (colour & 0x00FF00) >> 8;
                b += colour & 0x0000FF;
                divider++;
            }
        }

        int multiplier = (r / divider & 255) << 16 | (g / divider & 255) << 8 | b / divider & 255;

        skyX = center.func_177958_n();
        skyZ = center.func_177956_o();
        skyRGBMultiplier = multiplier;
        return skyRGBMultiplier;
    }
    /**
     * Initialization of Forge Renderers.
     */
    static
    {
        //FluidRegistry.renderIdFluid = RenderingRegistry.getNextAvailableRenderId();
        //RenderingRegistry.registerBlockHandler(RenderBlockFluid.instance);
    }

    public static void renderMainMenu(GuiMainMenu gui, FontRenderer font, int width, int height)
    {
        Status status = ForgeVersion.getStatus();
        if (status == BETA || status == BETA_OUTDATED)
        {
            // render a warning at the top of the screen,
            String line = I18n.func_135052_a("forge.update.beta.1", EnumChatFormatting.RED, EnumChatFormatting.RESET);
            gui.func_73731_b(font, line, (width - font.func_78256_a(line)) / 2, 4 + (0 * (font.field_78288_b + 1)), -1);
            line = I18n.func_135052_a("forge.update.beta.2");
            gui.func_73731_b(font, line, (width - font.func_78256_a(line)) / 2, 4 + (1 * (font.field_78288_b + 1)), -1);
        }

        String line = null;
        switch(status)
        {
            //case FAILED:        line = " Version check failed"; break;
            //case UP_TO_DATE:    line = "Forge up to date"}; break;
            //case AHEAD:         line = "Using non-recommended Forge build, issues may arise."}; break;
            case OUTDATED:
            case BETA_OUTDATED: line = I18n.func_135052_a("forge.update.newversion", ForgeVersion.getTarget()); break;
            default: break;
        }

        if (line != null)
        {
            // if we have a line, render it in the bottom right, above Mojang's copyright line
            gui.func_73731_b(font, line, width - font.func_78256_a(line) - 2, height - (2 * (font.field_78288_b + 1)), -1);
        }
    }

    public static ISound playSound(SoundManager manager, ISound sound)
    {
        SoundEventAccessorComposite accessor = manager.field_148622_c.func_147680_a(sound.func_147650_b());
        PlaySoundEvent e = new PlaySoundEvent(manager, sound, (accessor == null ? null : accessor.func_148728_d()));
        MinecraftForge.EVENT_BUS.post(e);
        return e.result;
    }

    //static RenderBlocks worldRendererRB;
    static int worldRenderPass;

    public static int getWorldRenderPass()
    {
        return worldRenderPass;
    }

    public static void drawScreen(GuiScreen screen, int mouseX, int mouseY, float partialTicks)
    {
        if (!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(screen, mouseX, mouseY, partialTicks)))
            screen.func_73863_a(mouseX, mouseY, partialTicks);
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(screen, mouseX, mouseY, partialTicks));
    }

    public static float getFogDensity(EntityRenderer renderer, Entity entity, Block block, float partial, float density)
    {
        EntityViewRenderEvent.FogDensity event = new EntityViewRenderEvent.FogDensity(renderer, entity, block, partial, density);
        if (MinecraftForge.EVENT_BUS.post(event)) return event.density;
        return -1;
    }

    public static void onFogRender(EntityRenderer renderer, Entity entity, Block block, float partial, int mode, float distance)
    {
        MinecraftForge.EVENT_BUS.post(new EntityViewRenderEvent.RenderFogEvent(renderer, entity, block, partial, mode, distance));
    }

    /*
    public static void setWorldRendererRB(RenderBlocks renderBlocks)
    {
        worldRendererRB = renderBlocks;
    }

    public static void onPreRenderWorld(WorldRenderer worldRenderer, int pass)
    {
        if(worldRendererRB != null)
        {
            worldRenderPass = pass;
            MinecraftForge.EVENT_BUS.post(new RenderWorldEvent.Pre(worldRenderer, (ChunkCache)worldRendererRB.blockAccess, worldRendererRB, pass));
        }
    }

    public static void onPostRenderWorld(WorldRenderer worldRenderer, int pass)
    {
        if(worldRendererRB != null)
        {
            MinecraftForge.EVENT_BUS.post(new RenderWorldEvent.Post(worldRenderer, (ChunkCache)worldRendererRB.blockAccess, worldRendererRB, pass));
            worldRenderPass = -1;
        }
    }
    */

    public static void onModelBake(ModelManager modelManager, IRegistry modelRegistry, ModelBakery modelBakery)
    {
        ModelLoader loader = (ModelLoader)modelBakery;
        MinecraftForge.EVENT_BUS.post(new ModelBakeEvent(modelManager, modelRegistry, loader));
        loader.onPostBakeEvent(modelRegistry);
    }

    public static Matrix4f getMatrix(ItemTransformVec3f transform)
    {
        javax.vecmath.Matrix4f m = new javax.vecmath.Matrix4f(), t = new javax.vecmath.Matrix4f();
        m.setIdentity();
        m.setTranslation(transform.field_178365_c);
        t.setIdentity();
        t.rotY(transform.field_178364_b.y);
        m.mul(t);
        t.setIdentity();
        t.rotX(transform.field_178364_b.x);
        m.mul(t);
        t.setIdentity();
        t.rotZ(transform.field_178364_b.z);
        m.mul(t);
        t.setIdentity();
        t.m00 = transform.field_178363_d.x;
        t.m11 = transform.field_178363_d.y;
        t.m22 = transform.field_178363_d.z;
        m.mul(t);
        return m;
    }

    public static IBakedModel handleCameraTransforms(IBakedModel model, ItemCameraTransforms.TransformType cameraTransformType)
    {
        if(model instanceof IPerspectiveAwareModel)
        {
            Pair<IBakedModel, Matrix4f> pair = ((IPerspectiveAwareModel)model).handlePerspective(cameraTransformType);

            if(pair.getRight() != null) multiplyCurrentGlMatrix(pair.getRight());
            return pair.getLeft();
        }
        switch(cameraTransformType)
        {
            case FIRST_PERSON:
                RenderItem.applyVanillaTransform(model.func_177552_f().field_178356_c);
                break;
            case GUI:
                RenderItem.applyVanillaTransform(model.func_177552_f().field_178354_e);
                break;
            case HEAD:
                RenderItem.applyVanillaTransform(model.func_177552_f().field_178353_d);
                break;
            case THIRD_PERSON:
                RenderItem.applyVanillaTransform(model.func_177552_f().field_178355_b);
                break;
            default:
                break;
        }
        return model;
    }

    private static final FloatBuffer matrixBuf = BufferUtils.createFloatBuffer(16);

    public static void multiplyCurrentGlMatrix(Matrix4f matrix)
    {
        matrixBuf.clear();
        float[] t = new float[4];
        for(int i = 0; i < 4; i++)
        {
            matrix.getColumn(i, t);
            matrixBuf.put(t);
        }
        matrixBuf.flip();
        GL11.glMultMatrix(matrixBuf);
    }

    // moved and expanded from WorldVertexBufferUploader.draw

    public static void preDraw(EnumUsage attrType, VertexFormatElement attr, int stride, ByteBuffer buffer)
    {
        buffer.position(attr.func_177373_a());
        switch(attrType)
        {
            case POSITION:
                glVertexPointer(attr.func_177370_d(), attr.func_177367_b().func_177397_c(), stride, buffer);
                glEnableClientState(GL_VERTEX_ARRAY);
                break;
            case NORMAL:
                if(attr.func_177370_d() != 3)
                {
                    throw new IllegalArgumentException("Normal attribute should have the size 3: " + attr);
                }
                glNormalPointer(attr.func_177367_b().func_177397_c(), stride, buffer);
                glEnableClientState(GL_NORMAL_ARRAY);
                break;
            case COLOR:
                glColorPointer(attr.func_177370_d(), attr.func_177367_b().func_177397_c(), stride, buffer);
                glEnableClientState(GL_COLOR_ARRAY);
                break;
            case UV:
                OpenGlHelper.func_77472_b(OpenGlHelper.field_77478_a + attr.func_177369_e());
                glTexCoordPointer(attr.func_177370_d(), attr.func_177367_b().func_177397_c(), stride, buffer);
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                OpenGlHelper.func_77472_b(OpenGlHelper.field_77478_a);
                break;
            case PADDING:
                break;
            case GENERIC:
                glEnableVertexAttribArray(attr.func_177369_e());
                glVertexAttribPointer(attr.func_177369_e(), attr.func_177370_d(), attr.func_177367_b().func_177397_c(), false, stride, buffer);
            default:
                FMLLog.severe("Unimplemented vanilla attribute upload: %s", attrType.func_177384_a());
        }
    }

    public static void postDraw(EnumUsage attrType, VertexFormatElement attr, int stride, ByteBuffer buffer)
    {
        switch(attrType)
        {
            case POSITION:
                glDisableClientState(GL_VERTEX_ARRAY);
                break;
            case NORMAL:
                glDisableClientState(GL_NORMAL_ARRAY);
                break;
            case COLOR:
                glDisableClientState(GL_COLOR_ARRAY);
                // is this really needed?
                GlStateManager.func_179117_G();
                break;
            case UV:
                OpenGlHelper.func_77472_b(OpenGlHelper.field_77478_a + attr.func_177369_e());
                glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                OpenGlHelper.func_77472_b(OpenGlHelper.field_77478_a);
                break;
            case PADDING:
                break;
            case GENERIC:
                glDisableVertexAttribArray(attr.func_177369_e());
            default:
                FMLLog.severe("Unimplemented vanilla attribute upload: %s", attrType.func_177384_a());
        }
    }

    public static void transform(Vector3d vec, Matrix4f m)
    {
        Vector4f tmp = new Vector4f((float)vec.x, (float)vec.y, (float)vec.z, 1f);
        m.transform(tmp);
        if(Math.abs(tmp.w - 1f) > 1e-5) tmp.scale(1f / tmp.w);
        vec.set(tmp.x, tmp.y, tmp.z);
    }

    public static Matrix4f getMatrix(ModelRotation modelRotation)
    {
        Matrix4f ret = new Matrix4f(modelRotation.func_177525_a()), tmp = new Matrix4f();
        tmp.setIdentity();
        tmp.m03 = tmp.m13 = tmp.m23 = .5f;
        ret.mul(tmp, ret);
        tmp.invert();
        //tmp.m03 = tmp.m13 = tmp.m23 = -.5f;
        ret.mul(tmp);
        return ret;
    }

    public static void putQuadColor(WorldRenderer renderer, BakedQuad quad, int color)
    {
        float cr = color & 0xFF;
        float cg = (color >>> 8) & 0xFF;
        float cb = (color >>> 16) & 0xFF;
        float ca = (color >>> 24) & 0xFF;
        for(int i = 0; i < 4; i++)
        {
            int vc = quad.func_178209_a()[3 + 7 * i];
            float vcr = vc & 0xFF;
            float vcg = (vc >>> 8) & 0xFF;
            float vcb = (vc >>> 16) & 0xFF;
            float vca = (vc >>> 24) & 0xFF;
            int ncr = Math.min(0xFF, (int)(cr * vcr / 0xFF));
            int ncg = Math.min(0xFF, (int)(cg * vcg / 0xFF));
            int ncb = Math.min(0xFF, (int)(cb * vcb / 0xFF));
            int nca = Math.min(0xFF, (int)(ca * vca / 0xFF));
            renderer.func_178972_a(renderer.func_78909_a(i + 1), ncr, ncg, ncb, nca);
        }
    }
}
