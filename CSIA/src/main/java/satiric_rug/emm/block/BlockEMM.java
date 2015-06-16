package satiric_rug.emm.block;

import satiric_rug.emm.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockEMM extends Block {
	
	public BlockEMM(Material material) {
		super(material);
	}
	
	public BlockEMM() {
		super(Material.rock);
	}
	
	@Override
	public String getUnlocalizedName() {
		return String.format("tile.%s%s", Reference.MOD_ID.toLowerCase() + ":", getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
	}
	
	protected String getUnwrappedUnlocalizedName(String unlocalizedName) {
		return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
	}
}
