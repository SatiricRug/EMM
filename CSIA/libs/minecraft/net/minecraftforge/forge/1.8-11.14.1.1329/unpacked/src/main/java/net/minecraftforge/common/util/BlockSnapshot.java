package net.minecraftforge.common.util;


import java.io.Serializable;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 * Represents a captured snapshot of a block which will not change
 * automatically.
 * <p>
 * Unlike Block, which only one object can exist per coordinate, BlockSnapshot
 * can exist multiple times for any given Block.
 */
@SuppressWarnings("serial")
public class BlockSnapshot implements Serializable
{
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("forge.debugBlockSnapshot", "false"));

    public final BlockPos pos;
    public final int dimId;
    public transient IBlockState replacedBlock;
    public int flag;
    private final NBTTagCompound nbt;
    public transient World world;
    public final UniqueIdentifier blockIdentifier;
    public final int meta;

    public BlockSnapshot(World world, BlockPos pos, IBlockState state)
    {
        this.world = world;
        this.dimId = world.field_73011_w.func_177502_q();
        this.pos = pos;
        this.replacedBlock = state;
        this.blockIdentifier = GameRegistry.findUniqueIdentifierFor(state.func_177230_c());
        this.meta = state.func_177230_c().func_176201_c(state);
        this.flag = 3;
        TileEntity te = world.func_175625_s(pos);
        if (te != null)
        {
            nbt = new NBTTagCompound();
            te.func_145841_b(nbt);
        }
        else nbt = null;
        if (DEBUG)
        {
            System.out.printf("Created BlockSnapshot - [World: %s ][Location: %d,%d,%d ][Block: %s ][Meta: %d ]", world.func_72912_H().func_76065_j(), pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), blockIdentifier, meta);
        }
    }

    public BlockSnapshot(World world, BlockPos pos, IBlockState state, NBTTagCompound nbt)
    {
        this.world = world;
        this.dimId = world.field_73011_w.func_177502_q();
        this.pos = pos;
        this.replacedBlock = state;
        this.blockIdentifier = GameRegistry.findUniqueIdentifierFor(state.func_177230_c());
        this.meta = state.func_177230_c().func_176201_c(state);
        this.flag = 3;
        this.nbt = nbt;
        if (DEBUG)
        {
            System.out.printf("Created BlockSnapshot - [World: %s ][Location: %d,%d,%d ][Block: %s ][Meta: %d ]", world.func_72912_H().func_76065_j(), pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), blockIdentifier, meta);
        }
    }

    public BlockSnapshot(World world, BlockPos pos, IBlockState state, int flag)
    {
        this(world, pos, state);
        this.flag = flag;
    }

    /**
     * Raw constructor designed for serialization usages.
     */
    public BlockSnapshot(int dimension, BlockPos pos, String modid, String blockName, int meta, int flag, NBTTagCompound nbt)
    {
        this.dimId = dimension;
        this.pos = pos;
        this.flag = flag;
        this.blockIdentifier = new UniqueIdentifier(modid + ":" + blockName);
        this.meta = meta;
        this.nbt = nbt;
    }

    public static BlockSnapshot getBlockSnapshot(World world, BlockPos pos)
    {
        return new BlockSnapshot(world, pos, world.func_180495_p(pos));
    }

    public static BlockSnapshot getBlockSnapshot(World world, BlockPos pos, int flag)
    {
        return new BlockSnapshot(world, pos, world.func_180495_p(pos), flag);
    }

    public static BlockSnapshot readFromNBT(NBTTagCompound tag)
    {
        NBTTagCompound nbt = tag.func_74767_n("hasTE") ? null : tag.func_74775_l("tileEntity");

        return new BlockSnapshot(
                tag.func_74762_e("dimension"),
                new BlockPos(tag.func_74762_e("posX"), tag.func_74762_e("posY"), tag.func_74762_e("posZ")),
                tag.func_74779_i("blockMod"),
                tag.func_74779_i("blockName"),
                tag.func_74762_e("metadata"),
                tag.func_74762_e("flag"),
                nbt);
    }

    public IBlockState getCurrentBlock()
    {
        return world.func_180495_p(pos);
    }

    public World getWorld()
    {
        if (this.world == null)
        {
            this.world = DimensionManager.getWorld(dimId);
        }
        return this.world;
    }

    public IBlockState getReplacedBlock()
    {
        if (this.replacedBlock == null)
        {
            this.replacedBlock = GameRegistry.findBlock(this.blockIdentifier.modId, this.blockIdentifier.name).func_176203_a(meta);
        }
        return this.replacedBlock;
    }

    public TileEntity getTileEntity()
    {
        if (nbt != null)
            return TileEntity.func_145827_c(nbt);
        else return null;
    }

    public boolean restore()
    {
        return restore(false);
    }

    public boolean restore(boolean force)
    {
        return restore(force, true);
    }

    public boolean restore(boolean force, boolean applyPhysics)
    {
        IBlockState current = getCurrentBlock();
        IBlockState replaced = getReplacedBlock();
        if (current.func_177230_c() != replaced.func_177230_c() || current.func_177230_c().func_176201_c(current) != replaced.func_177230_c().func_176201_c(replaced))
        {
            if (force)
            {
                world.func_180501_a(pos, replaced, applyPhysics ? 3 : 2);
            }
            else
            {
                return false;
            }
        }

        world.func_180501_a(pos, replaced, applyPhysics ? 3 : 2);
        world.func_175689_h(pos);
        TileEntity te = null;
        if (nbt != null)
        {
            te = world.func_175625_s(pos);
            if (te != null)
            {
                te.func_145839_a(nbt);
            }
        }

        if (DEBUG)
        {
            System.out.printf("Restored BlockSnapshot with data [World: %s ][Location: %d,%d,%d ][Meta: %d ][Block: %s ][TileEntity: %s ][force: %s ][applyPhysics: %s]", world.func_72912_H().func_76065_j(), pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), replaced.func_177230_c().func_176201_c(replaced), replaced.func_177230_c().delegate.name(), te, force, applyPhysics);
        }
        return true;
    }

    public boolean restoreToLocation(World world, BlockPos pos, boolean force, boolean applyPhysics)
    {
        IBlockState current = getCurrentBlock();
        IBlockState replaced = getReplacedBlock();
        if (current.func_177230_c() != replaced.func_177230_c() || current.func_177230_c().func_176201_c(current) != replaced.func_177230_c().func_176201_c(replaced))
        {
            if (force)
            {
                world.func_180501_a(pos, replaced, applyPhysics ? 3 : 2);
            }
            else
            {
                return false;
            }
        }

        world.func_180501_a(pos, replaced, applyPhysics ? 3 : 2);
        world.func_175689_h(pos);
        TileEntity te = null;
        if (nbt != null)
        {
            te = world.func_175625_s(pos);
            if (te != null)
            {
                te.func_145839_a(nbt);
            }
        }

        if (DEBUG)
        {
            System.out.printf("Restored BlockSnapshot with data [World: %s ][Location: %d,%d,%d ][Meta: %d ][Block: %s ][TileEntity: %s ][force: %s ][applyPhysics: %s]", world.func_72912_H().func_76065_j(), pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), replaced.func_177230_c().func_176201_c(replaced), replaced.func_177230_c().delegate.name(), te, force, applyPhysics);
        }
        return true;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.func_74778_a("blockMod", blockIdentifier.modId);
        compound.func_74778_a("blockName", blockIdentifier.name);
        compound.func_74768_a("posX", pos.func_177958_n());
        compound.func_74768_a("posY", pos.func_177956_o());
        compound.func_74768_a("posZ", pos.func_177952_p());
        compound.func_74768_a("flag", flag);
        compound.func_74768_a("dimension", dimId);
        compound.func_74768_a("metadata", meta);

        compound.func_74757_a("hasTE", nbt != null);

        if (nbt != null)
        {
            compound.func_74782_a("tileEntity", nbt);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final BlockSnapshot other = (BlockSnapshot) obj;
        if (!this.pos.equals(other.pos))
        {
            return false;
        }
        if (this.meta != other.meta)
        {
            return false;
        }
        if (this.dimId != other.dimId)
        {
            return false;
        }
        if (this.nbt != other.nbt && (this.nbt == null || !this.nbt.equals(other.nbt)))
        {
            return false;
        }
        if (this.world != other.world && (this.world == null || !this.world.equals(other.world)))
        {
            return false;
        }
        if (this.blockIdentifier != other.blockIdentifier && (this.blockIdentifier == null || !this.blockIdentifier.equals(other.blockIdentifier)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 73 * hash + this.pos.func_177958_n();
        hash = 73 * hash + this.pos.func_177956_o();
        hash = 73 * hash + this.pos.func_177952_p();
        hash = 73 * hash + this.meta;
        hash = 73 * hash + this.dimId;
        hash = 73 * hash + (this.nbt != null ? this.nbt.hashCode() : 0);
        hash = 73 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = 73 * hash + (this.blockIdentifier != null ? this.blockIdentifier.hashCode() : 0);
        return hash;
    }
}