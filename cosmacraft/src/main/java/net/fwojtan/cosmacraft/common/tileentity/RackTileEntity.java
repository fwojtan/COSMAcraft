package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class RackTileEntity extends ParentTileEntity {

    public static List<ServerType> serverTypes;
    private boolean listInitialized = false;
    private BlockPos controllerPosition;

    public RackTileEntity(TileEntityType<?> type){super(type);}
    public RackTileEntity(){this(ModTileEntities.RACK_TILE_ENTITY.get());}

    @Override
    public void tick() {
        super.tick();
        if (!listInitialized){createServerList();}

    }

    private void createServerList(){
        serverTypes = new ArrayList<ServerType>();
        for (int i=0; i<3; i++){
            serverTypes.add(ServerType.TWO_U_HEX);
        }
        for (int i=0; i<3; i++){
            serverTypes.add(ServerType.R740_HEX_FLAT);
        }

        for (int i=0; i<2; i++){
            serverTypes.add(ServerType.ONE_U_HORIZONTAL_DRIVES);
        }

        serverTypes.add(ServerType.MELLANOX_EDR);
        serverTypes.add(ServerType.TWO_U_HEX);

        for (int i=0; i<5; i++){
            serverTypes.add(ServerType.C_6525);
        }

        serverTypes.add(ServerType.ME_484);
        serverTypes.add(ServerType.MD_3420);

        listInitialized = true;
    }

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
                return new Vector3d(0d, 1d, -0.5d);
            case EAST:
                return new Vector3d(-0.5d, 1d, 0d);

            case WEST:
                return new Vector3d(0.5d, 1d, 0d);
            default:
                return new Vector3d(0d, 1d, 0.5d);
        }
    }

    private Vector3d getBBInflate(){
        switch (parentDirection) {

            case EAST:
            case WEST:
                return new Vector3d(0.5d, 1d, 0d);
            default:
                return new Vector3d(0d, 1d, 0.5d);
        }
    }

    public void setControllerPosition(BlockPos controllerPosition) {
        this.controllerPosition = controllerPosition;
    }

    public BlockPos getControllerPosition(){
        return this.controllerPosition;
    }
}
