package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.init.ModBlocks;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import static net.fwojtan.cosmacraft.common.block.ParentBlock.FACING;

public class ParentTileEntity extends TileEntity implements ITickableTileEntity {

    private boolean childrenPlaced = false;
    public Direction parentDirection;

    public ParentTileEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    public ParentTileEntity() {

        this(ModTileEntities.PARENT_TILE_ENTITY.get());

    }

    @Override
    public void tick() {
        if (!childrenPlaced && getBlockState() != null){placeChildren();childrenPlaced=true;}


    }

    private void placeChildren() {
        BlockState state = getBlockState();
        parentDirection = state.getValue(FACING);

        if (!getLevel().isClientSide()) {
            getLevel().setBlock(getChildPosition(0, 0, 1), ModBlocks.DUMMY_BLOCK.get().defaultBlockState(), 3);
            getLevel().setBlock(getChildPosition(1, 0, 1), Blocks.STONE.defaultBlockState(), 3);
            getLevel().setBlock(getChildPosition(-1, 0, 1), Blocks.SAND.defaultBlockState(), 3);
        }
    }

    // TO-DO: Fix the rotation-aware placement of child blocks

    private BlockPos getChildPosition(int x, int y, int z){
        Vector3i translation;

        // north is -ve z direction
        // south is +ve z direction
        // west is -ve x direction
        // east is +ve x direction

        System.out.println(parentDirection);
        System.out.println(getBlockState());

        switch (parentDirection){
            case SOUTH:
                System.out.println("We went south");
                translation = new Vector3i(-x, y, -z); break;
            case EAST:
                System.out.println("We went east");
                translation = new Vector3i(z, y, -x); break;
            case WEST:
                System.out.println("We went west");
                translation = new Vector3i(-z, y, x); break;
            default:
                System.out.println("We went north");
                translation = new Vector3i(x, y, z); break;
        }

        return getBlockPos().offset(translation);
    }

}
