package net.fwojtan.cosmacraft.common.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class DummyBlock extends Block {

    public DummyBlock() {
        super(AbstractBlock.Properties.of(Material.METAL, MaterialColor.COLOR_BLACK).strength(15f).sound(SoundType.METAL));
    }


}
