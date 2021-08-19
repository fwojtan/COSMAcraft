package net.fwojtan.cosmacraft.common.block;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class EmptyRackBlock extends RackBlock{
    //for decoration purposes

    public EmptyRackBlock() {super();}

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(SHOULD_RENDER, true);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ModTileEntities().EMPTY_RACK_TILE_ENTITY.get().create();
    }

}
