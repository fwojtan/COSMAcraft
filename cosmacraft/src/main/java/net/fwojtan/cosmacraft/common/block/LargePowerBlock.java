package net.fwojtan.cosmacraft.common.block;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class LargePowerBlock extends SmallPowerBlock{

    public LargePowerBlock() {super();}

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ModTileEntities().LARGE_POWER_TILE_ENTITY.get().create();
    }

}
