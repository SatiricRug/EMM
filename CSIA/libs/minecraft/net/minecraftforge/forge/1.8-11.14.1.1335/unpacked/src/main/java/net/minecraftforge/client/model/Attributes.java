package net.minecraftforge.client.model;

import java.nio.ByteBuffer;
import java.util.List;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;

import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
public class Attributes
{
    /*
     * Default format of the data in IBakedModel
     */
    public static final VertexFormat DEFAULT_BAKED_FORMAT;

    static
    {
        DEFAULT_BAKED_FORMAT = new VertexFormat();
        DEFAULT_BAKED_FORMAT.func_177349_a(new VertexFormatElement(0, EnumType.FLOAT, EnumUsage.POSITION, 3));
        DEFAULT_BAKED_FORMAT.func_177349_a(new VertexFormatElement(0, EnumType.UBYTE, EnumUsage.COLOR,    4));
        DEFAULT_BAKED_FORMAT.func_177349_a(new VertexFormatElement(0, EnumType.FLOAT, EnumUsage.UV,       2));
        DEFAULT_BAKED_FORMAT.func_177349_a(new VertexFormatElement(0, EnumType.BYTE,  EnumUsage.PADDING,  4));
    }

    /*
     * Can first format be used where second is expected
     */
    public static boolean moreSpecific(VertexFormat first, VertexFormat second)
    {
        int size = first.func_177338_f();
        if(size != second.func_177338_f()) return false;

        int padding = 0;
        int j = 0;
        for(VertexFormatElement firstAttr : (List<VertexFormatElement>)first.func_177343_g())
        {
            while(j < second.func_177345_h() && second.func_177348_c(j).func_177375_c() == EnumUsage.PADDING)
            {
                padding += second.func_177348_c(j++).func_177368_f();
            }
            if(j >= second.func_177345_h() && padding == 0)
            {
                // if no padding is left, but there are still elements in first (we're processing one) - it doesn't fit
                return false;
            }
            if(padding == 0)
            {
                // no padding - attributes have to match
                VertexFormatElement secondAttr = second.func_177348_c(j++);
                if(
                    firstAttr.func_177369_e() != secondAttr.func_177369_e() ||
                    firstAttr.func_177370_d() != secondAttr.func_177370_d() ||
                    firstAttr.func_177367_b() != secondAttr.func_177367_b() ||
                    firstAttr.func_177375_c() != secondAttr.func_177375_c())
                {
                    return false;
                }
            }
            else
            {
                // padding - attribute should fit in it
                padding -= firstAttr.func_177368_f();
                if(padding < 0) return false;
            }
        }

        if(padding != 0 || j != second.func_177345_h()) return false;
        return true;
    }

    public static void put(ByteBuffer buf, VertexFormatElement e, boolean denormalize, Number fill, Number... ns)
    {
        if(e.func_177370_d() > ns.length && fill == null) throw new IllegalArgumentException("not enough elements");
        Number n;
        for(int i = 0; i < e.func_177370_d(); i++)
        {
            if(i < ns.length) n = ns[i];
            else n = fill;
            switch(e.func_177367_b())
            {
            case BYTE:
                buf.put(denormalize ? (byte)(n.floatValue() * (Byte.MAX_VALUE - 1)) : n.byteValue());
                break;
            case UBYTE:
                buf.put(denormalize ? (byte)(n.floatValue() * ((1 << Byte.SIZE) - 1)) : n.byteValue());
                break;
            case SHORT:
                buf.putShort(denormalize ? (short)(n.floatValue() * (Short.MAX_VALUE - 1)) : n.shortValue());
                break;
            case USHORT:
                buf.putShort(denormalize ? (short)(n.floatValue() * ((1 << Short.SIZE) - 1)) : n.shortValue());
                break;
            case INT:
                buf.putInt(denormalize ? (int)(n.doubleValue() * (Integer.MAX_VALUE - 1)) : n.intValue());
                break;
            case UINT:
                buf.putInt(denormalize ? (int)(n.doubleValue() * ((1L << Integer.SIZE) - 1)) : n.intValue());
                break;
            case FLOAT:
                buf.putFloat(n.floatValue());
                break;
            }
        }
    }
}
