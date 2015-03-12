package net.minecraftforge.client.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

/*
 * Version of IBakedModel with less restriction on camera transformations and with explicit format of the baked array.
 */
public interface IFlexibleBakedModel extends IBakedModel
{
    // non-erased versions of the IBakedModel methods
    List<BakedQuad> func_177551_a(EnumFacing side);
    List<BakedQuad> func_177550_a();
    /*
     * Specifies the format which BakedQuads' getVertexData will have.
     */
    VertexFormat getFormat();

    /*
     * Default implementation of IFlexibleBakedModel that should be useful in most cases
     */
    public static class Wrapper implements IFlexibleBakedModel
    {
        private final IBakedModel parent;
        VertexFormat format;

        public Wrapper(IBakedModel parent, VertexFormat format)
        {
            this.parent = parent;
            this.format = format;
        }

        @SuppressWarnings("unchecked")
        public List<BakedQuad> func_177551_a(EnumFacing side)
        {
            return parent.func_177551_a(side);
        }

        @SuppressWarnings("unchecked")
        public List<BakedQuad> func_177550_a()
        {
            return parent.func_177550_a();
        }

        public boolean func_177555_b()
        {
            return parent.func_177555_b();
        }

        public boolean func_177556_c()
        {
            return parent.func_177556_c();
        }

        public boolean func_177553_d()
        {
            return parent.func_177553_d();
        }

        public TextureAtlasSprite func_177554_e()
        {
            return parent.func_177554_e();
        }

        @Deprecated
        public ItemCameraTransforms func_177552_f()
        {
            return parent.func_177552_f();
        }

        public VertexFormat getFormat()
        {
            return new VertexFormat(format);
        }
    }
}
