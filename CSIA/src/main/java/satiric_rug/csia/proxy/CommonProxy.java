package satiric_rug.csia.proxy;

import satiric_rug.csia.tileentity.TileEntityPortal;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonProxy {
	
	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntityPortal.class, TileEntityPortal.publicName);
	}
	
	public void setForcedChunkLoadingCallback(Object mod, LoadingCallback callback) {
		ForgeChunkManager.setForcedChunkLoadingCallback(mod, callback);
	}
	
}
