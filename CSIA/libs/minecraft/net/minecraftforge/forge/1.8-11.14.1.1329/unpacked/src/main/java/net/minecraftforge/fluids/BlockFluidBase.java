package net.minecraftforge.fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This is a base implementation for Fluid blocks.
 *
 * It is highly recommended that you extend this class or one of the Forge-provided child classes.
 *
 * @author King Lemming, OvermindDL1
 *
 */
public abstract class BlockFluidBase extends Block implements IFluidBlock
{
    protected final static Map<Block, Boolean> defaultDisplacements = Maps.newHashMap();

    static
    {
        defaultDisplacements.put(Blocks.field_180413_ao,                       false);
        defaultDisplacements.put(Blocks.field_180414_ap,                    false);
        defaultDisplacements.put(Blocks.field_180412_aq,                     false);
        defaultDisplacements.put(Blocks.field_180411_ar,                    false);
        defaultDisplacements.put(Blocks.field_180410_as,                    false);
        defaultDisplacements.put(Blocks.field_180409_at,                  false);
        defaultDisplacements.put(Blocks.field_150415_aT,                       false);
        defaultDisplacements.put(Blocks.field_180400_cw,                  false);
        defaultDisplacements.put(Blocks.field_180407_aO,                      false);
        defaultDisplacements.put(Blocks.field_180408_aP,                   false);
        defaultDisplacements.put(Blocks.field_180404_aQ,                    false);
        defaultDisplacements.put(Blocks.field_180403_aR,                   false);
        defaultDisplacements.put(Blocks.field_180406_aS,                 false);
        defaultDisplacements.put(Blocks.field_180405_aT,                   false);
        defaultDisplacements.put(Blocks.field_150386_bk,             false);
        defaultDisplacements.put(Blocks.field_180390_bo,                 false);
        defaultDisplacements.put(Blocks.field_180391_bp,              false);
        defaultDisplacements.put(Blocks.field_180392_bq,               false);
        defaultDisplacements.put(Blocks.field_180386_br,              false);
        defaultDisplacements.put(Blocks.field_180385_bs,            false);
        defaultDisplacements.put(Blocks.field_180387_bt,              false);
        defaultDisplacements.put(Blocks.field_150452_aw,          false);
        defaultDisplacements.put(Blocks.field_150456_au,           false);
        defaultDisplacements.put(Blocks.field_150445_bS,  false);
        defaultDisplacements.put(Blocks.field_150443_bT,  false);
        defaultDisplacements.put(Blocks.field_150468_ap,                         false);
        defaultDisplacements.put(Blocks.field_150411_aY,                      false);
        defaultDisplacements.put(Blocks.field_150410_aZ,                     false);
        defaultDisplacements.put(Blocks.field_150397_co,             false);
        defaultDisplacements.put(Blocks.field_150427_aO,                         false);
        defaultDisplacements.put(Blocks.field_150384_bq,                     false);
        defaultDisplacements.put(Blocks.field_150463_bK,               false);
        defaultDisplacements.put(Blocks.field_180401_cv,                        false);
        defaultDisplacements.put(Blocks.field_180393_cK,                false);
        defaultDisplacements.put(Blocks.field_180394_cL,                    false);
        defaultDisplacements.put(Blocks.field_150414_aQ,                           false);

        defaultDisplacements.put(Blocks.field_150454_av,     false);
        defaultDisplacements.put(Blocks.field_150472_an, false);
        defaultDisplacements.put(Blocks.field_150444_as,     false);
        defaultDisplacements.put(Blocks.field_150436_aH,         false);
    }
    protected Map<Block, Boolean> displacements = Maps.newHashMap();

    public static final PropertyInteger LEVEL = PropertyInteger.func_177719_a("level", 0, 15);
    protected int quantaPerBlock = 8;
    protected float quantaPerBlockFloat = 8F;
    protected int density = 1;
    protected int densityDir = -1;
	protected int temperature = 295;

    protected int tickRate = 20;
    protected EnumWorldBlockLayer renderLayer = EnumWorldBlockLayer.TRANSLUCENT;
    protected int maxScaledLight = 0;

    protected final String fluidName;

