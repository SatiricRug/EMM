package satiric_rug.csia.block;

//Code copied from net.minecraft.block.BlockDirectional; all rights go to Mojang

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.EnumFacing;

public class BlockCSIADirectional extends BlockCSIA {
    public static PropertyDirection FACING;

    protected BlockCSIADirectional(Material materialIn)
    {
        super(materialIn);
        
        FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    }
}
