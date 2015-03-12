package net.minecraftforge.fluids;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * This is a fluid block implementation which emulates vanilla Minecraft fluid behavior.
 *
 * It is highly recommended that you use/extend this class for "classic" fluid blocks.
 *
 * @author King Lemming
 *
 */
public class BlockFluidClassic extends BlockFluidBase
{
    protected boolean[] isOptimalFlowDirection = new boolean[4];
    protected int[] flowCost = new int[4];

    protected FluidStack stack;
    public BlockFluidClassic(Fluid fluid, Material material)
    {
        super(fluid, material);
        stack = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
    }

    public BlockFluidClassic setFluidStack(FluidStack stack)
    {
        this.stack = stack;
        return this;
    }

    public BlockFluidClassic setFluidStackAmount(int amount)
    {
        this.stack.amount = amount;
        return this;
    }

    @Override
    public int getQuantaValue(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        if (state.func_177230_c() == Blocks.field_150350_a)
        {
            return 0;
        }

        if (state.func_177230_c() != this)
        {
            return -1;
        }

        int quantaRemaining = quantaPerBlock - ((Integer)state.func_177229_b(LEVEL)).intValue();
        return quantaRemaining;
    }

    @Override
    public boolean func_176209_a(IBlockState state, boolean fullHit)
    {
        return fullHit && ((Integer)state.func_177229_b(LEVEL)).intValue() == 0;
    }

    @Override
    public int getMaxRenderHeightMeta()
    {
        return 0;
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos)
    {
        if (maxScaledLight == 0)
        {
            return super.getLightValue(world, pos);
        }
        int data = quantaPerBlock - ((Integer)world.func_180495_p(pos).func_177229_b(LEVEL)).intValue() - 1;
        return (int) (data / quantaPerBlockFloat * maxScaledLight);
    }

    @Override
    public void func_180650_b(World world, BlockPos pos, IBlockState state, Random rand)
    {
        int quantaRemaining = quantaPerBlock - ((Integer)state.func_177229_b(LEVEL)).intValue();
        int expQuanta = -101;

        // check adjacent block levels if non-source
        if (quantaRemaining < quantaPerBlock)
        {
            if (world.func_180495_p(pos.func_177982_a( 0, -densityDir,  0)).func_177230_c() == this ||
                world.func_180495_p(pos.func_177982_a(-1, -densityDir,  0)).func_177230_c() == this ||
                world.func_180495_p(pos.func_177982_a( 1, -densityDir,  0)).func_177230_c() == this ||
                world.func_180495_p(pos.func_177982_a( 0, -densityDir, -1)).func_177230_c() == this ||
                world.func_180495_p(pos.func_177982_a( 0, -densityDir,  1)).func_177230_c() == this)
            {
                expQuanta = quantaPerBlock - 1;
            }
            else
            {
                int maxQuanta = -100;
                maxQuanta = getLargerQuanta(world, pos.func_177982_a(-1, 0,  0), maxQuanta);
                maxQuanta = getLargerQuanta(world, pos.func_177982_a( 1, 0,  0), maxQuanta);
                maxQuanta = getLargerQuanta(world, pos.func_177982_a( 0, 0, -1), maxQuanta);
                maxQuanta = getLargerQuanta(world, pos.func_177982_a( 0, 0,  1), maxQuanta);

                expQuanta = maxQuanta - 1;
            }

            // decay calculation
            if (expQuanta != quantaRemaining)
            {
                quantaRemaining = expQuanta;

                if (expQuanta <= 0)
                {
                    world.func_175698_g(pos);
                }
                else
                {
                    world.func_180501_a(pos, state.func_177226_a(LEVEL, quantaPerBlock - expQuanta), 2);
                    world.func_175684_a(pos, this, tickRate);
                    world.func_175685_c(pos, this);
                }
            }
        }
        // This is a "source" block, set meta to zero, and send a server only update
        else if (quantaRemaining >= quantaPerBlock)
        {
            world.func_180501_a(pos, this.func_176223_P(), 2);
        }

        // Flow vertically if possible
        if (canDisplace(world, pos.func_177981_b(densityDir)))
        {
            flowIntoBlock(world, pos.func_177981_b(densityDir), 1);
            return;
        }

        // Flow outward if possible
        int flowMeta = quantaPerBlock - quantaRemaining + 1;
        if (flowMeta >= quantaPerBlock)
        {
            return;
        }

        if (isSourceBlock(world, pos) || !isFlowingVertically(world, pos))
        {
            if (world.func_180495_p(pos.func_177979_c(densityDir)).func_177230_c() == this)
            {
                flowMeta = 1;
            }
            boolean flowTo[] = getOptimalFlowDirections(world, pos);

            if (flowTo[0]) flowIntoBlock(world, pos.func_177982_a(-1, 0,  0), flowMeta);
            if (flowTo[1]) flowIntoBlock(world, pos.func_177982_a( 1, 0,  0), flowMeta);
            if (flowTo[2]) flowIntoBlock(world, pos.func_177982_a( 0, 0, -1), flowMeta);
            if (flowTo[3]) flowIntoBlock(world, pos.func_177982_a( 0, 0,  1), flowMeta);
        }
    }

