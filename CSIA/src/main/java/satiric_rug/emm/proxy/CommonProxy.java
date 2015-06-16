package satiric_rug.emm.proxy;

import satiric_rug.emm.EMM;
import satiric_rug.emm.block.BlockEMM;
import satiric_rug.emm.block.BlockPortal;
import satiric_rug.emm.common.WorldEvents;
import satiric_rug.emm.reference.Names;
import satiric_rug.emm.tileentity.TileEntityPortal;
import net.minecraft.block.Block;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class CommonProxy {
	
	Block portal;
	
	public void preInit(FMLPreInitializationEvent e) {
		portal = new BlockPortal().setHardness(5.0f);
		
		GameRegistry.registerBlock(portal, Names.Blocks.PORTAL);
	}
	
	public void init(FMLInitializationEvent e) {
		GameRegistry.registerTileEntity(TileEntityPortal.class, TileEntityPortal.publicName);
		
		ForgeChunkManager.setForcedChunkLoadingCallback(EMM.instance, new WorldEvents());
	}
	
	public void postInit(FMLPostInitializationEvent e) {
		
	}
}
