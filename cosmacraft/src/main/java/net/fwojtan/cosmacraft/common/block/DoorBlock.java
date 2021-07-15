package net.fwojtan.cosmacraft.common.block;

import net.fwojtan.cosmacraft.common.utils.DoorType;
import net.fwojtan.cosmacraft.common.utils.DoorTypeProperty;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;

import javax.annotation.Nullable;

public class DoorBlock extends Block {

    public static final EnumProperty<DoorType> RENDER_CHOICE = DoorTypeProperty.create("door_type", DoorType.class);

    public DoorBlock() {
        super(AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(15f).sound(SoundType.METAL).noOcclusion());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(RENDER_CHOICE, DoorType.NONE);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RENDER_CHOICE);
    }

}
