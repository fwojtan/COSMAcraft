package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class EmptyRackTileEntity extends ParentTileEntity{

    public EmptyRackTileEntity(TileEntityType<?> type){super(type);}
    public EmptyRackTileEntity(){this(ModTileEntities.EMPTY_RACK_TILE_ENTITY.get());}

    @Override
    public List<BlockPos> createChildPositonList() {
        List<Vector3i> offsetList = new ArrayList<Vector3i>();
        List<BlockPos> retList = new ArrayList<BlockPos>();
        offsetList.add(new Vector3i(0, 0, 1));
        offsetList.add(new Vector3i(0, 1, 1));
        offsetList.add(new Vector3i(0, 2, 1));
        offsetList.add(new Vector3i(0, 1, 0));
        offsetList.add(new Vector3i(0, 2, 0));
        for (Vector3i vec : offsetList){
            retList.add(getChildPosition(vec));
        }
        return retList;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (parentDirection != null) {
            Vector3d inflation = getBBInflate();
            return super.getRenderBoundingBox().inflate(inflation.x, inflation.y, inflation.z).move(getBBOffset());
        } else {return super.getRenderBoundingBox();}
    }

    private Vector3d getBBOffset(){
        switch (parentDirection) {
            case SOUTH:
                return new Vector3d(-0.5d, 0.5d, 0d);
            case EAST:
                return new Vector3d(0d, 0.5d, -0.5d);

            case WEST:
                return new Vector3d(0d, 0.5d, 0.5d);
            default:
                return new Vector3d(0.5d, 0.5d, 0d);
        }
    }

    private Vector3d getBBInflate(){
        switch (parentDirection) {

            case EAST:
            case WEST:
                return new Vector3d(0d, 0.5d, 0.5d);
            default:
                return new Vector3d(0.5d, 0.5d, 0d);
        }
    }
}
