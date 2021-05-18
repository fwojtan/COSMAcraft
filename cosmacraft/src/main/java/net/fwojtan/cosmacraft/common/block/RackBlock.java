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

public class RackBlock extends ParentBlock{

    public static final Property<Boolean> SHOULD_RENDER = BooleanProperty.create("should_render");

    public RackBlock() {super();}

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHOULD_RENDER);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(SHOULD_RENDER, false);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ModTileEntities().RACK_TILE_ENTITY.get().create();
    }
}
