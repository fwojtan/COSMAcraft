package net.fwojtan.cosmacraft.common.block;

import com.ibm.icu.util.CodePointTrie;
import net.fwojtan.cosmacraft.common.tileentity.*;
import net.fwojtan.cosmacraft.init.ModItems;
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
        super(AbstractBlock.Properties.of(Material.STRUCTURAL_AIR, MaterialColor.NONE).strength(15f).sound(SoundType.METAL).noOcclusion());
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
                tileEntity = world.getBlockEntity(((ChildTileEntity) tileEntity).parentPosition);

            }

            if (tileEntity instanceof RackTileEntity) {
                ((RackTileEntity) tileEntity).onUse(yHit, yAngle, player.getMainHandItem(), player.isShiftKeyDown(), player);
            } else if (tileEntity instanceof EmptyRackTileEntity){
                if (player.getMainHandItem().sameItem(ModItems.INFO_TOOL.get().getDefaultInstance()) && tileEntity.getLevel().isClientSide()) {
                    ((EmptyRackTileEntity) tileEntity).sendInfo(player);
                }
            } else if (tileEntity instanceof LargePowerTileEntity){
                if (player.getMainHandItem().sameItem(ModItems.INFO_TOOL.get().getDefaultInstance()) && tileEntity.getLevel().isClientSide()) {
                    ((LargePowerTileEntity) tileEntity).sendInfo(player);
                }
            } else if (tileEntity instanceof SmallPowerTileEntity){
                if (player.getMainHandItem().sameItem(ModItems.INFO_TOOL.get().getDefaultInstance()) && tileEntity.getLevel().isClientSide()) {
                    ((SmallPowerTileEntity) tileEntity).sendInfo(player);
                }
            } else if (tileEntity instanceof ExchangerTileEntity){
                if (player.getMainHandItem().sameItem(ModItems.INFO_TOOL.get().getDefaultInstance()) && tileEntity.getLevel().isClientSide()) {
                    ((ExchangerTileEntity) tileEntity).sendInfo(player);
                }
            } else if (tileEntity instanceof ArchiveTileEntity){
                if (player.getMainHandItem().sameItem(ModItems.INFO_TOOL.get().getDefaultInstance()) && tileEntity.getLevel().isClientSide()) {
                    ((ArchiveTileEntity) tileEntity).sendInfo(player);
                }
            } else if (tileEntity instanceof PipeTileEntity){
                if (player.getMainHandItem().sameItem(ModItems.INFO_TOOL.get().getDefaultInstance()) && tileEntity.getLevel().isClientSide()) {
                    ((PipeTileEntity) tileEntity).sendInfo(player);
                }
            }



        }



        return ActionResultType.SUCCESS;
    }
}