    public BlockFluidBase(Fluid fluid, Material material)
    {
        super(material);
        this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.func_149675_a(true);
        this.func_149649_H();

        this.fluidName = fluid.getName();
        this.density = fluid.density;
        this.temperature = fluid.temperature;
        this.maxScaledLight = fluid.luminosity;
        this.tickRate = fluid.viscosity / 200;
        this.densityDir = fluid.density > 0 ? -1 : 1;
        fluid.setBlock(this);

        displacements.putAll(defaultDisplacements);
        this.func_180632_j(field_176227_L.func_177621_b().func_177226_a(LEVEL, 0));
    }

    @Override
    protected BlockState func_180661_e()
    {
        return new BlockState(this, LEVEL);
    }

    @Override
    public int func_176201_c(IBlockState state)
    {
        return ((Integer)state.func_177229_b(LEVEL)).intValue();
    }
    public BlockFluidBase setQuantaPerBlock(int quantaPerBlock)
    {
        if (quantaPerBlock > 16 || quantaPerBlock < 1) quantaPerBlock = 8;
        this.quantaPerBlock = quantaPerBlock;
        this.quantaPerBlockFloat = quantaPerBlock;
        return this;
    }

    public BlockFluidBase setDensity(int density)
    {
        if (density == 0) density = 1;
        this.density = density;
        this.densityDir = density > 0 ? -1 : 1;
        return this;
    }

    public BlockFluidBase setTemperature(int temperature)
    {
        this.temperature = temperature;
        return this;
    }

    public BlockFluidBase setTickRate(int tickRate)
    {
        if (tickRate <= 0) tickRate = 20;
        this.tickRate = tickRate;
        return this;
    }

    public BlockFluidBase setRenderLayer(EnumWorldBlockLayer renderLayer)
    {
        this.renderLayer = renderLayer;
        return this;
    }

    public BlockFluidBase setMaxScaledLight(int maxScaledLight)
    {
        this.maxScaledLight = maxScaledLight;
        return this;
    }

    /**
     * Returns true if the block at (pos) is displaceable. Does not displace the block.
     */
    public boolean canDisplace(IBlockAccess world, BlockPos pos)
    {
        if (world.func_175623_d(pos)) return true;

        Block block = world.func_180495_p(pos).func_177230_c();

        if (block == this)
        {
            return false;
        }

        if (displacements.containsKey(block))
        {
            return displacements.get(block);
        }

        Material material = block.func_149688_o();
        if (material.func_76230_c() || material == Material.field_151567_E)
        {
            return false;
        }

        int density = getDensity(world, pos);
        if (density == Integer.MAX_VALUE)
        {
        	 return true;
        }

        if (this.density > density)
        {
        	return true;
        }
        else
        {
        	return false;
        }
    }

    /**
     * Attempt to displace the block at (pos), return true if it was displaced.
     */
    public boolean displaceIfPossible(World world, BlockPos pos)
    {
        if (world.func_175623_d(pos))
        {
            return true;
        }

        IBlockState state = world.func_180495_p(pos);
        Block block = state.func_177230_c();
        if (block == this)
        {
            return false;
        }

        if (displacements.containsKey(block))
        {
            if (displacements.get(block))
            {
                block.func_176226_b(world, pos, state, 0);
                return true;
            }
            return false;
        }

        Material material = block.func_149688_o();
        if (material.func_76230_c() || material == Material.field_151567_E)
        {
            return false;
        }

        int density = getDensity(world, pos);
        if (density == Integer.MAX_VALUE)
        {
        	 block.func_176226_b(world, pos, state, 0);
        	 return true;
        }

        if (this.density > density)
        {
        	return true;
        }
        else
        {
        	return false;
        }
    }

    public abstract int getQuantaValue(IBlockAccess world, BlockPos pos);

    @Override
    public abstract boolean func_176209_a(IBlockState state, boolean fullHit);

    public abstract int getMaxRenderHeightMeta();

    /* BLOCK FUNCTIONS */
    @Override
    public void func_176213_c(World world, BlockPos pos, IBlockState state)
    {
        world.func_175684_a(pos, this, tickRate);
    }

