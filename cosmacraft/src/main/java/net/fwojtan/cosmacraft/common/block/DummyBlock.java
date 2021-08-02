package net.fwojtan.cosmacraft.common.block;

import net.fwojtan.cosmacraft.common.tileentity.ChildTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.RackTileEntity;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class DummyBlock extends Block {

    public DummyBlock() {
        super(AbstractBlock.Properties.of(Material.AIR, MaterialColor.COLOR_BLACK).strength(15f).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState p_200011_1_, IBlockReader p_200011_2_, BlockPos p_200011_3_) {
        return 100;
    }



    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ModTileEntities().CHILD_TILE_ENTITY.get().create();
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        double yHit = rayTraceResult.getLocation().y;
        double yAngle = player.getLookAngle().y;
        TileEntity tileEntity = world.getBlockEntity(pos);
        if (world.getBlockEntity(pos) != null) {
            if (tileEntity instanceof ChildTileEntity){
                TileEntity parentEntity = world.getBlockEntity(((ChildTileEntity) tileEntity).parentPosition);
                if (parentEntity instanceof RackTileEntity){
                    ((RackTileEntity) parentEntity).onUse(yHit, yAngle, player.getMainHandItem(), player.isShiftKeyDown(), player);
                }
            } else if (tileEntity instanceof RackTileEntity) {
                ((RackTileEntity) tileEntity).onUse(yHit, yAngle, player.getMainHandItem(), player.isShiftKeyDown(), player);
            }



        }



        return ActionResultType.SUCCESS;
    }
}
