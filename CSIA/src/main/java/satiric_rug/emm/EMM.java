package satiric_rug.emm;

import satiric_rug.emm.block.BlockEMM;
import satiric_rug.emm.common.WorldEvents;
import satiric_rug.emm.proxy.CommonProxy;
import satiric_rug.emm.reference.Names;
import satiric_rug.emm.reference.Reference;
import satiric_rug.emm.utility.LogHelper;
import net.minecraft.block.BlockPortal;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class EMM {
	
	@Mod.Instance(Reference.MOD_ID)
	public static EMM instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
		
		LogHelper.info("Pre initialization complete!");
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		
		LogHelper.info("Initialization complete!");
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		LogHelper.info("Post initialization complete!");
	}
	
}
