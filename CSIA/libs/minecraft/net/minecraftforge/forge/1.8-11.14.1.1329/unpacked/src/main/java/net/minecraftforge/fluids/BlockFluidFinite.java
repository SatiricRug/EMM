
package net.minecraftforge.fluids;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * This is a cellular-automata based finite fluid block implementation.
 *
 * It is highly recommended that you use/extend this class for finite fluid blocks.
 *
 * @author OvermindDL1, KingLemming
 *
 */
public class BlockFluidFinite extends BlockFluidBase
{
    public BlockFluidFinite(Fluid fluid, Material material)
    {
        super(fluid, material);
    }

    @Override
    public int getQuantaValue(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.func_180495_p(pos);
        if (state.func_177230_c().isAir(world, pos))
        {
            return 0;
        }

        if (state.func_177230_c() != this)
        {
            return -1;
        }
        return ((Integer)state.func_177229_b(LEVEL)) + 1;
    }

    @Override
    public boolean func_176209_a(IBlockState state, boolean fullHit)
    {
        return fullHit && ((Integer)state.func_177229_b(LEVEL)) == quantaPerBlock - 1;
    }

    @Override
    public int getMaxRenderHeightMeta()
    {
        return quantaPerBlock - 1;
    }

    @Override
    public void func_180650_b(World world, BlockPos pos, IBlockState state, Random rand)
    {
        boolean changed = false;
        int quantaRemaining = ((Integer)state.func_177229_b(LEVEL)) + 1;

        // Flow vertically if possible
        int prevRemaining = quantaRemaining;
        quantaRemaining = tryToFlowVerticallyInto(world, pos, quantaRemaining);

        if (quantaRemaining < 1)
        {
            return;
        }
        else if (quantaRemaining != prevRemaining)
        {
            changed = true;
            if (quantaRemaining == 1)
            {
                world.func_180501_a(pos, state.func_177226_a(LEVEL, quantaRemaining - 1), 2);
                return;
            }
        }
        else if (quantaRemaining == 1)
        {
            return;
        }

        // Flow out if possible
        int lowerthan = quantaRemaining - 1;
        int total = quantaRemaining;
        int count = 1;

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
        {
            BlockPos off = pos.func_177972_a(side);
            if (displaceIfPossible(world, off))
                world.func_175698_g(off);

            int quanta = getQuantaValueBelow(world, off, lowerthan);
            if (quanta >= 0)
            {
                count++;
                total += quanta;
            }
        }

        if (count == 1)
        {
            if (changed)
            {
                world.func_180501_a(pos, state.func_177226_a(LEVEL, quantaRemaining - 1), 2);
            }
            return;
        }

        int each = total / count;
        int rem = total % count;

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL)
        {
            BlockPos off = pos.func_177972_a(side);
            int quanta = getQuantaValueBelow(world, off, lowerthan);
            if (quanta >= 0)
            {
                int newquanta = each;
                if (rem == count || rem > 1 && rand.nextInt(count - rem) != 0)
                {
                    ++newquanta;
                    --rem;
                }

                if (newquanta != quanta)
                {
                    if (newquanta == 0)
                    {
                        world.func_175698_g(off);
                    }
                    else
                    {
                        world.func_180501_a(off, func_176223_P().func_177226_a(LEVEL, newquanta - 1), 2);
                    }
                    world.func_175684_a(off, this, tickRate);
                }
                --count;
            }
        }

        if (rem > 0)
        {
            ++each;
        }
        world.func_180501_a(pos, state.func_177226_a(LEVEL, each - 1), 2);
    }

    public int tryToFlowVerticallyInto(World world, BlockPos pos, int amtToInput)
    {
        IBlockState myState = world.func_180495_p(pos);
        BlockPos other = pos.func_177982_a(0, densityDir, 0);
        if (other.func_177956_o() < 0 || other.func_177956_o() >= world.func_72800_K())
        {
            world.func_175698_g(pos);
            return 0;
        }

        int amt = getQuantaValueBelow(world, other, quantaPerBlock);
        if (amt >= 0)
        {
            amt += amtToInput;
            if (amt > quantaPerBlock)
            {
                world.func_180501_a(other, myState.func_177226_a(LEVEL, quantaPerBlock - 1), 3);
                world.func_175684_a(other, this, tickRate);
                return amt - quantaPerBlock;
            }
            else if (amt > 0)
            {
                world.func_180501_a(other, myState.func_177226_a(LEVEL, amt - 1), 3);
                world.func_175684_a(other, this, tickRate);
                world.func_175698_g(pos);
                return 0;
            }
            return amtToInput;
        }
        else
        {
            int density_other = getDensity(world, other);
            if (density_other == Integer.MAX_VALUE)
            {
                if (displaceIfPossible(world, other))
                {
                    world.func_180501_a(other, myState.func_177226_a(LEVEL, amtToInput - 1), 3);
                    world.func_175684_a(other, this, tickRate);
                    world.func_175698_g(pos);
                    return 0;
                }
                else
                {
                    return amtToInput;
                }
            }

            if (densityDir < 0)
            {
                if (density_other < density) // then swap
                {
                    IBlockState state = world.func_180495_p(other);
                    world.func_180501_a(other, myState.func_177226_a(LEVEL, amtToInput - 1), 3);
                    world.func_180501_a(pos,   state, 3);
                    world.func_175684_a(other, this, tickRate);
                    world.func_175684_a(pos,   state.func_177230_c(), state.func_177230_c().func_149738_a(world));
                    return 0;
                }
            }
            else
            {
                if (density_other > density)
                {
                    IBlockState state = world.func_180495_p(other);
                    world.func_180501_a(other, myState.func_177226_a(LEVEL, amtToInput - 1), 3);
                    world.func_180501_a(other, state, 3);
                    world.func_175684_a(other, this,  tickRate);
                    world.func_175684_a(other, state.func_177230_c(), state.func_177230_c().func_149738_a(world));
                    return 0;
                }
            }
            return amtToInput;
        }
    }

    /* IFluidBlock */
    @Override
    public FluidStack drain(World world, BlockPos pos, boolean doDrain)
    {
        if (doDrain)
        {
            world.func_175698_g(pos);
        }

        return new FluidStack(getFluid(),
                MathHelper.func_76141_d(getQuantaPercentage(world, pos) * FluidContainerRegistry.BUCKET_VOLUME));
    }

    @Override
    public boolean canDrain(World world, BlockPos pos)
    {
        return true;
    }
}