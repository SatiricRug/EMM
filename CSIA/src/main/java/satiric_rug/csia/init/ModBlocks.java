package satiric_rug.csia.init;

import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;
import satiric_rug.csia.block.BlockPortal;
import satiric_rug.csia.reference.Names;

public class ModBlocks {

	public static BlockPortal portal = new BlockPortal();
	
	public static void init() {
		GameRegistry.registerBlock(portal, Names.Blocks.PORTAL);
	}
}