    public boolean isFlowingVertically(IBlockAccess world, BlockPos pos)
    {
        return world.func_180495_p(pos.func_177981_b(densityDir)).func_177230_c() == this ||
            (world.func_180495_p(pos).func_177230_c() == this && canFlowInto(world, pos.func_177981_b(densityDir)));
    }

    public boolean isSourceBlock(IBlockAccess world, BlockPos pos)
    {
        return world.func_180495_p(pos) == this && ((Integer)world.func_180495_p(pos).func_177229_b(LEVEL)).intValue() == 0;
    }

    protected boolean[] getOptimalFlowDirections(World world, BlockPos pos)
    {
        for (int side = 0; side < 4; side++)
        {
            flowCost[side] = 1000;

            BlockPos pos2 = pos;

            switch (side)
            {
                case 0: pos2 = pos2.func_177982_a(-1, 0,  0); break;
                case 1: pos2 = pos2.func_177982_a( 1, 0,  0); break;
                case 2: pos2 = pos2.func_177982_a( 0, 0, -1); break;
                case 3: pos2 = pos2.func_177982_a( 0, 0,  1); break;
            }

            if (!canFlowInto(world, pos2) || isSourceBlock(world, pos2))
            {
                continue;
            }

            if (canFlowInto(world, pos2.func_177982_a(0, densityDir, 0)))
            {
                flowCost[side] = 0;
            }
            else
            {
                flowCost[side] = calculateFlowCost(world, pos2, 1, side);
            }
        }

        int min = flowCost[0];
        for (int side = 1; side < 4; side++)
        {
            if (flowCost[side] < min)
            {
                min = flowCost[side];
            }
        }
        for (int side = 0; side < 4; side++)
        {
            isOptimalFlowDirection[side] = flowCost[side] == min;
        }
        return isOptimalFlowDirection;
    }

    protected int calculateFlowCost(World world, BlockPos pos, int recurseDepth, int side)
    {
        int cost = 1000;
        for (int adjSide = 0; adjSide < 4; adjSide++)
        {
            if ((adjSide == 0 && side == 1) ||
                (adjSide == 1 && side == 0) ||
                (adjSide == 2 && side == 3) ||
                (adjSide == 3 && side == 2))
            {
                continue;
            }

            BlockPos pos2 = pos;

            switch (adjSide)
            {
                case 0: pos2 = pos2.func_177982_a(-1, 0,  0); break;
                case 1: pos2 = pos2.func_177982_a( 1, 0,  0); break;
                case 2: pos2 = pos2.func_177982_a( 0, 0, -1); break;
                case 3: pos2 = pos2.func_177982_a( 0, 0,  1); break;
            }

            if (!canFlowInto(world, pos2) || isSourceBlock(world, pos2))
            {
                continue;
            }

            if (canFlowInto(world, pos2.func_177982_a(0, densityDir, 0)))
            {
                return recurseDepth;
            }

            if (recurseDepth >= 4)
            {
                continue;
            }

            int min = calculateFlowCost(world, pos2, recurseDepth + 1, adjSide);
            if (min < cost)
            {
                cost = min;
            }
        }
        return cost;
    }

    protected void flowIntoBlock(World world, BlockPos pos, int meta)
    {
        if (meta < 0) return;
        if (displaceIfPossible(world, pos))
        {
            world.func_180501_a(pos, this.func_176194_O().func_177621_b().func_177226_a(LEVEL, meta), 3);
        }
    }

    protected boolean canFlowInto(IBlockAccess world, BlockPos pos)
    {
        if (world.func_175623_d(pos)) return true;

        Block block = world.func_180495_p(pos).func_177230_c();
        if (block == this)
        {
            return true;
        }

        if (displacements.containsKey(block))
        {
            return displacements.get(block);
        }

        Material material = block.func_149688_o();
        if (material.func_76230_c()  ||
            material == Material.field_151586_h ||
            material == Material.field_151587_i  ||
            material == Material.field_151567_E)
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

    protected int getLargerQuanta(IBlockAccess world, BlockPos pos, int compare)
    {
        int quantaRemaining = getQuantaValue(world, pos);
        if (quantaRemaining <= 0)
        {
            return compare;
        }
        return quantaRemaining >= compare ? quantaRemaining : compare;
    }

    /* IFluidBlock */
    @Override
    public FluidStack drain(World world, BlockPos pos, boolean doDrain)
    {
        if (!isSourceBlock(world, pos))
        {
            return null;
        }

        if (doDrain)
        {
            world.func_175698_g(pos);
        }

        return stack.copy();
    }

    @Override
    public boolean canDrain(World world, BlockPos pos)
    {
        return isSourceBlock(world, pos);
    }
}