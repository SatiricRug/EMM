package satiric_rug.csia_old.init;

import net.minecraftforge.fml.common.registry.GameRegistry;
import satiric_rug.csia_old.block.BlockPortal;

public class ModBlocks {

	public static final BlockPortal portal = new BlockPortal();
	
	public static void init() {
		GameRegistry.registerBlock(portal, "portal");
	}
}
