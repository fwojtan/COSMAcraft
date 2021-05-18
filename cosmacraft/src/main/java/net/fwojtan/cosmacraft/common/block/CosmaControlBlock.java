package net.fwojtan.cosmacraft.common.block;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class CosmaControlBlock extends ParentBlock {

    public CosmaControlBlock(){super();}

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTileEntities.COSMA_CONTROL_TILE_ENTITY.get().create();
    }




}
