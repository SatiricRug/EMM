package satiric_rug.csia.block;

import satiric_rug.csia.tileentity.TileEntityPortal;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPortal extends BlockCSIA implements ITileEntityProvider {
	
	public BlockPortal() {
		super(Material.air);
		this.setUnlocalizedName("portal");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityPortal();
	}
	
	public boolean isOpaqueCube() {
		return false;
	}
	
	public boolean isFullCube() {
		return false;
	}
	
	public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
		float minX = 0f;
		float minY = 0f;
		float minZ = 0f;
		float maxX = 0.5f;
		float maxY = 0.5f;
		float maxZ = 0.5f;
		
		this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
