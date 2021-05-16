package net.fwojtan.cosmacraft.common.block;

import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.common.utils.ServerTypeProperty;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.*;


import javax.annotation.Nullable;


public class ServerBlock extends Block {

    public static final  EnumProperty<ServerType> RENDER_CHOICE=  ServerTypeProperty.create("server_type", ServerType.class);
    public static final DirectionProperty THiNG = DirectionProperty.create("test_dir");

    public ServerBlock() {
        super(AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(15f).sound(SoundType.METAL).noOcclusion());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(RENDER_CHOICE, ServerType.NONE);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RENDER_CHOICE);

    }

}
