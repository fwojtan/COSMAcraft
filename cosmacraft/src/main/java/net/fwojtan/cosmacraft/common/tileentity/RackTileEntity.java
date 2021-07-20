package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.common.utils.DoorType;
import net.fwojtan.cosmacraft.common.utils.ServerState;
import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    public List<ServerState> serverStates = new ArrayList<>();
    private boolean listInitialized = false;
    private BlockPos controllerPosition;
    public DoorType doorType;
    public int doorOpen;
    public int doorOpenProgress;
    private int loopCounter = 0;

    public RackTileEntity(TileEntityType<?> type){super(type);}
    public RackTileEntity(){this(ModTileEntities.RACK_TILE_ENTITY.get());}


    @Override
    public void tick() {
        super.tick();

        if (loopCounter == 40){
            updateStateList();
        }

        if (loopCounter > 1241){
            loopCounter = 41;
            // update with latest state data here?
            updateStateList();
        }
        loopCounter++;

    }

    public void updateStateList(){
        CosmaControlTileEntity controller = (CosmaControlTileEntity) this.level.getBlockEntity(this.controllerPosition);
        if (controller.latestStateData != null) {
            getLevel().sendBlockUpdated(controllerPosition, getLevel().getBlockState(controllerPosition), getLevel().getBlockState(controllerPosition), 2);
            for (ServerState serverState : serverStates) {

                if (serverState.isComputeNode) {
                    for (int i = 0; i < serverState.serverName.size(); i++) {
                        String name = serverState.serverName.get(i);
                        serverState.updateTime.add(i, controller.latestStateData.get(name).updated);
                        serverState.jobName.add(i, controller.latestStateData.get(name).job);
                        serverState.jobDuration.add(i, controller.latestStateData.get(name).runtime);
                        serverState.status.add(i, controller.latestStateData.get(name).state);
                        serverState.cpuUsage.add(i, String.valueOf(controller.latestStateData.get(name).cpu));
                        serverState.memUsage.add(i, String.valueOf(controller.latestStateData.get(name).mem));
                    }

                }

            }
            System.out.println("Attempted to update state list");
        } else {System.out.println("Skipped statelist update due to null data");}
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
        int[] serverStateIntArray = new int[this.serverTypes.size()];
        for (int i=0; i<this.serverTypes.size(); i++){
            serverTypeIntArray[i] = serverTypes.get(i).getIndex();
            serverStateIntArray[i] = serverStates.get(i).ejected;

            nbtTag.putString("stateServerName"+i, serverStates.get(i).combineNodeNames());
            nbtTag.putBoolean("stateIsComputeNode"+i, serverStates.get(i).isComputeNode);

        }
        nbtTag.putString("direction", parentDirection.name());
        nbtTag.putIntArray("serverTypes",serverTypeIntArray);
        nbtTag.putIntArray("serverStatesEjected", serverStateIntArray);
        nbtTag.putBoolean("listInitialized", this.listInitialized);
        nbtTag.putInt("controllerXPos", this.controllerPosition.getX());
        nbtTag.putInt("controllerYPos", this.controllerPosition.getY());
        nbtTag.putInt("controllerZPos", this.controllerPosition.getZ());
        nbtTag.putInt("doorInfo", this.doorType.getIndex());
        nbtTag.putInt("doorOpen", this.doorOpen);






        //System.out.println("Sending update tag");

        return new SUpdateTileEntityPacket(getBlockPos(), -1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbtTag = pkt.getTag();
        //System.out.println("Received update tag");

        int[] serverTypeIntArray = nbtTag.getIntArray("serverTypes");
        int [] serverStateIntArray = nbtTag.getIntArray("serverStatesEjected");
        this.listInitialized = nbtTag.getBoolean("listInitialized");
        this.controllerPosition = new BlockPos(
                nbtTag.getInt("controllerXPos"),
                nbtTag.getInt("controllerYPos"),
                nbtTag.getInt("controllerZPos"));
        this.serverTypes = new ArrayList<>();
        this.serverStates = new ArrayList<>();
        for (int i=0; i<serverTypeIntArray.length; i++){
            this.serverTypes.add(ServerType.getTypeFromIndex(serverTypeIntArray[i]));

            ServerState state = new ServerState(nbtTag.getString("stateServerName"+i), serverStateIntArray[i],
                    nbtTag.getBoolean("stateIsComputeNode"+i));

            this.serverStates.add(state);

        }
        CosmaControlTileEntity controller = (CosmaControlTileEntity) getLevel().getBlockEntity(controllerPosition);
        if (controller.latestStateData != null){ updateStateList();}
        this.parentDirection = getDirectionFromString(nbtTag.getString("direction"));
        this.doorType = DoorType.getTypeFromIndex(nbtTag.getInt("doorInfo"));
        this.doorOpen = nbtTag.getInt("doorOpen");



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

    public void onUse(double yHit, double yAngle, ItemStack item, boolean shiftKeyDown, PlayerEntity player){

        double serverOffset = 0.15;

        // I think (?) the line below should correct the parallax error in the Y
        double serverHeight = yHit-getBlockPos().getY() - serverOffset * Math.tan(Math.acos(yAngle) - Math.PI / 2);
        int serverHitIndex = getServerListIndexFromY(serverHeight);

        if (item.sameItem(Items.STICK.getDefaultInstance()) && serverHitIndex>0) {
            this.serverStates.get(serverHitIndex).ejected = 1;
            System.out.println("Ejecting server");
        }

        else if (item.sameItem(Items.WOODEN_SWORD.getDefaultInstance())){
            this.doorOpen = 1;
        }

        else if (shiftKeyDown) {
            System.out.println("Triggered server de-eject");
            for (ServerState serverState : serverStates) {
                serverState.ejected = 0;
            }
            doorOpen = 0;

        }
        else {
            if (level.isClientSide()) {
                this.serverStates.get(serverHitIndex).printStateToPlayer(player, this.serverTypes.get(serverHitIndex));
            }
        }

    }





    private int getServerListIndexFromY(double y){
        if (y > 2.667) return -1;
        double uHeight = 42 * y / 2.667;
        int serverU = 0;
        for (int i=0; i<serverTypes.size(); i++){
            serverU += serverTypes.get(i).getUHeight();
            if (serverU > uHeight) return i;
        }
        return 0;
    }

    public void destroyChildren(){
        System.out.println(this.childPositionList);
        System.out.println(this.childPositionList.get(0));
    }

    public void createFreshStateList(){
        for (ServerType serverType : serverTypes){
            this.serverStates.add(new ServerState("a", 0,false));
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbtTag){
        super.load(state, nbtTag);
        int[] serverTypeIntArray = nbtTag.getIntArray("COSMAServerTypes");
        int [] serverStateIntArray = nbtTag.getIntArray("COSMAServerStatesEjected");
        this.listInitialized = nbtTag.getBoolean("COSMAListInitialized");
        this.controllerPosition = new BlockPos(
                nbtTag.getInt("COSMAControllerXPos"),
                nbtTag.getInt("COSMAControllerYPos"),
                nbtTag.getInt("COSMAControllerZPos"));
        this.serverTypes = new ArrayList<>();
        this.serverStates = new ArrayList<>();
        for (int i=0; i<serverTypeIntArray.length; i++){
            this.serverTypes.add(ServerType.getTypeFromIndex(serverTypeIntArray[i]));
            //this.serverStates.get(i).ejected = serverStateIntArray[i];

            ServerState serverState = new ServerState(nbtTag.getString("stateServerName"+i), serverStateIntArray[i],
                    nbtTag.getBoolean("stateIsComputeNode"+i));

            this.serverStates.add(serverState);
        }
        //updateStateList();
        this.parentDirection = getDirectionFromString(nbtTag.getString("COSMADirection"));
        this.doorType = DoorType.getTypeFromIndex(nbtTag.getInt("COSMADoorInfo"));
        this.doorOpen = nbtTag.getInt("COSMADoorOpen");
        System.out.println("Loading door type as "+this.doorType.getSerializedName());

    }

    @Override
    public CompoundNBT save(CompoundNBT nbtTag) {
        super.save(nbtTag);
        int[] serverTypeIntArray = new int[this.serverTypes.size()];
        int[] serverStateIntArray = new int[this.serverTypes.size()];
        for (int i=0; i<this.serverTypes.size(); i++){
            serverTypeIntArray[i] = serverTypes.get(i).getIndex();
            serverStateIntArray[i] = serverStates.get(i).ejected;

            nbtTag.putString("stateServerName"+i, serverStates.get(i).combineNodeNames());
            nbtTag.putBoolean("stateIsComputeNode"+i, serverStates.get(i).isComputeNode);
        }
        nbtTag.putString("COSMADirection", parentDirection.name());
        nbtTag.putIntArray("COSMAServerTypes",serverTypeIntArray);
        nbtTag.putIntArray("COSMAServerStatesEjected", serverStateIntArray);
        nbtTag.putBoolean("COSMAListInitialized", this.listInitialized);
        nbtTag.putInt("COSMAControllerXPos", this.controllerPosition.getX());
        nbtTag.putInt("COSMAControllerYPos", this.controllerPosition.getY());
        nbtTag.putInt("COSMAControllerZPos", this.controllerPosition.getZ());
        nbtTag.putInt("COSMADoorInfo", this.doorType.getIndex());
        nbtTag.putInt("COSMADoorOpen", this.doorOpen);
        System.out.println("Saving door type as "+this.doorType.getSerializedName());
        return nbtTag;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTag = super.getUpdateTag();
        int[] serverTypeIntArray = new int[this.serverTypes.size()];
        int[] serverStateIntArray = new int[this.serverTypes.size()];
        for (int i=0; i<this.serverTypes.size(); i++){
            serverTypeIntArray[i] = serverTypes.get(i).getIndex();
            serverStateIntArray[i] = serverStates.get(i).ejected;

            nbtTag.putString("stateServerName"+i, serverStates.get(i).combineNodeNames());
            nbtTag.putBoolean("stateIsComputeNode"+i, serverStates.get(i).isComputeNode);

        }
        nbtTag.putString("COSMADirection", parentDirection.name());
        nbtTag.putIntArray("COSMAServerTypes",serverTypeIntArray);
        nbtTag.putIntArray("COSMAServerStatesEjected", serverStateIntArray);
        nbtTag.putBoolean("COSMAListInitialized", this.listInitialized);
        nbtTag.putInt("COSMAControllerXPos", this.controllerPosition.getX());
        nbtTag.putInt("COSMAControllerYPos", this.controllerPosition.getY());
        nbtTag.putInt("COSMAControllerZPos", this.controllerPosition.getZ());
        nbtTag.putInt("COSMADoorInfo", this.doorType.getIndex());
        nbtTag.putInt("COSMADoorOpen", this.doorOpen);
        System.out.println("Syncing door type as "+this.doorType.getSerializedName());
        return nbtTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbtTag) {
        super.handleUpdateTag(state, nbtTag);
        int[] serverTypeIntArray = nbtTag.getIntArray("COSMAServerTypes");
        int [] serverStateIntArray = nbtTag.getIntArray("COSMAServerStatesEjected");
        this.listInitialized = nbtTag.getBoolean("COSMAListInitialized");
        this.controllerPosition = new BlockPos(
                nbtTag.getInt("COSMAControllerXPos"),
                nbtTag.getInt("COSMAControllerYPos"),
                nbtTag.getInt("COSMAControllerZPos"));
        this.serverTypes = new ArrayList<>();
        this.serverStates = new ArrayList<>();
        for (int i=0; i<serverTypeIntArray.length; i++){
            this.serverTypes.add(ServerType.getTypeFromIndex(serverTypeIntArray[i]));
            //this.serverStates.get(i).ejected = serverStateIntArray[i];

            ServerState serverState = new ServerState(nbtTag.getString("stateServerName"+i), serverStateIntArray[i],
                    nbtTag.getBoolean("stateIsComputeNode"+i));

            this.serverStates.add(serverState);
        }
        updateStateList();
        this.parentDirection = getDirectionFromString(nbtTag.getString("COSMADirection"));
        this.doorType = DoorType.getTypeFromIndex(nbtTag.getInt("COSMADoorInfo"));
        this.doorOpen = nbtTag.getInt("COSMADoorOpen");
        System.out.println("Syncing door type as "+this.doorType.getSerializedName());
    }
}
