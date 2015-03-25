package satiric_rug.csia.block;

import satiric_rug.csia.tileentity.TileEntityPortal;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPortal extends BlockCSIADirectional implements ITileEntityProvider {
	
	public BlockPortal() {
		super(Material.air);
		this.setUnlocalizedName("portal");
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityPortal();
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return null;
    }

    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
    
    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
        return ((EnumFacing)state.getValue(FACING)).getIndex();
    }
    
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
		EnumFacing facing = (EnumFacing)worldIn.getBlockState(pos).getValue(FACING);
		
		float minX = 0f;
		float minY = 0f;
		float minZ = 0f;
		float maxX = 0.5f;
		float maxY = 0.5f;
		float maxZ = 0.5f;
		
		this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);
        
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }
    
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		TileEntityPortal thisTileEntityPortal = (TileEntityPortal) worldIn.getTileEntity(pos);
		
		entityIn.setPosition((entityIn.posX % 1) + thisTileEntityPortal.getLinkedBlockPos().getX(), 
							 (entityIn.posY % 1) + thisTileEntityPortal.getLinkedBlockPos().getY(), 
							 (entityIn.posZ % 1) + thisTileEntityPortal.getLinkedBlockPos().getZ());
	}
}
