package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModBlocks;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;

import static net.fwojtan.cosmacraft.common.block.ParentBlock.FACING;

public class ParentTileEntity extends TileEntity implements ITickableTileEntity {

    public boolean childrenPlaced = false;
    public Direction parentDirection;
    private List<BlockPos> childPositionList;


    public ParentTileEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    public ParentTileEntity() {this(ModTileEntities.PARENT_TILE_ENTITY.get()); }

    @Override
    public void tick() {
        if (!childrenPlaced && getBlockState() != null){placeChildren();childrenPlaced=true;}
    }

    public void placeChildren() {
        BlockState state = getBlockState();
        parentDirection = state.getValue(FACING);

        childPositionList = createChildPositonList();

        if (!getLevel().isClientSide()) {
            for (BlockPos childPosition : childPositionList) {
                getLevel().setBlock(childPosition, ModBlocks.DUMMY_BLOCK.get().defaultBlockState(), 3);
                ChildTileEntity entity = (ChildTileEntity) getLevel().getBlockEntity(childPosition);
                entity.parentPosition = getBlockPos();
            }
        }
    }

    public BlockPos getChildPosition(Vector3i vec){
        int x = vec.getX();
        int y = vec.getY();
        int z = vec.getZ();
        Vector3i translation;

        // north is -ve z direction
        // south is +ve z direction
        // west is -ve x direction
        // east is +ve x direction

        switch (parentDirection){
            case SOUTH:
                translation = new Vector3i(-x, y, -z); break;
            case EAST:
                translation = new Vector3i(-z, y, x); break;
            case WEST:
                translation = new Vector3i(z, y, -x); break;
            default:
                translation = new Vector3i(x, y, z); break;
        }

        return getBlockPos().offset(translation);
    }


    public List<BlockPos> createChildPositonList() {
        List<Vector3i> offsetList = new ArrayList<Vector3i>();
        List<BlockPos> retList = new ArrayList<BlockPos>();
        // add more children at the desired relative coordinates here when overriding
        offsetList.add(new Vector3i(0, 0, 1));
        for (Vector3i vec : offsetList){
           retList.add(getChildPosition(vec));
        }
        return retList;
    }

    public List<BlockPos> getChildPositionList() {
        return this.childPositionList;
    }



}
