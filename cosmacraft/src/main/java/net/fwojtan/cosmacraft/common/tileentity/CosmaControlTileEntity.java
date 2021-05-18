package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class CosmaControlTileEntity extends ParentTileEntity{

    public CosmaControlTileEntity(TileEntityType<?> type){super(type);}
    public CosmaControlTileEntity(){this(ModTileEntities.COSMA_CONTROL_TILE_ENTITY.get());}

    @Override
    public void tick() {
        if(!childrenPlaced){placeChildren();childrenPlaced=true;}
    }

    @Override
    public void placeChildren() {
        //do things
    }

    @Override
    public List<BlockPos> createChildPositonList() {
        return super.createChildPositonList();
    }
}
