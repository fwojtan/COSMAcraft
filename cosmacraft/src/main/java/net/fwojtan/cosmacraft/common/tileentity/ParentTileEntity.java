package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class ParentTileEntity extends TileEntity implements ITickableTileEntity {

    private boolean yeah = false;

    public ParentTileEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    public ParentTileEntity() {this(ModTileEntities.PARENT_TILE_ENTITY.get());}

    @Override
    public void tick() {
        if (yeah){System.out.println("Yeah!");yeah=true;}


    }
}
