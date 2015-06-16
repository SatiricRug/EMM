package satiric_rug.emm.proxy;

import satiric_rug.emm.reference.Names;
import satiric_rug.emm.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit(FMLPreInitializationEvent e) {
		super.preInit(e);
	}
	
	@Override
	public void init(FMLInitializationEvent e) {
		super.init(e);
		
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item.getItemFromBlock(super.portal), 0, new ModelResourceLocation(Reference.MOD_ID.toLowerCase() + ":" + Names.Blocks.PORTAL, "inventory"));
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);
	}
}
