package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RackTileEntity extends ParentTileEntity {

    public List<ServerType> serverTypes = new ArrayList<>();
    private boolean listInitialized = false;
    private BlockPos controllerPosition;

    public RackTileEntity(TileEntityType<?> type){super(type);}
    public RackTileEntity(){this(ModTileEntities.RACK_TILE_ENTITY.get());}


    @Override
    public void tick() {
        super.tick();
        //if (!listInitialized){createServerList();}

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

    public Boolean getListInitialized(){
        return listInitialized;
    }
    public void setListInitialized(Boolean bool){
        listInitialized = bool;
    }

    // the below two overrides handle updates from the server thread
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        //write data into nbtTag
        int[] serverTypeIntArray = new int[this.serverTypes.size()];
        for (int i=0; i<this.serverTypes.size(); i++){
            serverTypeIntArray[i] = serverTypes.get(i).getIndex();
        }
        nbtTag.putString("direction", parentDirection.name());
        nbtTag.putIntArray("serverTypes",serverTypeIntArray);
        nbtTag.putBoolean("listInitialized", this.listInitialized);
        nbtTag.putInt("controllerXPos", this.controllerPosition.getX());
        nbtTag.putInt("controllerYPos", this.controllerPosition.getY());
        nbtTag.putInt("controllerZPos", this.controllerPosition.getZ());

        System.out.println("Sending update tag");

        return new SUpdateTileEntityPacket(getBlockPos(), -1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbtTag = pkt.getTag();
        System.out.println("Received update tag");

        int[] serverTypeIntArray = nbtTag.getIntArray("serverTypes");
        this.listInitialized = nbtTag.getBoolean("listInitialized");
        this.controllerPosition = new BlockPos(
                nbtTag.getInt("controllerXPos"),
                nbtTag.getInt("controllerYPos"),
                nbtTag.getInt("controllerZPos"));
        for (int i=0; i<serverTypeIntArray.length; i++){
            this.serverTypes.add(ServerType.getTypeFromIndex(serverTypeIntArray[i]));
        }
        this.parentDirection = getDirectionFromString(nbtTag.getString("direction"));



    }

    private Direction getDirectionFromString(String string){
        switch (string){
            case "south":
                return Direction.SOUTH;
            case "west":
                return Direction.WEST;
            case "east":
                return Direction.EAST;
            default:
                return Direction.NORTH;
        }
    }

    public void onUse(double yHit){
        double rackBlockY = getBlockPos().getY();
        System.out.println(yHit-rackBlockY);




    }
}
