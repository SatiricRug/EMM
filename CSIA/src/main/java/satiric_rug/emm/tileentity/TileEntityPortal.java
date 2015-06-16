package satiric_rug.emm.tileentity;

import satiric_rug.emm.utility.LogHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

public class TileEntityPortal extends TileEntity {
	
	public static final String publicName = "tileEntityPortal";
	private String name = "tileEntityPortal";
	
	//BlockPos of linked portal (the "other side" of the portal).
	private BlockPos linkedBlockPos =  new BlockPos(this.getPos().getX() , this.getPos().getY(), this.getPos().getZ());
	
	
	
	public String getName() {
		return this.name;
	}
	
	public BlockPos getLinkedBlockPos() {
		return this.linkedBlockPos;
	}
	
	public void setLinkedBlockPos(BlockPos linkedBlockPos) {
		this.linkedBlockPos = linkedBlockPos;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound par1) {
		super.writeToNBT(par1);
		
		par1.setInteger("linkedBlockX", linkedBlockPos.getX());
		par1.setInteger("linkedBlockY", linkedBlockPos.getY());
		par1.setInteger("linkedBlockZ", linkedBlockPos.getZ());
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1) {
		super.readFromNBT(par1);
		
		this.linkedBlockPos = new BlockPos(par1.getInteger("linkedBlockX"),
										   par1.getInteger("linkedBlockY"),
										   par1.getInteger("linkedBlockZ"));
	}
	
	/**
	 * This code is run every tick (1/20th of a second)
	 */
	public void update() {
		if (!worldObj.isRemote) {
			Ticket ticket = ForgeChunkManager.requestTicket(name, worldObj, Type.ENTITY);
			
			//Load the chunk that this portal is in
			if (ticket != null) {
				ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(this.getPos().getX() / 16, this.getPos().getZ() / 16));
				
				LogHelper.info("Forcing chunk load at X: " + this.getPos().getX() + ", Z: " + this.getPos().getZ());
			}
		}
	}
	
}
