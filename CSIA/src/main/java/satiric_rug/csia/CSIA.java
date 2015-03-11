package satiric_rug.csia;

import satiric_rug.csia.block.BlockCSIA;
import satiric_rug.csia.handler.ConfigHandler;
import satiric_rug.csia.init.ModBlocks;
import satiric_rug.csia.proxy.CommonProxy;
import satiric_rug.csia.reference.Reference;
import satiric_rug.csia.utility.LogHelper;
import net.minecraft.block.BlockPortal;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, guiFactory = Reference.GUI_FACTORY_CLASS)
public class CSIA {
	
	@Mod.Instance(Reference.MOD_ID)
	public static CSIA instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.init(event.getSuggestedConfigurationFile());
		FMLCommonHandler.instance().bus().register(new ConfigHandler());
		
		ModBlocks.init();
		
		LogHelper.info("Pre initialization complete!kewkjfrwkjwgwg;lj");
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerTileEntities();
		LogHelper.info("Initialization complete!");
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		LogHelper.info("Post initialization complete!");
	}
	
}