    @Override
    public void func_176204_a(World world, BlockPos pos, IBlockState state, Block neighborBlock)
    {
        world.func_175684_a(pos, this, tickRate);
    }

    // Used to prevent updates on chunk generation
    @Override
    public boolean func_149698_L()
    {
        return false;
    }

    @Override
    public boolean func_176205_b(IBlockAccess world, BlockPos pos)
    {
        return true;
    }

    @Override
    public AxisAlignedBB func_180640_a(World world, BlockPos pos, IBlockState state)
    {
        return null;
    }

    @Override
    public Item func_180660_a(IBlockState state, Random rand, int fortune)
    {
        return null;
    }

    @Override
    public int func_149745_a(Random par1Random)
    {
        return 0;
    }

    @Override
    public int func_149738_a(World world)
    {
        return tickRate;
    }

    @Override
    public Vec3 func_176197_a(World world, BlockPos pos, Entity entity, Vec3 vec)
    {
        if (densityDir > 0) return vec;
        Vec3 vec_flow = this.getFlowVector(world, pos);
        return vec.func_72441_c(
                vec_flow.field_72450_a * (quantaPerBlock * 4),
                vec_flow.field_72448_b * (quantaPerBlock * 4),
                vec_flow.field_72449_c * (quantaPerBlock * 4));
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos)
    {
        if (maxScaledLight == 0)
        {
            return super.getLightValue(world, pos);
        }
        int data = ((Integer)world.func_180495_p(pos).func_177229_b(LEVEL)).intValue();
        return (int) (data / quantaPerBlockFloat * maxScaledLight);
    }

    @Override
    public int func_149645_b()
    {
        return FluidRegistry.renderIdFluid;
    }

    @Override
    public boolean func_149662_c()
    {
        return false;
    }

    @Override
    public boolean func_149686_d()
    {
        return false;
    }

    /* Never used...?
    @Override
    public float getBlockBrightness(World world, BlockPos pos)
    {
        float lightThis = world.getLightBrightness(pos);
        float lightUp = world.getLightBrightness(x, y + 1, z);
        return lightThis > lightUp ? lightThis : lightUp;
    }
    */

