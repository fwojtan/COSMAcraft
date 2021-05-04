package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

public class ChildTileEntity extends TileEntity {

    public BlockPos parentPosition;

    public ChildTileEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    public ChildTileEntity() {this(ModTileEntities.CHILD_TILE_ENTITY.get());}


}
