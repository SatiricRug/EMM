package satiric_rug.emm.block;

import satiric_rug.emm.EMM;
import satiric_rug.emm.reference.Names;
import satiric_rug.emm.tileentity.TileEntityPortal;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class BlockPortal extends BlockEMM implements ITileEntityProvider {
    
	public BlockPortal() {
		super(Material.portal);
		this.setDefaultState(this.blockState.getBaseState());
		this.setUnlocalizedName(Names.Blocks.PORTAL);
		this.setLightOpacity(0);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}
	
    @Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
	    return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean isFullCube() {
		return false;
	}

	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if(!worldIn.isRemote) { //always load the chunk this block is in
			Ticket ticket = ForgeChunkManager.requestTicket(EMM.instance, worldIn, Type.NORMAL);
			
			if(ticket != null) {
				ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair((int)pos.getX() / 16, (int)pos.getZ() / 16));
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		TileEntityPortal par1 = new TileEntityPortal();
		
		return par1;
	}
    
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		TileEntityPortal thisTileEntityPortal = (TileEntityPortal) worldIn.getTileEntity(pos);
		
		entityIn.setPositionAndUpdate((entityIn.posX) + 
								thisTileEntityPortal.getLinkedBlockPos().getX(), 
							 (entityIn.posY) + 
							 	thisTileEntityPortal.getLinkedBlockPos().getY(), 
							 (entityIn.posZ) + 
							 	thisTileEntityPortal.getLinkedBlockPos().getZ());
	}
}