    @Override
    public int func_176207_c(IBlockAccess world, BlockPos pos)
    {
        int lightThis     = world.func_175626_b(pos, 0);
        int lightUp       = world.func_175626_b(pos.func_177984_a(), 0);
        int lightThisBase = lightThis & 255;
        int lightUpBase   = lightUp & 255;
        int lightThisExt  = lightThis >> 16 & 255;
        int lightUpExt    = lightUp >> 16 & 255;
        return (lightThisBase > lightUpBase ? lightThisBase : lightUpBase) |
               ((lightThisExt > lightUpExt ? lightThisExt : lightUpExt) << 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer func_180664_k()
    {
        return this.renderLayer;
    }

    @Override
    public boolean func_176225_a(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        Block block = world.func_180495_p(pos).func_177230_c();
        if (block != this)
        {
            return !block.func_149662_c();
        }
        return block.func_149688_o() == this.func_149688_o() ? false : super.func_176225_a(world, pos, side);
    }

    /* FLUID FUNCTIONS */
    public static final int getDensity(IBlockAccess world, BlockPos pos)
    {
        Block block = world.func_180495_p(pos).func_177230_c();
        if (!(block instanceof BlockFluidBase))
        {
            return Integer.MAX_VALUE;
        }
        return ((BlockFluidBase)block).density;
    }

    public static final int getTemperature(IBlockAccess world, BlockPos pos)
    {
        Block block = world.func_180495_p(pos).func_177230_c();
        if (!(block instanceof BlockFluidBase))
        {
            return Integer.MAX_VALUE;
        }
        return ((BlockFluidBase)block).temperature;
    }

    public static double getFlowDirection(IBlockAccess world, BlockPos pos)
    {
        Block block = world.func_180495_p(pos).func_177230_c();
        if (!block.func_149688_o().func_76224_d())
        {
            return -1000.0;
        }
        Vec3 vec = ((BlockFluidBase) block).getFlowVector(world, pos);
        return vec.field_72450_a == 0.0D && vec.field_72449_c == 0.0D ? -1000.0D : Math.atan2(vec.field_72449_c, vec.field_72450_a) - Math.PI / 2D;
    }

    public final int getQuantaValueBelow(IBlockAccess world, BlockPos pos, int belowThis)
    {
        int quantaRemaining = getQuantaValue(world, pos);
        if (quantaRemaining >= belowThis)
        {
            return -1;
        }
        return quantaRemaining;
    }

    public final int getQuantaValueAbove(IBlockAccess world, BlockPos pos, int aboveThis)
    {
        int quantaRemaining = getQuantaValue(world, pos);
        if (quantaRemaining <= aboveThis)
        {
            return -1;
        }
        return quantaRemaining;
    }

    public final float getQuantaPercentage(IBlockAccess world, BlockPos pos)
    {
        int quantaRemaining = getQuantaValue(world, pos);
        return quantaRemaining / quantaPerBlockFloat;
    }

    public Vec3 getFlowVector(IBlockAccess world, BlockPos pos)
    {
        Vec3 vec = new Vec3(0.0D, 0.0D, 0.0D);
        int decay = quantaPerBlock - getQuantaValue(world, pos);

        for (int side = 0; side < 4; ++side)
        {
            int x2 = pos.func_177958_n();
            int z2 = pos.func_177952_p();

            switch (side)
            {
                case 0: --x2; break;
                case 1: --z2; break;
                case 2: ++x2; break;
                case 3: ++z2; break;
            }

            BlockPos pos2 = new BlockPos(x2, pos.func_177956_o(), z2);
            int otherDecay = quantaPerBlock - getQuantaValue(world, pos2);
            if (otherDecay >= quantaPerBlock)
            {
                if (!world.func_180495_p(pos2).func_177230_c().func_149688_o().func_76230_c())
                {
                    otherDecay = quantaPerBlock - getQuantaValue(world, pos2.func_177977_b());
                    if (otherDecay >= 0)
                    {
                        int power = otherDecay - (decay - quantaPerBlock);
                        vec = vec.func_72441_c((pos2.func_177958_n() - pos.func_177958_n()) * power, 0, (pos2.func_177952_p() - pos.func_177952_p()) * power);
                    }
                }
            }
            else if (otherDecay >= 0)
            {
                int power = otherDecay - decay;
                vec = vec.func_72441_c((pos2.func_177958_n() - pos.func_177958_n()) * power, 0, (pos2.func_177952_p() - pos.func_177952_p()) * power);
            }
        }

        if (world.func_180495_p(pos.func_177984_a()).func_177230_c() == this)
        {
            boolean flag =
                func_176212_b(world, pos.func_177982_a( 0,  0, -1), EnumFacing.NORTH) ||
                func_176212_b(world, pos.func_177982_a( 0,  0,  1), EnumFacing.SOUTH) ||
                func_176212_b(world, pos.func_177982_a(-1,  0,  0), EnumFacing.WEST) ||
                func_176212_b(world, pos.func_177982_a( 1,  0,  0), EnumFacing.EAST) ||
                func_176212_b(world, pos.func_177982_a( 0,  1, -1), EnumFacing.NORTH) ||
                func_176212_b(world, pos.func_177982_a( 0,  1,  1), EnumFacing.SOUTH) ||
                func_176212_b(world, pos.func_177982_a(-1,  1,  0), EnumFacing.WEST) ||
                func_176212_b(world, pos.func_177982_a( 1,  1,  0), EnumFacing.EAST);

            if (flag)
            {
                vec = vec.func_72432_b().func_72441_c(0.0D, -6.0D, 0.0D);
            }
        }
        vec = vec.func_72432_b();
        return vec;
    }

    /* IFluidBlock */
    @Override
    public Fluid getFluid()
    {
        return FluidRegistry.getFluid(fluidName);
    }

    @Override
    public float getFilledPercentage(World world, BlockPos pos)
    {
        int quantaRemaining = getQuantaValue(world, pos) + 1;
        float remaining = quantaRemaining / quantaPerBlockFloat;
        if (remaining > 1) remaining = 1.0f;
        return remaining * (density > 0 ? 1 : -1);
    }
}