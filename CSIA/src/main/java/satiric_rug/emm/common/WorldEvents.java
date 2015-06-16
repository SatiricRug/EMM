package satiric_rug.emm.common;

import java.util.List;

import com.google.common.collect.ImmutableSet;

import satiric_rug.emm.reference.Reference;
import satiric_rug.emm.utility.LogHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class WorldEvents implements LoadingCallback {

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		for (Ticket ticket : tickets) {
			if (ticket.getModId().equals(Reference.MOD_ID)) {
				ImmutableSet<ChunkCoordIntPair> chunkList = ticket.getChunkList();
				for (ChunkCoordIntPair chunk: chunkList) {
					LogHelper.info("Chunk Loaded at X = " + chunk.getCenterXPos() + " Z = " + chunk.getCenterZPosition());
				}
			}
		}
	}

}
