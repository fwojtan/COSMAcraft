package net.fwojtan.cosmacraft.common.tileentity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.common.utils.*;
import net.fwojtan.cosmacraft.init.ModBlocks;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.stats.Stat;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.*;

import static net.fwojtan.cosmacraft.common.block.ParentBlock.FACING;
import static net.fwojtan.cosmacraft.common.block.RackBlock.SHOULD_RENDER;

public class CosmaControlTileEntity extends ParentTileEntity{

    public CosmaControlTileEntity(TileEntityType<?> type){super(type);}
    public CosmaControlTileEntity(){this(ModTileEntities.COSMA_CONTROL_TILE_ENTITY.get());}

    private boolean waiting = false;
    private boolean firstTime = true;
    private Instant startTime = Instant.now();
    public Map<String, StateData> latestStateData;
    private int loopCounter = 0;
    private final int cosmaPort = 5432;
    private Instant lastReceived = startTime.minus(1, ChronoUnit.HOURS);
    public boolean displayDataOverlay = false;
    public boolean displayCables = true;
    //public boolean collectTimingData = true;
    //public List<List<Long>> timingData = new ArrayList<>();

    @Override
    public void tick() {
        /*
        //timing data collection code for benchmarking

        if (collectTimingData && timingData.size() == 1000){
            collectTimingData = false;
            Path configLocation = FMLPaths.getOrCreateGameRelativePath(Paths.get("util/cosma_config/"), "cosma_config_path");
            File file = new File(configLocation.toString() + "/timing_data.json");
            try {
                FileWriter writer = new FileWriter(file.getPath());
                writer.write(new Gson().toJson(timingData));
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Saved timing data");
            for (PlayerEntity player : getLevel().players()){
                ITextComponent message = new TranslationTextComponent("Saved timing data");
                player.sendMessage(message, new UUID(16, 0));
            }
        }
        */


        Instant timeNow = Instant.now();
        long timeElapsed = Duration.between(startTime, timeNow).toMinutes();
        if (timeElapsed > 1) {waiting = false;}


        if (!waiting) {
            startTime = Instant.now();
            waiting = true;

            if (firstTime){
                firstTime=false;
                verifyConnection();
                fetchLatestConfigData();
                ServerType.FOUR_U_GAP.printTypes();
            }

            // Do stuff here
            fetchLatestStateData();
            loadLatestStateData();



        }

        if(!childrenPlaced){

            placeChildren();

            if (!getLevel().isClientSide()) {
                childrenPlaced = true;
            }

            getLevel().sendBlockUpdated(getBlockPos(), getLevel().getBlockState(getBlockPos()), getLevel().getBlockState(getBlockPos()), 2);
        }

        if (loopCounter == 40){
            getLevel().sendBlockUpdated(getBlockPos(), getLevel().getBlockState(getBlockPos()), getLevel().getBlockState(getBlockPos()), 2);
            loopCounter++;
        } else if (loopCounter < 41){
            loopCounter++;
        }




    }

    private void loadLatestStateData(){
        if (!getLevel().isClientSide()){
            Map<String, StateData> stateDataMap = null;
            Type stateDataType = new TypeToken<HashMap<String, StateData>>(){}.getType();
            Path configLocation = FMLPaths.getOrCreateGameRelativePath(Paths.get("util/cosma_config/"), "cosma_config_path");

            try {
                File file = new File(configLocation.toString() + "/cosma_usage_latest.json");

                // read json
                Reader reader = Files.newBufferedReader(Paths.get(file.getPath()));
                stateDataMap = new Gson().fromJson(reader, stateDataType);
                reader.close();

                StateData testData = stateDataMap.get("m7031");
                System.out.println(testData.id);

            } catch (IOException e){
                e.printStackTrace();
            }
            latestStateData =  stateDataMap;

            System.out.println("Updated state data to latest version for controller on server thread");

        }
    }

    private void fetchLatestStateData(){
        if (!getLevel().isClientSide()) {
            Path configLocation = FMLPaths.getOrCreateGameRelativePath(Paths.get("util/cosma_config/"), "cosma_config_path");

            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT ).withLocale( Locale.UK ).withZone( ZoneId.systemDefault() );
            String lastReceivedString = formatter.format(lastReceived);
            System.out.println("Fetching latest usage data from COSMA on port "+cosmaPort+"... (was last sent at "+lastReceivedString+")");

            try {
                Socket socket = new Socket("localhost", cosmaPort);
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());


                dataOut.writeUTF("MCServer requesting state data &"+lastReceivedString);
                dataOut.flush();

                String response = readInputStream(dataIn);
                if (response.length() > 100) {
                    File file = new File(configLocation.toString() + "/cosma_usage_latest.json");
                    FileWriter writer = new FileWriter(file.getPath());
                    writer.write(response);
                    writer.close();
                    System.out.println("Wrote latest state data to file!");
                    lastReceived = Instant.now();
                } else {
                    System.out.println("Received response: "+response);
                }
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchLatestConfigData(){
        if (!getLevel().isClientSide()) {
            Path configLocation = FMLPaths.getOrCreateGameRelativePath(Paths.get("util/cosma_config/"), "cosma_config_path");
            System.out.println("Fetching latest config data from COSMA on port "+cosmaPort+"...");

            try {
                Socket socket = new Socket("localhost", cosmaPort);
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

                dataOut.writeUTF("MCServer requesting config data");
                dataOut.flush();

                File file = new File(configLocation.toString() + "/cosma_config.json");
                FileWriter writer = new FileWriter(file.getPath());
                writer.write(readInputStream(dataIn));
                writer.close();
                System.out.println("Wrote latest config data to file!");
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void verifyConnection(){
        if (!getLevel().isClientSide()) {

            System.out.println("Checking connection to COSMA on port "+cosmaPort+"...");

            try {
                Socket socket = new Socket("localhost", cosmaPort);
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

                dataOut.writeUTF("MCServer testing connection");
                dataOut.flush();

                String response = readInputStream(dataIn);

                System.out.println("Connection verified! (Response: "+response+")");
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String readInputStream(DataInputStream dataIn){
        StringBuilder result = new StringBuilder();

        while (true) {
            byte[] b = new byte[1024];
            int readStatus = 0;
            try {
                readStatus = dataIn.read(b, 0, 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
            result.append((new String(b, StandardCharsets.UTF_8)).replace("\0", ""));
            if (readStatus == -1) {
                break;
            }
        }
        return result.toString();
    }



    @Override
    public void placeChildren() {

        childPositionList = new ArrayList<>();

        parentDirection = getBlockState().getValue(FACING);
        List<RackConfig> rackConfigList;
        Type rackConfigType = new TypeToken<ArrayList<RackConfig>>(){}.getType();
        Path configLocation = FMLPaths.getOrCreateGameRelativePath(Paths.get("util/cosma_config/"), "cosma_config_path");
        if (!this.level.isClientSide()) {
            try {
                File file = new File(configLocation.toString() + "/cosma_config.json");
                File file2 = new File(configLocation.toString() + "/cosma_usage_latest.json");

                // generate jsons in the right location if it doesn't exist
                if (file.createNewFile()) {
                    System.out.println("Created: " + file.getName() + " at " + file.getPath());
                    FileWriter writer = new FileWriter(file.getPath());
                    writer.write(createDefaultConfigJson());
                    writer.close();
                }
                if (file2.createNewFile()) {
                    System.out.println("Created: " + file2.getName() + " at " + file2.getPath());
                    FileWriter writer = new FileWriter(file2.getPath());
                    writer.write(createDefaultUsageJson());
                    writer.close();

                }else {
                    System.out.println("File already exists");

                    // read json
                    Reader reader = Files.newBufferedReader(Paths.get(file.getPath()));
                    rackConfigList = new Gson().fromJson(reader, rackConfigType);
                    reader.close();

                    for (RackConfig rackConfig : rackConfigList) {
                        Vector3i offset = new Vector3i(2, 1, 3);

                        BlockPos childPos = getChildPosition(new Vector3i(rackConfig.x, rackConfig.y, rackConfig.z));
                        childPos = childPos.offset(offset);
                        BlockState childState = ModBlocks.RACK_BLOCK.get().defaultBlockState().setValue(FACING, Direction.valueOf(rackConfig.facing))
                                .setValue(SHOULD_RENDER, false);

                        // we only create these things on the server side

                        // place racks in required positions
                        getLevel().setBlock(childPos, childState, 3);

                        // feed the tile entity its required data
                        RackTileEntity rackTileEntity = (RackTileEntity) getLevel().getBlockEntity(childPos);
                        rackTileEntity.setControllerPosition(getBlockPos());
                        rackTileEntity.serverTypes = this.createServerList(rackConfig.servers);
                        rackTileEntity.parentDirection = childState.getValue(FACING);
                        rackTileEntity.doorType = DoorType.valueOf(rackConfig.backDoor);
                        int j = 0;
                        for (int i = 0; i < rackConfig.servers.size(); i++) {
                            String name = rackConfig.names.get(j);
                            if (rackConfig.servers.get(i).contains("GAP")) {
                                rackTileEntity.serverStates.add(new ServerState("Empty space", 0, false));
                            } else {
                                if (this.latestStateData.containsKey(name) || name.contains("m7")) {
                                    rackTileEntity.serverStates.add(new ServerState(name, 0, true));
                                } else {
                                    rackTileEntity.serverStates.add(new ServerState(name, 0, false));
                                }
                                j++;
                            }
                        }
                        rackTileEntity.updateStateList();
                        System.out.println("Placed Rack and Initialized for Rack " + rackConfig.id);
                        System.out.println("StateList size:" + rackTileEntity.serverStates.size());
                        System.out.println("TypeList size:" + rackTileEntity.serverTypes.size());
                        //rackTileEntity.createFreshStateList();
                        rackTileEntity.setListInitialized(true);


                        // and then after they're created we mark them for an update on the render thread
                        getLevel().sendBlockUpdated(childPos, childState, childState, 2);
                        childPositionList.add(childPos);
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        if (latestStateData != null) {
            int i = 0;
            for (String key : latestStateData.keySet()) {
                StateData data = latestStateData.get(key);

                nbtTag.putFloat("dataCpu" + i, data.cpu);
                nbtTag.putFloat("dataMem" + i, data.mem);
                nbtTag.putString("id" + i, data.id);
                nbtTag.putString("dataJob" + i, data.job);
                nbtTag.putString("dataState" + i, data.state);
                nbtTag.putString("dataRuntime" + i, data.runtime);
                nbtTag.putString("dataUpdated" + i, data.updated);

                i++;
            }
            nbtTag.putInt("numberOfEntries", i);
            nbtTag.putBoolean("childrenPlaced", childrenPlaced);
            nbtTag.putBoolean("displayData", displayDataOverlay);
        }
        return new SUpdateTileEntityPacket(getBlockPos(), -1, nbtTag);

    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbtTag = pkt.getTag();

        latestStateData = new HashMap<>();

        int number = nbtTag.getInt("numberOfEntries");
        childrenPlaced = nbtTag.getBoolean("childrenPlaced");
        displayDataOverlay = nbtTag.getBoolean("displayData");
        for (int i=0; i<number+1; i++){
            StateData state = new StateData();
            state.id = nbtTag.getString("id"+i);
            state.cpu = nbtTag.getFloat("dataCpu"+i);
            state.mem = nbtTag.getFloat("dataMem"+i);
            state.state = nbtTag.getString("dataState"+i);
            state.job = nbtTag.getString("dataJob"+i);
            state.runtime = nbtTag.getString("dataRuntime"+i);
            state.updated = nbtTag.getString("dataUpdated"+i);

            latestStateData.put(state.id, state);
        }

        System.out.println("Completed packet update");

    }


    private List<ServerType> createServerList(List<String> servers){
        List<ServerType> retList = new ArrayList<>();
        for (String server : servers){
            retList.add(ServerType.valueOf(server));
        }
        return retList;
    }


    @Override
    public List<BlockPos> createChildPositonList() {

        return super.createChildPositonList();
    }
    @Override
    public void load(BlockState state, CompoundNBT nbtTag){
        super.load(state, nbtTag);
        latestStateData = new HashMap<>();

        int number = nbtTag.getInt("numberOfEntries");
        childrenPlaced = nbtTag.getBoolean("childrenPlaced");
        for (int i=0; i<number+1; i++){
            StateData stateData = new StateData();
            stateData.id = nbtTag.getString("id"+i);
            stateData.cpu = nbtTag.getFloat("dataCpu"+i);
            stateData.mem = nbtTag.getFloat("dataMem"+i);
            stateData.state = nbtTag.getString("dataState"+i);
            stateData.job = nbtTag.getString("dataJob"+i);
            stateData.runtime = nbtTag.getString("dataRuntime"+i);
            stateData.updated = nbtTag.getString("dataUpdated"+i);

            latestStateData.put(stateData.id, stateData);
        }

    }

    @Override
    public CompoundNBT save(CompoundNBT nbtTag) {
        super.save(nbtTag);
        int i=0;
        for (String key : latestStateData.keySet()){
            StateData data = latestStateData.get(key);

            nbtTag.putFloat("dataCpu"+i, data.cpu);
            nbtTag.putFloat("dataMem"+i, data.mem);
            nbtTag.putString("id"+i, data.id);
            nbtTag.putString("dataJob"+i, data.job);
            nbtTag.putString("dataState"+i, data.state);
            nbtTag.putString("dataRuntime"+i, data.runtime);
            nbtTag.putString("dataUpdated"+i, data.updated);

            i++;
        }
        nbtTag.putInt("numberOfEntries", i);
        nbtTag.putBoolean("childrenPlaced", childrenPlaced);
        return nbtTag;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbtTag = super.getUpdateTag();
        int i=0;
        for (String key : latestStateData.keySet()){
            StateData data = latestStateData.get(key);

            nbtTag.putFloat("dataCpu"+i, data.cpu);
            nbtTag.putFloat("dataMem"+i, data.mem);
            nbtTag.putString("id"+i, data.id);
            nbtTag.putString("dataJob"+i, data.job);
            nbtTag.putString("dataState"+i, data.state);
            nbtTag.putString("dataRuntime"+i, data.runtime);
            nbtTag.putString("dataUpdated"+i, data.updated);

            i++;
        }
        nbtTag.putInt("numberOfEntries", i);
        nbtTag.putBoolean("childrenPlaced", childrenPlaced);
        return nbtTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbtTag) {
        super.handleUpdateTag(state, nbtTag);
        latestStateData = new HashMap<>();

        int number = nbtTag.getInt("numberOfEntries");
        childrenPlaced = nbtTag.getBoolean("childrenPlaced");
        for (int i=0; i<number+1; i++){
            StateData stateData = new StateData();
            stateData.id = nbtTag.getString("id"+i);
            stateData.cpu = nbtTag.getFloat("dataCpu"+i);
            stateData.mem = nbtTag.getFloat("dataMem"+i);
            stateData.state = nbtTag.getString("dataState"+i);
            stateData.job = nbtTag.getString("dataJob"+i);
            stateData.runtime = nbtTag.getString("dataRuntime"+i);
            stateData.updated = nbtTag.getString("dataUpdated"+i);

            latestStateData.put(stateData.id, stateData);
        }
    }

    public boolean getChildrenPlaced(){
        return childrenPlaced;
    }



    // there is presumably a better way than hardcoding this config file but I can't figure out how to load a file from a resource location for the life of me
    private String createDefaultConfigJson(){
        return "[\n" +
                "\t{\n" +
                "\t\t\"id\": \"A7\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 1,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MAD_03\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7328/m7327/m7325/m7326\",\n" +
                "\t\t\t\"m7332/m7331/m7329/m7330\",\n" +
                "\t\t\t\"m7336/m7335/m7333/m7334\",\n" +
                "\t\t\t\"m7340/m7339/m7337/m7338\",\n" +
                "\t\t\t\"m7344/m7343/m7341/m7342\",\n" +
                "\t\t\t\"m7348/m7347/m7345/m7346\",\n" +
                "\t\t\t\"c7sw13\",\n" +
                "\t\t\t\"c7ibesw17\",\n" +
                "\t\t\t\"m7352/m7351/m7349/m7350\",\n" +
                "\t\t\t\"m7356/m7355/m7353/m7354\",\n" +
                "\t\t\t\"m7360/m7359/m7357/m7358\",\n" +
                "\t\t\t\"m7364/m7363/m7361/m7362\",\n" +
                "\t\t\t\"m7368/m7367/m7365/m7366\",\n" +
                "\t\t\t\"m7372/m7371/m7369/m7370\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"c7ibesw18\",\n" +
                "\t\t\t\"m7376/m7375/m7373/m7374\",\n" +
                "\t\t\t\"m7380/m7379/m7377/m7378\",\n" +
                "\t\t\t\"m7384/m7383/m7381/m7382\",\n" +
                "\t\t\t\"m7388/m7387/m7385/m7386\",\n" +
                "\t\t\t\"mad03\",\n" +
                "\t\t\t\"c7sw14\",\n" +
                "\t\t\t\"c7ibesw19\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"A8\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 2,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MAD_03\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7392/m7391/m7389/m7390\",\n" +
                "\t\t\t\"m7396/m7395/m7393/m7394\",\n" +
                "\t\t\t\"m7400/m7399/m7397/m7398\",\n" +
                "\t\t\t\"m7404/m7403/m7401/m7402\",\n" +
                "\t\t\t\"m7408/m7407/m7405/m7406\",\n" +
                "\t\t\t\"m7412/m7411/m7409/m7410\",\n" +
                "\t\t\t\"c7sw15\",\n" +
                "\t\t\t\"c7ibesw20\",\n" +
                "\t\t\t\"m7416/m7415/m7413/m7414\",\n" +
                "\t\t\t\"m7420/m7419/m7417/m7418\",\n" +
                "\t\t\t\"m7424/m7423/m7421/m7422\",\n" +
                "\t\t\t\"m7428/m7427/m7425/m7426\",\n" +
                "\t\t\t\"m7432/m7431/m7429/m7430\",\n" +
                "\t\t\t\"m7436/m7435/m7433/m7434\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"c7ibesw21\",\n" +
                "\t\t\t\"m7440/m7439/m7437/m7438\",\n" +
                "\t\t\t\"m7444/m7443/m7441/m7442\",\n" +
                "\t\t\t\"m7448/m7447/m7445/m7446\",\n" +
                "\t\t\t\"m7452/m7451/m7449/m7450\",\n" +
                "\t\t\t\"daos\",\n" +
                "\t\t\t\"c7sw16\",\n" +
                "\t\t\t\"c7ibesw22\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"A1\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 3,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7004/m7003/m7001/m7002\",\n" +
                "\t\t\t\"m7008/m7007/m7005/m7006\",\n" +
                "\t\t\t\"m7012/m7011/m7009/m7010\",\n" +
                "\t\t\t\"m7016/m7015/m7013/m7014\",\n" +
                "\t\t\t\"m7020/m7019/m7017/m7018\",\n" +
                "\t\t\t\"m7024/m7023/m7021/m7022\",\n" +
                "\t\t\t\"c7sw01\",\n" +
                "\t\t\t\"c7ibesw01\",\n" +
                "\t\t\t\"m7028/m7027/m7025/m7026\",\n" +
                "\t\t\t\"m7032/m7031/m7029/m7030\",\n" +
                "\t\t\t\"m7036/m7035/m7033/m7034\",\n" +
                "\t\t\t\"m7040/m7039/m7037/m7038\",\n" +
                "\t\t\t\"m7044/m7043/m7041/m7042\",\n" +
                "\t\t\t\"m7048/m7047/m7045/m7046\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"c7ibesw02\",\n" +
                "\t\t\t\"m7052/m7051/m7049/m7050\",\n" +
                "\t\t\t\"m7056/m7055/m7053/m7054\",\n" +
                "\t\t\t\"m7060/m7059/m7057/m7058\",\n" +
                "\t\t\t\"m7064/m7063/m7061/m7062\",\n" +
                "\t\t\t\"login7a\",\n" +
                "\t\t\t\"c7sw02\",\n" +
                "\t\t\t\"c7ibesw03\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"A2\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 4,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"HANDLEBAR_2U\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ME_484_2U\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"LCDKVM\",\n" +
                "\t\t\t\"DOUBLE_SQUARES\",\n" +
                "\t\t\t\"HANDLEBAR_2U\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7132/m7131/m7129/m7130\",\n" +
                "\t\t\t\"m7136/m7135/m7133/m7134\",\n" +
                "\t\t\t\"m7140/m7139/m7137/m7138\",\n" +
                "\t\t\t\"m7144/m7143/m7141/m7142\",\n" +
                "\t\t\t\"m7148/m7147/m7145/m7146\",\n" +
                "\t\t\t\"dellmds1\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"c7sw05\",\n" +
                "\t\t\t\"c7ibesw07\",\n" +
                "\t\t\t\"headary1\",\n" +
                "\t\t\t\"console71\",\n" +
                "\t\t\t\"console72\",\n" +
                "\t\t\t\"dellmds2\",\n" +
                "\t\t\t\"mdsary1\",\n" +
                "\t\t\t\"1U Rackmount LCD KVM\",\n" +
                "\t\t\t\"cosma-tt\",\n" +
                "\t\t\t\"mdsbackupary1\",\n" +
                "\t\t\t\"c7ibcsw12\",\n" +
                "\t\t\t\"c7ibcsw11\",\n" +
                "\t\t\t\"c7ibcsw10\",\n" +
                "\t\t\t\"c7ibcsw09\",\n" +
                "\t\t\t\"c7ibcsw08\",\n" +
                "\t\t\t\"c7ibcsw07\",\n" +
                "\t\t\t\"c7ibcsw04\",\n" +
                "\t\t\t\"c7ibcsw05\",\n" +
                "\t\t\t\"c7ibcsw06\",\n" +
                "\t\t\t\"c7ibcsw01\",\n" +
                "\t\t\t\"c7ibcsw02\",\n" +
                "\t\t\t\"c7ibcsw03\",\n" +
                "\t\t\t\"c7topSwitch\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"A3\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 5,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7068/m7067/m7065/m7066\",\n" +
                "\t\t\t\"m7072/m7071/m7069/m7070\",\n" +
                "\t\t\t\"m7076/m7075/m7073/m7074\",\n" +
                "\t\t\t\"m7080/m7079/m7077/m7078\",\n" +
                "\t\t\t\"m7084/m7083/m7081/m7082\",\n" +
                "\t\t\t\"m7088/m7087/m7085/m7086\",\n" +
                "\t\t\t\"c7sw03\",\n" +
                "\t\t\t\"c7ibesw04\",\n" +
                "\t\t\t\"m7092/m7091/m7089/m7090\",\n" +
                "\t\t\t\"m7096/m7095/m7093/m7094\",\n" +
                "\t\t\t\"m7100/m7099/m7097/m7098\",\n" +
                "\t\t\t\"m7104/m7103/m7101/m7102\",\n" +
                "\t\t\t\"m7108/m7107/m7105/m7106\",\n" +
                "\t\t\t\"m7112/m7111/m7109/m7110\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"c7ibesw05\",\n" +
                "\t\t\t\"login7c\",\n" +
                "\t\t\t\"m7116/m7115/m7113/m7114\",\n" +
                "\t\t\t\"m7120/m7119/m7117/m7118\",\n" +
                "\t\t\t\"m7124/m7123/m7121/m7122\",\n" +
                "\t\t\t\"m7128/m7127/m7125/m7126\",\n" +
                "\t\t\t\"login7b\",\n" +
                "\t\t\t\"c7sw04\",\n" +
                "\t\t\t\"c7ibesw06\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"A4\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 6,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"ATEMPO\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C7_FIBRE_SWITCH\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7152/m7151/m7149/m7150\",\n" +
                "\t\t\t\"m7156/m7155/m7153/m7154\",\n" +
                "\t\t\t\"m7160/m7159/m7157/m7158\",\n" +
                "\t\t\t\"m7164/m7163/m7161/m7162\",\n" +
                "\t\t\t\"m7168/m7167/m7165/m7166\",\n" +
                "\t\t\t\"m7172/m7171/m7169/m7170\",\n" +
                "\t\t\t\"c7sw07\",\n" +
                "\t\t\t\"c7ibesw09\",\n" +
                "\t\t\t\"m7176/m7175/m7173/m7174\",\n" +
                "\t\t\t\"m7180/m7179/m7177/m7178\",\n" +
                "\t\t\t\"m7184/m7183/m7181/m7182\",\n" +
                "\t\t\t\"m7188/m7187/m7185/m7186\",\n" +
                "\t\t\t\"m7192/m7191/m7189/m7190\",\n" +
                "\t\t\t\"m7196/m7195/m7193/m7194\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"c7ibesw10\",\n" +
                "\t\t\t\"atempo2\",\n" +
                "\t\t\t\"m7200/m7199/m7197/m7198\",\n" +
                "\t\t\t\"m7204/m7203/m7201/m7202\",\n" +
                "\t\t\t\"m7208/m7207/m7205/m7206\",\n" +
                "\t\t\t\"m7212/m7211/m7209/m7210\",\n" +
                "\t\t\t\"c7fibresw2\",\n" +
                "\t\t\t\"c7sw08\",\n" +
                "\t\t\t\"c7ibesw11\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"A5\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 7,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"ATEMPO\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C7_FIBRE_SWITCH\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7216/m7215/m7213/m7214\",\n" +
                "\t\t\t\"m7220/m7219/m7217/m7218\",\n" +
                "\t\t\t\"m7224/m7223/m7221/m7222\",\n" +
                "\t\t\t\"m7228/m7227/m7225/m7226\",\n" +
                "\t\t\t\"m7232/m7231/m7229/m7230\",\n" +
                "\t\t\t\"m7236/m7235/m7233/m7234\",\n" +
                "\t\t\t\"c7sw09\",\n" +
                "\t\t\t\"c7ibesw12\",\n" +
                "\t\t\t\"m7240/m7239/m7237/m7238\",\n" +
                "\t\t\t\"m7244/m7243/m7241/m7242\",\n" +
                "\t\t\t\"m7248/m7247/m7245/m7246\",\n" +
                "\t\t\t\"m7252/m7251/m7249/m7250\",\n" +
                "\t\t\t\"m7256/m7255/m7253/m7254\",\n" +
                "\t\t\t\"m7260/m7259/m7257/m7258\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"c7ibesw13\",\n" +
                "\t\t\t\"atempo1\",\n" +
                "\t\t\t\"m7264/m7263/m7261/m7262\",\n" +
                "\t\t\t\"m7268/m7267/m7265/m7266\",\n" +
                "\t\t\t\"m7272/m7271/m7269/m7270\",\n" +
                "\t\t\t\"m7276/m7275/m7273/m7274\",\n" +
                "\t\t\t\"c7fibresw1\",\n" +
                "\t\t\t\"c7sw10\",\n" +
                "\t\t\t\"c7ibesw14\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"A6\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 8,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"DELL_PSU\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m7280/m7279/m7277/m7278\",\n" +
                "\t\t\t\"m7284/m7283/m7281/m7282\",\n" +
                "\t\t\t\"m7288/m7287/m7285/m7286\",\n" +
                "\t\t\t\"m7292/m7291/m7289/m7290\",\n" +
                "\t\t\t\"m7296/m7295/m7293/m7294\",\n" +
                "\t\t\t\"m7300/m7299/m7297/m7298\",\n" +
                "\t\t\t\"c7sw12\",\n" +
                "\t\t\t\"c7ibesw15\",\n" +
                "\t\t\t\"m7304/m7303/m7301/m7302\",\n" +
                "\t\t\t\"m7308/m7307/m7305/m7306\",\n" +
                "\t\t\t\"m7312/m7311/m7309/m7310\",\n" +
                "\t\t\t\"m7316/m7315/m7313/m7314\",\n" +
                "\t\t\t\"m7320/m7319/m7317/m7318\",\n" +
                "\t\t\t\"m7324/m7323/m7321/m7322\",\n" +
                "\t\t\t\"Dell N-series redundant PSU\",\n" +
                "\t\t\t\"bluefield15/bluefield13/bluefield14/bluefield16\",\n" +
                "\t\t\t\"bluefield11/bluefield9/bluefield10/bluefield12\",\n" +
                "\t\t\t\"bluefield7/bluefield5/bluefield6/bluefield8\",\n" +
                "\t\t\t\"bluefield3/bluefield1/bluefield2/bluefield4\",\n" +
                "\t\t\t\"bluefieldswitch\",\n" +
                "\t\t\t\"Aruba (COSMA top switch)\",\n" +
                "\t\t\t\"c7sw11\",\n" +
                "\t\t\t\"c7ibesw16\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"MESH\",\n" +
                "\t\t\"backDoor\": \"C7_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA7\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"E1\",\n" +
                "\t\t\"x\": 9,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 1,\n" +
                "\t\t\"facing\": \"WEST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"FOUR_U_GAP\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"HANDLEBAR_2U\",\n" +
                "\t\t\t\"HANDLEBAR_2U\",\n" +
                "\t\t\t\"ONE_U_HEX\",\n" +
                "\t\t\t\"ONE_U_HEX\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"ETH_SWITCH\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"Disk Expansion space\",\n" +
                "\t\t\t\"ossary1\",\n" +
                "\t\t\t\"delloss01\",\n" +
                "\t\t\t\"delloss02\",\n" +
                "\t\t\t\"delloss03\",\n" +
                "\t\t\t\"delloss04\",\n" +
                "\t\t\t\"delloss20\",\n" +
                "\t\t\t\"Disk Expansion space\",\n" +
                "\t\t\t\"Disk Expansion space\",\n" +
                "\t\t\t\"ossary2\",\n" +
                "\t\t\t\"delloss05\",\n" +
                "\t\t\t\"delloss06\",\n" +
                "\t\t\t\"delloss07\",\n" +
                "\t\t\t\"delloss08\",\n" +
                "\t\t\t\"nfsary1-2\",\n" +
                "\t\t\t\"nfsary1-1\",\n" +
                "\t\t\t\"dellnfs1\",\n" +
                "\t\t\t\"dellnfs2\",\n" +
                "\t\t\t\"c7sw06\",\n" +
                "\t\t\t\"c7ibesw23\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"STORAGE_BACK\",\n" +
                "\t\t\"rackStyle\": \"STORAGE\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"E2\",\n" +
                "\t\t\"x\": 9,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 2,\n" +
                "\t\t\"facing\": \"WEST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"MD_3420\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"THREE_U_HEX\",\n" +
                "\t\t\t\"THREE_U_HEX\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"MELLANOX_EDR\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"Expansion space\",\n" +
                "\t\t\t\"Expansion space\",\n" +
                "\t\t\t\"ossary3\",\n" +
                "\t\t\t\"delloss09\",\n" +
                "\t\t\t\"delloss10\",\n" +
                "\t\t\t\"delloss11\",\n" +
                "\t\t\t\"delloss12\",\n" +
                "\t\t\t\"delloss19\",\n" +
                "\t\t\t\"Expansion space\",\n" +
                "\t\t\t\"Expansion space\",\n" +
                "\t\t\t\"ossary4\",\n" +
                "\t\t\t\"delloss13\",\n" +
                "\t\t\t\"delloss14\",\n" +
                "\t\t\t\"delloss15\",\n" +
                "\t\t\t\"delloss16\",\n" +
                "\t\t\t\"mad1\",\n" +
                "\t\t\t\"mad2\",\n" +
                "\t\t\t\"delloss17\",\n" +
                "\t\t\t\"delloss18\",\n" +
                "\t\t\t\"c7ibesw08\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"STORAGE_BACK\",\n" +
                "\t\t\"rackStyle\": \"STORAGE\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H5a\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 10,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [],\n" +
                "\t\t\"names\": [],\n" +
                "\t\t\"frontDoor\": \"FLAT_MESH\",\n" +
                "\t\t\"backDoor\": \"C6_BACK\",\n" +
                "\t\t\"rackStyle\": \"STORAGE\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H5\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 11,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"ME_484\",\n" +
                "\t\t\t\"ME_484\",\n" +
                "\t\t\t\"ME_484\",\n" +
                "\t\t\t\"ME_484\",\n" +
                "\t\t\t\"ME_484\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"TWO_U_HEX\",\n" +
                "\t\t\t\"TWO_U_HEX\",\n" +
                "\t\t\t\"TWO_U_HEX\",\n" +
                "\t\t\t\"TWO_U_HEX\",\n" +
                "\t\t\t\"ME_484_2U\",\n" +
                "\t\t\t\"C8_SWITCH\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"c8ossary05\",\n" +
                "\t\t\t\"c8ossary04\",\n" +
                "\t\t\t\"c8ossary03\",\n" +
                "\t\t\t\"c8ossary02\",\n" +
                "\t\t\t\"c8ossary01\",\n" +
                "\t\t\t\"c8oss05\",\n" +
                "\t\t\t\"c8oss04\",\n" +
                "\t\t\t\"c8oss03\",\n" +
                "\t\t\t\"c8oss02\",\n" +
                "\t\t\t\"c8oss01\",\n" +
                "\t\t\t\"IB switch: c8ibe23\",\n" +
                "\t\t\t\"c8mds4\",\n" +
                "\t\t\t\"c8mds3\",\n" +
                "\t\t\t\"c8mds2\",\n" +
                "\t\t\t\"c8mds1\",\n" +
                "\t\t\t\"c8mdsbackup\",\n" +
                "\t\t\t\"10G: c8sw13\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H4\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 12,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"TWO_U_HEX\",\n" +
                "\t\t\t\"FOUR_U_MI50\",\n" +
                "\t\t\t\"TWO_U_GA_SERVER\",\n" +
                "\t\t\t\"FOUR_U_CIRCLES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"ONE_U_HORIZONTAL_DRIVES\",\n" +
                "\t\t\t\"C8_SWITCH\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"ga004 Milan MI100\",\n" +
                "\t\t\t\"ga003 AMD MI50 node\",\n" +
                "\t\t\t\"ga002\",\n" +
                "\t\t\t\"ga001\",\n" +
                "\t\t\t\"gn001\",\n" +
                "\t\t\t\"snap8mds1\",\n" +
                "\t\t\t\"IB switch: c8ibe22\",\n" +
                "\t\t\t\"snap8oss24\",\n" +
                "\t\t\t\"snap8oss23\",\n" +
                "\t\t\t\"snap8oss22\",\n" +
                "\t\t\t\"snap8oss21\",\n" +
                "\t\t\t\"snap8oss20\",\n" +
                "\t\t\t\"snap8oss19\",\n" +
                "\t\t\t\"snap8oss18\",\n" +
                "\t\t\t\"snap8oss17\",\n" +
                "\t\t\t\"snap8oss16\",\n" +
                "\t\t\t\"snap8oss15\",\n" +
                "\t\t\t\"snap8oss14\",\n" +
                "\t\t\t\"snap8oss13\",\n" +
                "\t\t\t\"IBd switch: c8ibe21\",\n" +
                "\t\t\t\"snap8oss12\",\n" +
                "\t\t\t\"snap8oss11\",\n" +
                "\t\t\t\"snap8oss10\",\n" +
                "\t\t\t\"snap8oss09\",\n" +
                "\t\t\t\"snap8oss08\",\n" +
                "\t\t\t\"snap8oss07\",\n" +
                "\t\t\t\"snap8oss06\",\n" +
                "\t\t\t\"snap8oss05\",\n" +
                "\t\t\t\"snap8oss04\",\n" +
                "\t\t\t\"snap8oss03\",\n" +
                "\t\t\t\"snap8oss02\",\n" +
                "\t\t\t\"snap8oss01\",\n" +
                "\t\t\t\"10G: c8sw12\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H3\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 13,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m8215/m8213/m8214/m8216\",\n" +
                "\t\t\t\"m8211/m8209/m8210/m8212\",\n" +
                "\t\t\t\"IB switch: c8ibe12\",\n" +
                "\t\t\t\"m8207/m8205/m8206/m8208\",\n" +
                "\t\t\t\"m8203/m8201/m8202/m8204\",\n" +
                "\t\t\t\"m8199/m8197/m8198/m8200\",\n" +
                "\t\t\t\"c8sw07\",\n" +
                "\t\t\t\"m8195/m8193/m8194/m8196\",\n" +
                "\t\t\t\"m8191/m8189/m8190/m8192\",\n" +
                "\t\t\t\"IB switch: c8ibe11\",\n" +
                "\t\t\t\"m8187/m8185/m8186/m8188\",\n" +
                "\t\t\t\"m8183/m8181/m8182/m8184\",\n" +
                "\t\t\t\"m8179/m8177/m8178/m8180\",\n" +
                "\t\t\t\"m8175/m8173/m8174/m8176\",\n" +
                "\t\t\t\"IB switch: c8ibe10\",\n" +
                "\t\t\t\"m8171/m8169/m8170/m8172\",\n" +
                "\t\t\t\"m8167/m8165/m8166/m8168\",\n" +
                "\t\t\t\"c8sw06\",\n" +
                "\t\t\t\"m8163/m8161/m8162/m8164\",\n" +
                "\t\t\t\"m8159/m8157/m8158/m8160\",\n" +
                "\t\t\t\"m8155/m8153/m8154/m8156\",\n" +
                "\t\t\t\"IB switch: c8ibe09\",\n" +
                "\t\t\t\"m8151/m8149/m8150/m8152\",\n" +
                "\t\t\t\"m8147/m8145/m8146/m8148\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H2\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 14,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m8143/m8141/m8142/m8144\",\n" +
                "\t\t\t\"m8139/m8137/m8138/m8140\",\n" +
                "\t\t\t\"IB switch: c8ibe08\",\n" +
                "\t\t\t\"m8135/m8133/m8134/m8136\",\n" +
                "\t\t\t\"m8131/m8129/m8130/m8132\",\n" +
                "\t\t\t\"m8127/m8125/m8126/m8128\",\n" +
                "\t\t\t\"c8sw05\",\n" +
                "\t\t\t\"m8123/m8121/m8122/m8124\",\n" +
                "\t\t\t\"m8119/m8117/m8118/m8120\",\n" +
                "\t\t\t\"IB switch: c8ibe07\",\n" +
                "\t\t\t\"m8115/m8113/m8114/m8116\",\n" +
                "\t\t\t\"m8111/m8109/m8110/m8112\",\n" +
                "\t\t\t\"m8107/m8105/m8106/m8108\",\n" +
                "\t\t\t\"m8103/m8101/m8102/m8104\",\n" +
                "\t\t\t\"IB switch: c8ibe06\",\n" +
                "\t\t\t\"m8099/m8097/m8098/m8100\",\n" +
                "\t\t\t\"m8095/m8093/m8094/m8096\",\n" +
                "\t\t\t\"c8sw04\",\n" +
                "\t\t\t\"m8091/m8089/m8090/m8092\",\n" +
                "\t\t\t\"m8087/m8085/m8086/m8088\",\n" +
                "\t\t\t\"m8083/m8081/m8082/m8084\",\n" +
                "\t\t\t\"IB switch: c8ibe05\",\n" +
                "\t\t\t\"m8079/m8077/m8078/m8080\",\n" +
                "\t\t\t\"m8075/m8073/m8074/m8076\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H1\",\n" +
                "\t\t\"x\": 6,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 15,\n" +
                "\t\t\"facing\": \"EAST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m8071/m8069/m8070/m8072\",\n" +
                "\t\t\t\"m8067/m8065/m8066/m8068\",\n" +
                "\t\t\t\"IB switch: c8ibe04\",\n" +
                "\t\t\t\"m8063/m8061/m8062/m8064\",\n" +
                "\t\t\t\"m8059/m8057/m8058/m8060\",\n" +
                "\t\t\t\"m8055/m8053/m8054/m8056\",\n" +
                "\t\t\t\"c8sw03\",\n" +
                "\t\t\t\"m8051/m8049/m8050/m8052\",\n" +
                "\t\t\t\"m8047/m8045/m8046/m8048\",\n" +
                "\t\t\t\"IB switch: c8ibe03\",\n" +
                "\t\t\t\"m8043/m8041/m8042/m8044\",\n" +
                "\t\t\t\"m8039/m8037/m8038/m8040\",\n" +
                "\t\t\t\"m8035/m8033/m8034/m8036\",\n" +
                "\t\t\t\"m8031/m8029/m8030/m8032\",\n" +
                "\t\t\t\"IB switch: c8ibe02\",\n" +
                "\t\t\t\"m8027/m8025/m8026/m8028\",\n" +
                "\t\t\t\"m8023/m8021/m8022/m8024\",\n" +
                "\t\t\t\"c8sw02\",\n" +
                "\t\t\t\"m8019/m8017/m8018/m8020\",\n" +
                "\t\t\t\"m8015/m8013/m8014/m8016\",\n" +
                "\t\t\t\"m8011/m8009/m8010/m8012\",\n" +
                "\t\t\t\"IB switch: c8ibe01\",\n" +
                "\t\t\t\"m8007/m8005/m8006/m8008\",\n" +
                "\t\t\t\"m8003/m8001/m8002/m8004\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H6\",\n" +
                "\t\t\"x\": 1,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 15,\n" +
                "\t\t\"facing\": \"WEST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m8287/m8285/m8286/m8288\",\n" +
                "\t\t\t\"m8283/m8281/m8282/m8284\",\n" +
                "\t\t\t\"IB switch: c8ibe16\",\n" +
                "\t\t\t\"m8279/m8277/m8278/m8280\",\n" +
                "\t\t\t\"m8275/m8273/m8274/m8276\",\n" +
                "\t\t\t\"m8271/m8269/m8270/m8272\",\n" +
                "\t\t\t\"c8sw09\",\n" +
                "\t\t\t\"m8267/m8265/m8266/m8268\",\n" +
                "\t\t\t\"m8263/m8261/m8262/m8264\",\n" +
                "\t\t\t\"IB switch: c8ibe15\",\n" +
                "\t\t\t\"m8259/m8257/m8258/m8260\",\n" +
                "\t\t\t\"m8255/m8253/m8254/m8256\",\n" +
                "\t\t\t\"m8251/m8249/m8250/m8252\",\n" +
                "\t\t\t\"m8247/m8245/m8246/m8248\",\n" +
                "\t\t\t\"IB switch: c8ibe14\",\n" +
                "\t\t\t\"m8243/m8241/m8242/m8244\",\n" +
                "\t\t\t\"m8239/m8237/m8238/m8240\",\n" +
                "\t\t\t\"c8sw08\",\n" +
                "\t\t\t\"m8235/m8233/m8234/m8236\",\n" +
                "\t\t\t\"m8231/m8229/m8230/m8232\",\n" +
                "\t\t\t\"m8227/m8225/m8226/m8228\",\n" +
                "\t\t\t\"IB switch: c8ibe13\",\n" +
                "\t\t\t\"m8223/m8221/m8222/m8224\",\n" +
                "\t\t\t\"m8219/m8217/m8218/m8220\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H7\",\n" +
                "\t\t\"x\": 1,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 14,\n" +
                "\t\t\"facing\": \"WEST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m8359/m8357/m8358/m8360\",\n" +
                "\t\t\t\"m8355/m8353/m8354/m8356\",\n" +
                "\t\t\t\"IB switch: c8ibe20\",\n" +
                "\t\t\t\"m8351/m8349/m8350/m8352\",\n" +
                "\t\t\t\"m8347/m8345/m8346/m8348\",\n" +
                "\t\t\t\"m8343/m8341/m8342/m8344\",\n" +
                "\t\t\t\"c8sw11\",\n" +
                "\t\t\t\"m8339/m8337/m8338/m8340\",\n" +
                "\t\t\t\"m8335/m8333/m8334/m8336\",\n" +
                "\t\t\t\"IB switch: c8ibe19\",\n" +
                "\t\t\t\"m8331/m8329/m8330/m8332\",\n" +
                "\t\t\t\"m8327/m8325/m8326/m8328\",\n" +
                "\t\t\t\"m8323/m8321/m8322/m8324\",\n" +
                "\t\t\t\"m8319/m8317/m8318/m8320\",\n" +
                "\t\t\t\"IB switch: c8ibe18\",\n" +
                "\t\t\t\"m8315/m8313/m8314/m8316\",\n" +
                "\t\t\t\"m8311/m8309/m8310/m8312\",\n" +
                "\t\t\t\"c8sw10\",\n" +
                "\t\t\t\"m8307/m8305/m8306/m8308\",\n" +
                "\t\t\t\"m8303/m8301/m8302/m8304\",\n" +
                "\t\t\t\"m8299/m8297/m8298/m8300\",\n" +
                "\t\t\t\"IB switch: c8ibe17\",\n" +
                "\t\t\t\"m8295/m8293/m8294/m8296\",\n" +
                "\t\t\t\"m8291/m8289/m8290/m8292\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H8\",\n" +
                "\t\t\"x\": 1,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 13,\n" +
                "\t\t\"facing\": \"WEST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"FOUR_U_GAP\",\n" +
                "\t\t\t\"ONE_U_GAP\",\n" +
                "\t\t\t\"GREY_4U_SERVER\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"LOGIN_NODE_C8\",\n" +
                "\t\t\t\"TWO_U_HEX\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"LOGIN_NODE_C8\",\n" +
                "\t\t\t\"LOGIN_NODE_C8\",\n" +
                "\t\t\t\"R_6525\",\n" +
                "\t\t\t\"ME_484_2U\",\n" +
                "\t\t\t\"R_6525\",\n" +
                "\t\t\t\"LCDKVM\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C8_SWITCH\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"Mystery Server?\",\n" +
                "\t\t\t\"Ethernet switch: c8sw01\",\n" +
                "\t\t\t\"mad05\",\n" +
                "\t\t\t\"R7525 fat node (mad04)\",\n" +
                "\t\t\t\"IB switch: c8ibe24\",\n" +
                "\t\t\t\"login8b\",\n" +
                "\t\t\t\"login8a\",\n" +
                "\t\t\t\"R6525 console node\",\n" +
                "\t\t\t\"Shared console storage\",\n" +
                "\t\t\t\"R6525 console node\",\n" +
                "\t\t\t\"Rackmount monitor/key\",\n" +
                "\t\t\t\"IB switch: c8ibc20\",\n" +
                "\t\t\t\"IB switch: c8ibc19\",\n" +
                "\t\t\t\"IB switch: c8ibc18\",\n" +
                "\t\t\t\"IB switch: c8ibc17\",\n" +
                "\t\t\t\"IB switch: c8ibc16\",\n" +
                "\t\t\t\"IB switch: c8ibc15\",\n" +
                "\t\t\t\"IB switch: c8ibc14\",\n" +
                "\t\t\t\"IB switch: c8ibc13\",\n" +
                "\t\t\t\"IB switch: c8ibc12\",\n" +
                "\t\t\t\"IB switch: c8ibc11\",\n" +
                "\t\t\t\"IB switch: c8ibc10\",\n" +
                "\t\t\t\"IB switch: c8ibc09\",\n" +
                "\t\t\t\"IB switch: c8ibc08\",\n" +
                "\t\t\t\"IB switch: c8ibc07\",\n" +
                "\t\t\t\"IB switch: c8ibc06\",\n" +
                "\t\t\t\"IB switch: c8ibc05\",\n" +
                "\t\t\t\"IB switch: c8ibc04\",\n" +
                "\t\t\t\"IB switch: c8ibc03\",\n" +
                "\t\t\t\"IB switch: c8ibc02\",\n" +
                "\t\t\t\"IB switch: c8ibc01\",\n" +
                "\t\t\t\"C8 Top switch\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"H9\",\n" +
                "\t\t\"x\": 1,\n" +
                "\t\t\"y\": 0,\n" +
                "\t\t\"z\": 12,\n" +
                "\t\t\"facing\": \"WEST\",\n" +
                "\t\t\"servers\": [\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C8_SWITCH\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"MELLANOX_QUANT\",\n" +
                "\t\t\t\"C_6525\",\n" +
                "\t\t\t\"C_6525\"\n" +
                "\t\t],\n" +
                "\t\t\"names\": [\n" +
                "\t\t\t\"m8431/m8429/m8430/m8432\",\n" +
                "\t\t\t\"m8427/m8425/m8426/m8428\",\n" +
                "\t\t\t\"IB switch: c8ibe28\",\n" +
                "\t\t\t\"m8423/m8421/m8422/m8424\",\n" +
                "\t\t\t\"m8419/m8417/m8418/m8420\",\n" +
                "\t\t\t\"m8415/m8413/m8414/m8416\",\n" +
                "\t\t\t\"c8sw15\",\n" +
                "\t\t\t\"m8411/m8409/m8410/m8412\",\n" +
                "\t\t\t\"m8407/m8405/m8406/m8408\",\n" +
                "\t\t\t\"IB switch: c8ibe27\",\n" +
                "\t\t\t\"m8403/m8401/m8402/m8404\",\n" +
                "\t\t\t\"m8399/m8397/m8398/m8400\",\n" +
                "\t\t\t\"m8395/m8393/m8394/m8396\",\n" +
                "\t\t\t\"m8391/m8389/m8390/m8392\",\n" +
                "\t\t\t\"IB switch: c8ibe26\",\n" +
                "\t\t\t\"m8387/m8385/m8386/m8388\",\n" +
                "\t\t\t\"m8383/m8381/m8382/m8384\",\n" +
                "\t\t\t\"c8sw14\",\n" +
                "\t\t\t\"m8379/m8377/m8378/m8380\",\n" +
                "\t\t\t\"m8375/m8373/m8374/m8376\",\n" +
                "\t\t\t\"m8371/m8369/m8370/m8372\",\n" +
                "\t\t\t\"IB switch: c8ibe25\",\n" +
                "\t\t\t\"m8367/m8365/m8366/m8368\",\n" +
                "\t\t\t\"m8363/m8361/m8362/m8364\"\n" +
                "\t\t],\n" +
                "\t\t\"frontDoor\": \"NONE\",\n" +
                "\t\t\"backDoor\": \"C8_BACK\",\n" +
                "\t\t\"rackStyle\": \"COSMA8\"\n" +
                "\t}\n" +
                "]";
    }

    private String createDefaultUsageJson(){
        String stringpart1 = "{\"m7001\": {\"id\": \"m7001\", \"cpu\": 21.96, \"mem\": 21.6796875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:03\"}, \"m7002\": {\"id\": \"m7002\", \"cpu\": 35.04, \"mem\": 31.640625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:05\"}, \"m7003\": {\"id\": \"m7003\", \"cpu\": 35.32, \"mem\": 30.2734375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:07\"}, \"m7004\": {\"id\": \"m7004\", \"cpu\": 71.75, \"mem\": 45.5078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:08\"}, \"m7005\": {\"id\": \"m7005\", \"cpu\": 65.36, \"mem\": 15.8203125, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:10:02\", \"updated\": \"2021-07-13 12:00:10\"}, \"m7006\": {\"id\": \"m7006\", \"cpu\": 62.11, \"mem\": 15.625, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:10:04\", \"updated\": \"2021-07-13 12:00:11\"}, \"m7007\": {\"id\": \"m7007\", \"cpu\": 100.0, \"mem\": 37.6953125, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:31:14\", \"updated\": \"2021-07-13 12:00:13\"}, \"m7008\": {\"id\": \"m7008\", \"cpu\": 100.18, \"mem\": 37.6953125, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:31:16\", \"updated\": \"2021-07-13 12:00:15\"}, \"m7009\": {\"id\": \"m7009\", \"cpu\": 100.11, \"mem\": 18.9453125, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"31:23\", \"updated\": \"2021-07-13 12:00:16\"}, \"m7010\": {\"id\": \"m7010\", \"cpu\": 100.75, \"mem\": 10.7421875, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"31:25\", \"updated\": \"2021-07-13 12:00:18\"}, \"m7011\": {\"id\": \"m7011\", \"cpu\": 99.71, \"mem\": 21.875, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"31:27\", \"updated\": \"2021-07-13 12:00:20\"}, \"m7012\": {\"id\": \"m7012\", \"cpu\": 97.93, \"mem\": 13.0859375, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"31:29\", \"updated\": \"2021-07-13 12:00:21\"}, \"m7013\": {\"id\": \"m7013\", \"cpu\": 99.0, \"mem\": 13.28125, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"31:30\", \"updated\": \"2021-07-13 12:00:23\"}, \"m7014\": {\"id\": \"m7014\", \"cpu\": 100.39, \"mem\": 23.2421875, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"31:32\", \"updated\": \"2021-07-13 12:00:25\"}, \"m7015\": {\"id\": \"m7015\", \"cpu\": 100.0, \"mem\": 39.0625, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:14\", \"updated\": \"2021-07-13 12:00:26\"}, \"m7016\": {\"id\": \"m7016\", \"cpu\": 100.0, \"mem\": 37.5, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:16\", \"updated\": \"2021-07-13 12:00:28\"}, \"m7017\": {\"id\": \"m7017\", \"cpu\": 100.21, \"mem\": 37.6953125, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:18\", \"updated\": \"2021-07-13 12:00:30\"}, \"m7018\": {\"id\": \"m7018\", \"cpu\": 100.0, \"mem\": 37.5, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:19\", \"updated\": \"2021-07-13 12:00:31\"}, \"m7019\": {\"id\": \"m7019\", \"cpu\": 100.0, \"mem\": 37.3046875, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:21\", \"updated\": \"2021-07-13 12:00:33\"}, \"m7020\": {\"id\": \"m7020\", \"cpu\": 100.0, \"mem\": 37.5, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:23\", \"updated\": \"2021-07-13 12:00:35\"}, \"m7021\": {\"id\": \"m7021\", \"cpu\": 100.0, \"mem\": 37.5, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:24\", \"updated\": \"2021-07-13 12:00:36\"}, \"m7022\": {\"id\": \"m7022\", \"cpu\": 100.04, \"mem\": 37.3046875, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:29:26\", \"updated\": \"2021-07-13 12:00:38\"}, \"m7023\": {\"id\": \"m7023\", \"cpu\": 100.0, \"mem\": 6.25, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:46:34\", \"updated\": \"2021-07-13 12:00:40\"}, \"m7024\": {\"id\": \"m7024\", \"cpu\": 63.93, \"mem\": 16.015625, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:10:33\", \"updated\": \"2021-07-13 12:00:41\"}, \"m7025\": {\"id\": \"m7025\", \"cpu\": 51.57, \"mem\": 15.625, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:29:31\", \"updated\": \"2021-07-13 12:00:43\"}, \"m7026\": {\"id\": \"m7026\", \"cpu\": 50.68, \"mem\": 15.0390625, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:29:33\", \"updated\": \"2021-07-13 12:00:44\"}, \"m7027\": {\"id\": \"m7027\", \"cpu\": 100.0, \"mem\": 61.71875, \"job\": \"low_low_\", \"state\": \"idle\", \"runtime\": \"16:51:46\", \"updated\": \"2021-07-13 12:00:46\"}, \"m7028\": {\"id\": \"m7028\", \"cpu\": 100.0, \"mem\": 61.328125, \"job\": \"low_low_\", \"state\": \"idle\", \"runtime\": \"16:51:48\", \"updated\": \"2021-07-13 12:00:48\"}, \"m7029\": {\"id\": \"m7029\", \"cpu\": 21.68, \"mem\": 20.703125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:49\"}, \"m7030\": {\"id\": \"m7030\", \"cpu\": 29.82, \"mem\": 30.078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:51\"}, \"m7031\": {\"id\": \"m7031\", \"cpu\": 24.14, \"mem\": 26.3671875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:53\"}, \"m7032\": {\"id\": \"m7032\", \"cpu\": 44.54, \"mem\": 41.015625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:00:54\"}, \"m7033\": {\"id\": \"m7033\", \"cpu\": 100.57, \"mem\": 35.546875, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:36:34\", \"updated\": \"2021-07-13 12:00:56\"}, \"m7034\": {\"id\": \"m7034\", \"cpu\": 100.46, \"mem\": 35.3515625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:36:36\", \"updated\": \"2021-07-13 12:00:58\"}, \"m7035\": {\"id\": \"m7035\", \"cpu\": 100.89, \"mem\": 35.3515625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:36:37\", \"updated\": \"2021-07-13 12:00:59\"}, \"m7036\": {\"id\": \"m7036\", \"cpu\": 100.68, \"mem\": 35.3515625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:36:39\", \"updated\": \"2021-07-13 12:01:01\"}, \"m7037\": {\"id\": \"m7037\", \"cpu\": 100.0, \"mem\": 37.890625, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:32:04\", \"updated\": \"2021-07-13 12:01:02\"}, \"m7038\": {\"id\": \"m7038\", \"cpu\": 100.0, \"mem\": 37.890625, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:32:05\", \"updated\": \"2021-07-13 12:01:04\"}, \"m7039\": {\"id\": \"m7039\", \"cpu\": 0.0, \"mem\": 5.2734375, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"15:48\", \"updated\": \"2021-07-13 12:01:06\"}, \"m7040\": {\"id\": \"m7040\", \"cpu\": 0.21, \"mem\": 5.2734375, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"15:49\", \"updated\": \"2021-07-13 12:01:07\"}, \"m7041\": {\"id\": \"m7041\", \"cpu\": 92.86, \"mem\": 16.9921875, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:29:57\", \"updated\": \"2021-07-13 12:01:09\"}, \"m7042\": {\"id\": \"m7042\", \"cpu\": 92.86, \"mem\": 15.4296875, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:29:59\", \"updated\": \"2021-07-13 12:01:11\"}, \"m7043\": {\"id\": \"m7043\", \"cpu\": 63.29, \"mem\": 15.8203125, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:11:04\", \"updated\": \"2021-07-13 12:01:12\"}, \"m7044\": {\"id\": \"m7044\", \"cpu\": 100.0, \"mem\": 37.6953125, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:32:15\", \"updated\": \"2021-07-13 12:01:14\"}, \"m7045\": {\"id\": \"m7045\", \"cpu\": 99.75, \"mem\": 37.6953125, \"job\": \"MHD_10mg\", \"state\": \"idle\", \"runtime\": \"17:30:04\", \"updated\": \"2021-07-13 12:01:16\"}, \"m7046\": {\"id\": \"m7046\", \"cpu\": 100.0, \"mem\": 37.890625, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:32:18\", \"updated\": \"2021-07-13 12:01:17\"}, \"m7047\": {\"id\": \"m7047\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:32:20\", \"updated\": \"2021-07-13 12:01:19\"}, \"m7048\": {\"id\": \"m7048\", \"cpu\": 92.89, \"mem\": 15.625, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:09\", \"updated\": \"2021-07-13 12:01:21\"}, \"m7049\": {\"id\": \"m7049\", \"cpu\": 92.86, \"mem\": 15.0390625, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:10\", \"updated\": \"2021-07-13 12:01:22\"}, \"m7050\": {\"id\": \"m7050\", \"cpu\": 92.86, \"mem\": 14.2578125, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:13\", \"updated\": \"2021-07-13 12:01:24\"}, \"m7051\": {\"id\": \"m7051\", \"cpu\": 92.86, \"mem\": 14.0625, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:14\", \"updated\": \"2021-07-13 12:01:26\"}, \"m7052\": {\"id\": \"m7052\", \"cpu\": 89.29, \"mem\": 14.2578125, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:16\", \"updated\": \"2021-07-13 12:01:28\"}, \"m7053\": {\"id\": \"m7053\", \"cpu\": 89.29, \"mem\": 13.671875, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:17\", \"updated\": \"2021-07-13 12:01:29\"}, \"m7054\": {\"id\": \"m7054\", \"cpu\": 89.29, \"mem\": 13.671875, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:19\", \"updated\": \"2021-07-13 12:01:31\"}, \"m7055\": {\"id\": \"m7055\", \"cpu\": 89.46, \"mem\": 16.015625, \"job\": \"halo\", \"state\": \"idle\", \"runtime\": \"17:30:21\", \"updated\": \"2021-07-13 12:01:33\"}, \"m7056\": {\"id\": \"m7056\", \"cpu\": 13.75, \"mem\": 6.0546875, \"job\": \"star003\", \"state\": \"idle\", \"runtime\": \"3:32:32\", \"updated\": \"2021-07-13 12:01:34\"}, \"m7057\": {\"id\": \"m7057\", \"cpu\": 100.71, \"mem\": 35.3515625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:37:14\", \"updated\": \"2021-07-13 12:01:36\"}, \"m7058\": {\"id\": \"m7058\", \"cpu\": 101.0, \"mem\": 35.3515625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:37:16\", \"updated\": \"2021-07-13 12:01:38\"}, \"m7059\": {\"id\": \"m7059\", \"cpu\": 101.0, \"mem\": 35.15625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:37:17\", \"updated\": \"2021-07-13 12:01:39\"}, \"m7060\": {\"id\": \"m7060\", \"cpu\": 100.71, \"mem\": 35.15625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:37:19\", \"updated\": \"2021-07-13 12:01:41\"}, \"m7061\": {\"id\": \"m7061\", \"cpu\": 17.29, \"mem\": 10.15625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:01:43\"}, \"m7062\": {\"id\": \"m7062\", \"cpu\": 100.0, \"mem\": 10.7421875, \"job\": \"seed1\", \"state\": \"idle\", \"runtime\": \"2-12:01:24\", \"updated\": \"2021-07-13 12:01:44\"}, \"m7063\": {\"id\": \"m7063\", \"cpu\": 57.68, \"mem\": 15.8203125, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:30:34\", \"updated\": \"2021-07-13 12:01:46\"}, \"m7064\": {\"id\": \"m7064\", \"cpu\": 79.46, \"mem\": 15.234375, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:30:36\", \"updated\": \"2021-07-13 12:01:47\"}, \"m7065\": {\"id\": \"m7065\", \"cpu\": 74.07, \"mem\": 15.8203125, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:30:37\", \"updated\": \"2021-07-13 12:01:49\"}, \"m7066\": {\"id\": \"m7066\", \"cpu\": 15.5, \"mem\": 20.703125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:01:51\"}, \"m7067\": {\"id\": \"m7067\", \"cpu\": 22.54, \"mem\": 27.734375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:01:52\"}, \"m7068\": {\"id\": \"m7068\", \"cpu\": 27.54, \"mem\": 26.5625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:01:54\"}, \"m7069\": {\"id\": \"m7069\", \"cpu\": 49.36, \"mem\": 41.40625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:01:56\"}, \"m7070\": {\"id\": \"m7070\", \"cpu\": 100.29, \"mem\": 16.40625, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:04\", \"updated\": \"2021-07-13 12:01:57\"}, \"m7071\": {\"id\": \"m7071\", \"cpu\": 99.61, \"mem\": 26.7578125, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:06\", \"updated\": \"2021-07-13 12:01:59\"}, \"m7072\": {\"id\": \"m7072\", \"cpu\": 99.5, \"mem\": 16.796875, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:08\", \"updated\": \"2021-07-13 12:02:01\"}, \"m7073\": {\"id\": \"m7073\", \"cpu\": 99.75, \"mem\": 16.40625, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:09\", \"updated\": \"2021-07-13 12:02:02\"}, \"m7074\": {\"id\": \"m7074\", \"cpu\": 99.5, \"mem\": 26.3671875, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:11\", \"updated\": \"2021-07-13 12:02:04\"}, \"m7075\": {\"id\": \"m7075\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:33:07\", \"updated\": \"2021-07-13 12:02:06\"}, \"m7076\": {\"id\": \"m7076\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:33:08\", \"updated\": \"2021-07-13 12:02:07\"}, \"m7077\": {\"id\": \"m7077\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:33:10\", \"updated\": \"2021-07-13 12:02:09\"}, \"m7078\": {\"id\": \"m7078\", \"cpu\": 100.0, \"mem\": 40.0390625, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:24:19\", \"updated\": \"2021-07-13 12:02:10\"}, \"m7079\": {\"id\": \"m7079\", \"cpu\": 18.79, \"mem\": 21.09375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:12\"}, \"m7080\": {\"id\": \"m7080\", \"cpu\": 22.5, \"mem\": 30.859375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:14\"}, \"m7081\": {\"id\": \"m7081\", \"cpu\": 26.04, \"mem\": 28.90625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:15\"}, \"m7082\": {\"id\": \"m7082\", \"cpu\": 48.5, \"mem\": 43.75, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:17\"}, \"m7083\": {\"id\": \"m7083\", \"cpu\": 99.93, \"mem\": 9.5703125, \"job\": \"seed1\", \"state\": \"idle\", \"runtime\": \"2-12:01:59\", \"updated\": \"2021-07-13 12:02:19\"}, \"m7084\": {\"id\": \"m7084\", \"cpu\": 96.43, \"mem\": 26.5625, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"11:55:41\", \"updated\": \"2021-07-13 12:02:20\"}, \"m7085\": {\"id\": \"m7085\", \"cpu\": 99.82, \"mem\": 17.578125, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:29\", \"updated\": \"2021-07-13 12:02:22\"}, \"m7086\": {\"id\": \"m7086\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"failed\"}, \"m7087\": {\"id\": \"m7087\", \"cpu\": 99.71, \"mem\": 27.1484375, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:32\", \"updated\": \"2021-07-13 12:02:25\"}, \"m7088\": {\"id\": \"m7088\", \"cpu\": 99.46, \"mem\": 17.578125, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:34\", \"updated\": \"2021-07-13 12:02:27\"}, \"m7089\": {\"id\": \"m7089\", \"cpu\": 100.04, \"mem\": 17.578125, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:36\", \"updated\": \"2021-07-13 12:02:28\"}, \"m7090\": {\"id\": \"m7090\", \"cpu\": 100.29, \"mem\": 27.5390625, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"33:37\", \"updated\": \"2021-07-13 12:02:30\"}, \"m7091\": {\"id\": \"m7091\", \"cpu\": 13.96, \"mem\": 20.5078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:32\"}, \"m7092\": {\"id\": \"m7092\", \"cpu\": 31.21, \"mem\": 30.2734375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:33\"}, \"m7093\": {\"id\": \"m7093\", \"cpu\": 21.93, \"mem\": 28.515625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:35\"}, \"m7094\": {\"id\": \"m7094\", \"cpu\": 42.18, \"mem\": 42.96875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:02:37\"}, \"m7095\": {\"id\": \"m7095\", \"cpu\": 101.18, \"mem\": 35.15625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:38:16\", \"updated\": \"2021-07-13 12:02:38\"}, \"m7096\": {\"id\": \"m7096\", \"cpu\": 101.18, \"mem\": 35.15625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:38:18\", \"updated\": \"2021-07-13 12:02:40\"}, \"m7097\": {\"id\": \"m7097\", \"cpu\": 100.32, \"mem\": 34.765625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:38:20\", \"updated\": \"2021-07-13 12:02:42\"}, \"m7098\": {\"id\": \"m7098\", \"cpu\": 100.0, \"mem\": 37.890625, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:24:51\", \"updated\": \"2021-07-13 12:02:43\"}, \"m7099\": {\"id\": \"m7099\", \"cpu\": 100.0, \"mem\": 37.890625, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:24:53\", \"updated\": \"2021-07-13 12:02:45\"}, \"m7100\": {\"id\": \"m7100\", \"cpu\": 100.04, \"mem\": 37.890625, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:24:55\", \"updated\": \"2021-07-13 12:02:47\"}, \"m7101\": {\"id\": \"m7101\", \"cpu\": 99.96, \"mem\": 37.890625, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:24:56\", \"updated\": \"2021-07-13 12:02:48\"}, \"m7102\": {\"id\": \"m7102\", \"cpu\": 100.0, \"mem\": 52.5390625, \"job\": \"high_low\", \"state\": \"idle\", \"runtime\": \"16:53:50\", \"updated\": \"2021-07-13 12:02:50\"}, \"m7103\": {\"id\": \"m7103\", \"cpu\": 100.0, \"mem\": 53.7109375, \"job\": \"high_low\", \"state\": \"idle\", \"runtime\": \"16:53:52\", \"updated\": \"2021-07-13 12:02:51\"}, \"m7104\": {\"id\": \"m7104\", \"cpu\": 100.11, \"mem\": 51.5625, \"job\": \"high_low\", \"state\": \"idle\", \"runtime\": \"16:53:53\", \"updated\": \"2021-07-13 12:02:53\"}, \"m7105\": {\"id\": \"m7105\", \"cpu\": 100.46, \"mem\": 53.125, \"job\": \"high_low\", \"state\": \"idle\", \"runtime\": \"16:53:55\", \"updated\": \"2021-07-13 12:02:55\"}, \"m7106\": {\"id\": \"m7106\", \"cpu\": 100.29, \"mem\": 38.28125, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:31:45\", \"updated\": \"2021-07-13 12:02:57\"}, \"m7107\": {\"id\": \"m7107\", \"cpu\": 100.0, \"mem\": 37.5, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:19:05\", \"updated\": \"2021-07-13 12:02:58\"}, \"m7108\": {\"id\": \"m7108\", \"cpu\": 100.0, \"mem\": 36.328125, \"job\": \"new_3_hi\", \"state\": \"idle\", \"runtime\": \"21:00:01\", \"updated\": \"2021-07-13 12:03:00\"}, \"m7109\": {\"id\": \"m7109\", \"cpu\": 24.86, \"mem\": 21.6796875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:02\"}, \"m7110\": {\"id\": \"m7110\", \"cpu\": 37.5, \"mem\": 31.25, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:03\"}, \"m7111\": {\"id\": \"m7111\", \"cpu\": 26.39, \"mem\": 30.2734375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:05\"}, \"m7112\": {\"id\": \"m7112\", \"cpu\": 57.07, \"mem\": 43.75, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:07\"}, \"m7113\": {\"id\": \"m7113\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:34:09\", \"updated\": \"2021-07-13 12:03:08\"}, \"m7114\": {\"id\": \"m7114\", \"cpu\": 96.43, \"mem\": 36.71875, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:34:11\", \"updated\": \"2021-07-13 12:03:10\"}, \"m7115\": {\"id\": \"m7115\", \"cpu\": 96.68, \"mem\": 36.71875, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:34:13\", \"updated\": \"2021-07-13 12:03:12\"}, \"m7116\": {\"id\": \"m7116\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:34:14\", \"updated\": \"2021-07-13 12:03:13\"}, \"m7117\": {\"id\": \"m7117\", \"cpu\": 100.0, \"mem\": 37.6953125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:19:21\", \"updated\": \"2021-07-13 12:03:15\"}, \"m7118\": {\"id\": \"m7118\", \"cpu\": 66.25, \"mem\": 15.8203125, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:13:09\", \"updated\": \"2021-07-13 12:03:16\"}, \"m7119\": {\"id\": \"m7119\", \"cpu\": 100.04, \"mem\": 37.6953125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:19:24\", \"updated\": \"2021-07-13 12:03:18\"}, \"m7120\": {\"id\": \"m7120\", \"cpu\": 65.25, \"mem\": 16.2109375, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:13:12\", \"updated\": \"2021-07-13 12:03:20\"}, \"m7121\": {\"id\": \"m7121\", \"cpu\": 100.0, \"mem\": 37.6953125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:19:27\", \"updated\": \"2021-07-13 12:03:21\"}, \"m7122\": {\"id\": \"m7122\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:49:17\", \"updated\": \"2021-07-13 12:03:23\"}, \"m7123\": {\"id\": \"m7123\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:49:19\", \"updated\": \"failed\"}, \"m7124\": {\"id\": \"m7124\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:49:20\", \"updated\": \"2021-07-13 12:03:26\"}, \"m7125\": {\"id\": \"m7125\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:49:22\", \"updated\": \"2021-07-13 12:03:28\"}, \"m7126\": {\"id\": \"m7126\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:49:24\", \"updated\": \"2021-07-13 12:03:30\"}, \"m7127\": {\"id\": \"m7127\", \"cpu\": 59.39, \"mem\": 16.2109375, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:13:23\", \"updated\": \"2021-07-13 12:03:31\"}, \"m7128\": {\"id\": \"m7128\", \"cpu\": 68.0, \"mem\": 15.8203125, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:13:25\", \"updated\": \"2021-07-13 12:03:33\"}, \"m7129\": {\"id\": \"m7129\", \"cpu\": 64.93, \"mem\": 16.2109375, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:13:27\", \"updated\": \"2021-07-13 12:03:34\"}, \"m7130\": {\"id\": \"m7130\", \"cpu\": 96.43, \"mem\": 36.71875, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:34:37\", \"updated\": \"2021-07-13 12:03:36\"}, \"m7131\": {\"id\": \"m7131\", \"cpu\": 100.0, \"mem\": 34.9609375, \"job\": \"new_3_hi\", \"state\": \"idle\", \"runtime\": \"21:00:39\", \"updated\": \"2021-07-13 12:03:38\"}, \"m7132\": {\"id\": \"m7132\", \"cpu\": 100.0, \"mem\": 37.5, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:19:45\", \"updated\": \"2021-07-13 12:03:39\"}, \"m7133\": {\"id\": \"m7133\", \"cpu\": 100.0, \"mem\": 35.15625, \"job\": \"new_3_hi\", \"state\": \"idle\", \"runtime\": \"21:00:42\", \"updated\": \"2021-07-13 12:03:41\"}, \"m7134\": {\"id\": \"m7134\", \"cpu\": 67.11, \"mem\": 15.234375, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:13:35\", \"updated\": \"2021-07-13 12:03:43\"}, \"m7135\": {\"id\": \"m7135\", \"cpu\": 60.36, \"mem\": 15.0390625, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:13:36\", \"updated\": \"2021-07-13 12:03:44\"}, \"m7136\": {\"id\": \"m7136\", \"cpu\": 96.43, \"mem\": 37.109375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:34:47\", \"updated\": \"2021-07-13 12:03:46\"}, \"m7137\": {\"id\": \"m7137\", \"cpu\": 18.04, \"mem\": 20.8984375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:48\"}, \"m7138\": {\"id\": \"m7138\", \"cpu\": 21.86, \"mem\": 30.078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:49\"}, \"m7139\": {\"id\": \"m7139\", \"cpu\": 29.04, \"mem\": 28.7109375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:51\"}, \"m7140\": {\"id\": \"m7140\", \"cpu\": 50.93, \"mem\": 44.140625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:52\"}, \"m7141\": {\"id\": \"m7141\", \"cpu\": 100.0, \"mem\": 34.9609375, \"job\": \"new_3_hi\", \"state\": \"idle\", \"runtime\": \"21:00:55\", \"updated\": \"2021-07-13 12:03:54\"}, \"m7142\": {\"id\": \"m7142\", \"cpu\": 23.36, \"mem\": 20.3125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:56\"}, \"m7143\": {\"id\": \"m7143\", \"cpu\": 33.43, \"mem\": 29.6875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:57\"}, \"m7144\": {\"id\": \"m7144\", \"cpu\": 27.75, \"mem\": 27.9296875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:03:59\"}, \"m7145\": {\"id\": \"m7145\", \"cpu\": 48.86, \"mem\": 44.7265625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:04:01\"}, \"m7146\": {\"id\": \"m7146\", \"cpu\": 100.0, \"mem\": 50.9765625, \"job\": \"high_hig\", \"state\": \"idle\", \"runtime\": \"17:24:39\", \"updated\": \"2021-07-13 12:04:02\"}, \"m7147\": {\"id\": \"m7147\", \"cpu\": 100.0, \"mem\": 52.1484375, \"job\": \"high_hig\", \"state\": \"idle\", \"runtime\": \"17:24:41\", \"updated\": \"2021-07-13 12:04:04\"}, \"m7148\": {\"id\": \"m7148\", \"cpu\": 100.0, \"mem\": 53.90625, \"job\": \"high_hig\", \"state\": \"idle\", \"runtime\": \"17:24:43\", \"updated\": \"2021-07-13 12:04:06\"}, \"m7149\": {\"id\": \"m7149\", \"cpu\": 100.0, \"mem\": 51.953125, \"job\": \"high_hig\", \"state\": \"idle\", \"runtime\": \"17:24:44\", \"updated\": \"2021-07-13 12:04:07\"}, \"m7150\": {\"id\": \"m7150\", \"cpu\": 100.0, \"mem\": 52.734375, \"job\": \"high_hig\", \"state\": \"idle\", \"runtime\": \"17:24:46\", \"updated\": \"2021-07-13 12:04:09\"}, \"m7151\": {\"id\": \"m7151\", \"cpu\": 100.0, \"mem\": 51.5625, \"job\": \"high_hig\", \"state\": \"idle\", \"runtime\": \"17:24:48\", \"updated\": \"2021-07-13 12:04:11\"}, \"m7152\": {\"id\": \"m7152\", \"cpu\": 100.0, \"mem\": 50.78125, \"job\": \"high_hig\", \"state\": \"idle\", \"runtime\": \"17:24:49\", \"updated\": \"2021-07-13 12:04:12\"}, \"m7153\": {\"id\": \"m7153\", \"cpu\": 17.89, \"mem\": 31.25, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:04:14\"}, \"m7154\": {\"id\": \"m7154\", \"cpu\": 100.0, \"mem\": 37.3046875, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:20:22\", \"updated\": \"2021-07-13 12:04:15\"}, \"m7155\": {\"id\": \"m7155\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:20:23\", \"updated\": \"2021-07-13 12:04:17\"}, \"m7156\": {\"id\": \"m7156\", \"cpu\": 100.0, \"mem\": 59.1796875, \"job\": \"low_low_\", \"state\": \"idle\", \"runtime\": \"16:55:19\", \"updated\": \"2021-07-13 12:04:19\"}, \"m7157\": {\"id\": \"m7157\", \"cpu\": 100.0, \"mem\": 61.5234375, \"job\": \"low_low_\", \"state\": \"idle\", \"runtime\": \"16:55:20\", \"updated\": \"2021-07-13 12:04:20\"}, \"m7158\": {\"id\": \"m7158\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:20:28\", \"updated\": \"2021-07-13 12:04:22\"}, \"m7159\": {\"id\": \"m7159\", \"cpu\": 100.0, \"mem\": 53.515625, \"job\": \"high_low\", \"state\": \"idle\", \"runtime\": \"16:55:24\", \"updated\": \"2021-07-13 12:04:24\"}, \"m7160\": {\"id\": \"m7160\", \"cpu\": 100.0, \"mem\": 53.515625, \"job\": \"high_low\", \"state\": \"idle\", \"runtime\": \"16:55:25\", \"updated\": \"2021-07-13 12:04:25\"}, \"m7161\": {\"id\": \"m7161\", \"cpu\": 100.0, \"mem\": 51.3671875, \"job\": \"high_low\", \"state\": \"idle\", \"runtime\": \"16:55:28\", \"updated\": \"2021-07-13 12:04:27\"}, \"m7162\": {\"id\": \"m7162\", \"cpu\": 100.93, \"mem\": 35.15625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:40:07\", \"updated\": \"2021-07-13 12:04:29\"}, \"m7163\": {\"id\": \"m7163\", \"cpu\": 100.46, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:40:09\", \"updated\": \"2021-07-13 12:04:31\"}, \"m7164\": {\"id\": \"m7164\", \"cpu\": 100.5, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:40:11\", \"updated\": \"2021-07-13 12:04:33\"}, \"m7165\": {\"id\": \"m7165\", \"cpu\": 100.79, \"mem\": 35.15625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:40:12\", \"updated\": \"2021-07-13 12:04:34\"}, \"m7166\": {\"id\": \"m7166\", \"cpu\": 101.04, \"mem\": 34.765625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:40:14\", \"updated\": \"2021-07-13 12:04:36\"}, \"m7167\": {\"id\": \"m7167\", \"cpu\": 13.36, \"mem\": 21.6796875, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:19\", \"updated\": \"2021-07-13 12:04:38\"}, \"m7168\": {\"id\": \"m7168\", \"cpu\": 13.32, \"mem\": 21.484375, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:20\", \"updated\": \"2021-07-13 12:04:39\"}, \"m7169\": {\"id\": \"m7169\", \"cpu\": 13.71, \"mem\": 21.09375, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:22\", \"updated\": \"2021-07-13 12:04:41\"}, \"m7170\": {\"id\": \"m7170\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:24\", \"updated\": \"failed\"}, \"m7171\": {\"id\": \"m7171\", \"cpu\": 14.14, \"mem\": 21.875, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:25\", \"updated\": \"2021-07-13 12:04:44\"}, \"m7172\": {\"id\": \"m7172\", \"cpu\": 13.39, \"mem\": 20.8984375, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:27\", \"updated\": \"2021-07-13 12:04:46\"}, \"m7173\": {\"id\": \"m7173\", \"cpu\": 13.54, \"mem\": 21.2890625, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:29\", \"updated\": \"2021-07-13 12:04:47\"}, \"m7174\": {\"id\": \"m7174\", \"cpu\": 13.93, \"mem\": 21.875, \"job\": \"BAHAFIDL\", \"state\": \"idle\", \"runtime\": \"2-02:10:30\", \"updated\": \"2021-07-13 12:04:49\"}, \"m7175\": {\"id\": \"m7175\", \"cpu\": 100.46, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:50:45\", \"updated\": \"2021-07-13 12:04:51\"}, \"m7176\": {\"id\": \"m7176\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:50:46\", \"updated\": \"2021-07-13 12:04:52\"}, \"m7177\": {\"id\": \"m7177\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:50:48\", \"updated\": \"2021-07-13 12:04:54\"}, \"m7178\": {\"id\": \"m7178\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:50:50\", \"updated\": \"2021-07-13 12:04:56\"}, \"m7179\": {\"id\": \"m7179\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:50:51\", \"updated\": \"2021-07-13 12:04:57\"}, \"m7180\": {\"id\": \"m7180\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:50:53\", \"updated\": \"2021-07-13 12:04:59\"}, \"m7181\": {\"id\": \"m7181\", \"cpu\": 3.57, \"mem\": 25.1953125, \"job\": \"PF4125_9\", \"state\": \"idle\", \"runtime\": \"17:08\", \"updated\": \"2021-07-13 12:05:01\"}, \"m7182\": {\"id\": \"m7182\", \"cpu\": 96.43, \"mem\": 26.7578125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"11:58:23\", \"updated\": \"2021-07-13 12:05:02\"}, \"m7183\": {\"id\": \"m7183\", \"cpu\": 69.75, \"mem\": 15.625, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:33:52\", \"updated\": \"2021-07-13 12:05:04\"}, \"m7184\": {\"id\": \"m7184\", \"cpu\": 64.5, \"mem\": 15.4296875, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:33:54\", \"updated\": \"2021-07-13 12:05:05\"}, \"m7185\": {\"id\": \"m7185\", \"cpu\": 53.04, \"mem\": 15.625, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:33:55\", \"updated\": \"2021-07-13 12:05:07\"}, \"m7186\": {\"id\": \"m7186\", \"cpu\": 72.43, \"mem\": 15.4296875, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:33:57\", \"updated\": \"2021-07-13 12:05:09\"}, \"m7187\": {\"id\": \"m7187\", \"cpu\": 68.75, \"mem\": 15.625, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:33:58\", \"updated\": \"2021-07-13 12:05:10\"}, \"m7188\": {\"id\": \"m7188\", \"cpu\": 78.18, \"mem\": 15.234375, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:00\", \"updated\": \"2021-07-13 12:05:12\"}, \"m7189\": {\"id\": \"m7189\", \"cpu\": 62.21, \"mem\": 14.453125, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:02\", \"updated\": \"2021-07-13 12:05:14\"}, \"m7190\": {\"id\": \"m7190\", \"cpu\": 56.64, \"mem\": 14.6484375, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:03\", \"updated\": \"2021-07-13 12:05:15\"}, \"m7191\": {\"id\": \"m7191\", \"cpu\": 87.46, \"mem\": 14.84375, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:05\", \"updated\": \"2021-07-13 12:05:17\"}, \"m7192\": {\"id\": \"m7192\", \"cpu\": 99.32, \"mem\": 15.234375, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:07\", \"updated\": \"2021-07-13 12:05:19\"}, \"m7193\": {\"id\": \"m7193\", \"cpu\": 99.25, \"mem\": 15.0390625, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:08\", \"updated\": \"2021-07-13 12:05:20\"}, \"m7194\": {\"id\": \"m7194\", \"cpu\": 98.43, \"mem\": 15.0390625, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:10\", \"updated\": \"2021-07-13 12:05:22\"}, \"m7195\": {\"id\": \"m7195\", \"cpu\": 98.0, \"mem\": 15.4296875, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:12\", \"updated\": \"2021-07-13 12:05:24\"}, \"m7196\": {\"id\": \"m7196\", \"cpu\": 98.36, \"mem\": 15.4296875, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:13\", \"updated\": \"2021-07-13 12:05:25\"}, \"m7197\": {\"id\": \"m7197\", \"cpu\": 98.57, \"mem\": 15.4296875, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:15\", \"updated\": \"2021-07-13 12:05:27\"}, \"m7198\": {\"id\": \"m7198\", \"cpu\": 99.54, \"mem\": 15.625, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:17\", \"updated\": \"2021-07-13 12:05:28\"}, \"m7199\": {\"id\": \"m7199\", \"cpu\": 95.29, \"mem\": 15.625, \"job\": \"run0034I\", \"state\": \"idle\", \"runtime\": \"17:34:18\", \"updated\": \"2021-07-13 12:05:30\"}, \"m7200\": {\"id\": \"m7200\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:21:38\", \"updated\": \"2021-07-13 12:05:32\"}, \"m7201\": {\"id\": \"m7201\", \"cpu\": 14.82, \"mem\": 20.5078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:33\"}, \"m7202\": {\"id\": \"m7202\", \"cpu\": 27.61, \"mem\": 28.90625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:35\"}, \"m7203\": {\"id\": \"m7203\", \"cpu\": 24.68, \"mem\": 27.734375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:37\"}, \"m7204\": {\"id\": \"m7204\", \"cpu\": 48.61, \"mem\": 39.84375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:38\"}, \"m7205\": {\"id\": \"m7205\", \"cpu\": 17.43, \"mem\": 20.8984375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:40\"}, \"m7206\": {\"id\": \"m7206\", \"cpu\": 30.79, \"mem\": 30.6640625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:42\"}, \"m7207\": {\"id\": \"m7207\", \"cpu\": 32.68, \"mem\": 29.296875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:43\"}, \"m7208\": {\"id\": \"m7208\", \"cpu\": 57.54, \"mem\": 44.921875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:45\"}, \"m7209\": {\"id\": \"m7209\", \"cpu\": 22.71, \"mem\": 8.59375, \"job\": \"PFM6\", \"state\": \"idle\", \"runtime\": \"4:15:38\", \"updated\": \"2021-07-13 12:05:46\"}, \"m7210\": {\"id\": \"m7210\", \"cpu\": 96.43, \"mem\": 26.5625, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"11:59:09\", \"updated\": \"2021-07-13 12:05:48\"}, \"m7211\": {\"id\": \"m7211\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:21:56\", \"updated\": \"2021-07-13 12:05:50\"}, \"m7212\": {\"id\": \"m7212\", \"cpu\": 96.43, \"mem\": 26.5625, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"11:59:12\", \"updated\": \"2021-07-13 12:05:51\"}, \"m7213\": {\"id\": \"m7213\", \"cpu\": 96.43, \"mem\": 26.7578125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"11:59:14\", \"updated\": \"2021-07-13 12:05:53\"}, \"m7214\": {\"id\": \"m7214\", \"cpu\": 17.21, \"mem\": 21.09375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:55\"}, \"m7215\": {\"id\": \"m7215\", \"cpu\": 28.64, \"mem\": 30.6640625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:56\"}, \"m7216\": {\"id\": \"m7216\", \"cpu\": 25.21, \"mem\": 28.515625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:05:58\"}, \"m7217\": {\"id\": \"m7217\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"failed\"}, \"m7218\": {\"id\": \"m7218\", \"cpu\": 3.57, \"mem\": 24.4140625, \"job\": \"PF4125_9\", \"state\": \"idle\", \"runtime\": \"22:16\", \"updated\": \"2021-07-13 12:06:02\"}, \"m7219\": {\"id\": \"m7219\", \"cpu\": 96.43, \"mem\": 36.71875, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:22:10\", \"updated\": \"2021-07-13 12:06:04\"}, \"m7220\": {\"id\": \"m7220\", \"cpu\": 191.04, \"mem\": 47.65625, \"job\": \"cosmo\", \"state\": \"idle\", \"runtime\": \"16:04:17\", \"updated\": \"2021-07-13 12:06:05\"}, \"m7221\": {\"id\": \"m7221\", \"cpu\": 96.43, \"mem\": 36.71875, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:22:13\", \"updated\": \"2021-07-13 12:06:07\"}, \"m7222\": {\"id\": \"m7222\", \"cpu\": 0.0, \"mem\": 5.078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:06:09\"}, \"m7223\": {\"id\": \"m7223\", \"cpu\": 99.5, \"mem\": 16.9921875, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:34:58\", \"updated\": \"2021-07-13 12:06:10\"}, \"m7224\": {\"id\": \"m7224\", \"cpu\": 99.61, \"mem\": 16.40625, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:00\", \"updated\": \"2021-07-13 12:06:12\"}, \"m7225\": {\"id\": \"m7225\", \"cpu\": 99.32, \"mem\": 16.6015625, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:02\", \"updated\": \"2021-07-13 12:06:14\"}, \"m7226\": {\"id\": \"m7226\", \"cpu\": 97.75, \"mem\": 16.9921875, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:03\", \"updated\": \"2021-07-13 12:06:15\"}, \"m7227\": {\"id\": \"m7227\", \"cpu\": 99.5, \"mem\": 17.3828125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:05\", \"updated\": \"2021-07-13 12:06:17\"}, \"m7228\": {\"id\": \"m7228\", \"cpu\": 99.39, \"mem\": 17.3828125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:07\", \"updated\": \"2021-07-13 12:06:18\"}, \"m7229\": {\"id\": \"m7229\", \"cpu\": 97.25, \"mem\": 17.578125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:08\", \"updated\": \"2021-07-13 12:06:20\"}, \"m7230\": {\"id\": \"m7230\", \"cpu\": 97.04, \"mem\": 17.578125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:10\", \"updated\": \"2021-07-13 12:06:22\"}, \"m7231\": {\"id\": \"m7231\", \"cpu\": 97.75, \"mem\": 17.7734375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:11\", \"updated\": \"2021-07-13 12:06:23\"}, \"m7232\": {\"id\": \"m7232\", \"cpu\": 96.86, \"mem\": 17.3828125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:13\", \"updated\": \"2021-07-13 12:06:25\"}, \"m7233\": {\"id\": \"m7233\", \"cpu\": 99.71, \"mem\": 17.7734375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:15\", \"updated\": \"2021-07-13 12:06:27\"}, \"m7234\": {\"id\": \"m7234\", \"cpu\": 99.36, \"mem\": 17.7734375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:16\", \"updated\": \"2021-07-13 12:06:28\"}, \"m7235\": {\"id\": \"m7235\", \"cpu\": 98.64, \"mem\": 17.96875, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:18\", \"updated\": \"2021-07-13 12:06:30\"}, \"m7236\": {\"id\": \"m7236\", \"cpu\": 98.29, \"mem\": 17.96875, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:20\", \"updated\": \"2021-07-13 12:06:32\"}, \"m7237\": {\"id\": \"m7237\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime";
        String stringpart2 = "\": \"17:35:21\", \"updated\": \"failed\"}, \"m7238\": {\"id\": \"m7238\", \"cpu\": 99.43, \"mem\": 17.578125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:23\", \"updated\": \"2021-07-13 12:06:35\"}, \"m7239\": {\"id\": \"m7239\", \"cpu\": 99.54, \"mem\": 17.3828125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:25\", \"updated\": \"2021-07-13 12:06:36\"}, \"m7240\": {\"id\": \"m7240\", \"cpu\": 99.64, \"mem\": 17.3828125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:26\", \"updated\": \"2021-07-13 12:06:38\"}, \"m7241\": {\"id\": \"m7241\", \"cpu\": 99.54, \"mem\": 17.7734375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:28\", \"updated\": \"2021-07-13 12:06:40\"}, \"m7242\": {\"id\": \"m7242\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:22:47\", \"updated\": \"2021-07-13 12:06:41\"}, \"m7243\": {\"id\": \"m7243\", \"cpu\": 87.96, \"mem\": 15.8203125, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:16:35\", \"updated\": \"2021-07-13 12:06:43\"}, \"m7244\": {\"id\": \"m7244\", \"cpu\": 99.21, \"mem\": 15.234375, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:16:37\", \"updated\": \"2021-07-13 12:06:45\"}, \"m7245\": {\"id\": \"m7245\", \"cpu\": 18.29, \"mem\": 21.09375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:06:46\"}, \"m7246\": {\"id\": \"m7246\", \"cpu\": 24.71, \"mem\": 31.25, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:06:48\"}, \"m7247\": {\"id\": \"m7247\", \"cpu\": 25.36, \"mem\": 29.296875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:06:50\"}, \"m7248\": {\"id\": \"m7248\", \"cpu\": 52.61, \"mem\": 45.5078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:06:51\"}, \"m7249\": {\"id\": \"m7249\", \"cpu\": 97.82, \"mem\": 17.578125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:41\", \"updated\": \"2021-07-13 12:06:53\"}, \"m7250\": {\"id\": \"m7250\", \"cpu\": 99.25, \"mem\": 17.1875, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:43\", \"updated\": \"2021-07-13 12:06:55\"}, \"m7251\": {\"id\": \"m7251\", \"cpu\": 98.18, \"mem\": 18.359375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:44\", \"updated\": \"2021-07-13 12:06:56\"}, \"m7252\": {\"id\": \"m7252\", \"cpu\": 93.5, \"mem\": 17.1875, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:46\", \"updated\": \"2021-07-13 12:06:58\"}, \"m7253\": {\"id\": \"m7253\", \"cpu\": 96.61, \"mem\": 18.359375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:47\", \"updated\": \"2021-07-13 12:06:59\"}, \"m7254\": {\"id\": \"m7254\", \"cpu\": 98.11, \"mem\": 17.7734375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:49\", \"updated\": \"2021-07-13 12:07:01\"}, \"m7255\": {\"id\": \"m7255\", \"cpu\": 97.57, \"mem\": 17.1875, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:51\", \"updated\": \"2021-07-13 12:07:03\"}, \"m7256\": {\"id\": \"m7256\", \"cpu\": 93.32, \"mem\": 17.578125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:52\", \"updated\": \"2021-07-13 12:07:04\"}, \"m7257\": {\"id\": \"m7257\", \"cpu\": 98.68, \"mem\": 17.578125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:54\", \"updated\": \"2021-07-13 12:07:06\"}, \"m7258\": {\"id\": \"m7258\", \"cpu\": 99.0, \"mem\": 17.7734375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:56\", \"updated\": \"2021-07-13 12:07:08\"}, \"m7259\": {\"id\": \"m7259\", \"cpu\": 99.14, \"mem\": 17.3828125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:57\", \"updated\": \"2021-07-13 12:07:09\"}, \"m7260\": {\"id\": \"m7260\", \"cpu\": 97.68, \"mem\": 17.7734375, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:35:59\", \"updated\": \"2021-07-13 12:07:11\"}, \"m7261\": {\"id\": \"m7261\", \"cpu\": 98.11, \"mem\": 17.578125, \"job\": \"R0.65A12\", \"state\": \"idle\", \"runtime\": \"17:36:01\", \"updated\": \"2021-07-13 12:07:13\"}, \"m7262\": {\"id\": \"m7262\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:23:20\", \"updated\": \"2021-07-13 12:07:14\"}, \"m7263\": {\"id\": \"m7263\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:23:22\", \"updated\": \"2021-07-13 12:07:16\"}, \"m7264\": {\"id\": \"m7264\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:23:24\", \"updated\": \"2021-07-13 12:07:17\"}, \"m7265\": {\"id\": \"m7265\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:23:25\", \"updated\": \"2021-07-13 12:07:19\"}, \"m7266\": {\"id\": \"m7266\", \"cpu\": 174.32, \"mem\": 47.4609375, \"job\": \"cosmo\", \"state\": \"idle\", \"runtime\": \"14:48:21\", \"updated\": \"2021-07-13 12:07:21\"}, \"m7267\": {\"id\": \"m7267\", \"cpu\": 197.93, \"mem\": 47.4609375, \"job\": \"cosmo\", \"state\": \"idle\", \"runtime\": \"14:48:22\", \"updated\": \"2021-07-13 12:07:22\"}, \"m7268\": {\"id\": \"m7268\", \"cpu\": 96.54, \"mem\": 36.1328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:23:30\", \"updated\": \"2021-07-13 12:07:24\"}, \"m7269\": {\"id\": \"m7269\", \"cpu\": 100.36, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:53:20\", \"updated\": \"2021-07-13 12:07:26\"}, \"m7270\": {\"id\": \"m7270\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:53:21\", \"updated\": \"2021-07-13 12:07:27\"}, \"m7271\": {\"id\": \"m7271\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:53:23\", \"updated\": \"2021-07-13 12:07:29\"}, \"m7272\": {\"id\": \"m7272\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:53:25\", \"updated\": \"2021-07-13 12:07:31\"}, \"m7273\": {\"id\": \"m7273\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:53:27\", \"updated\": \"2021-07-13 12:07:32\"}, \"m7274\": {\"id\": \"m7274\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:23:41\", \"updated\": \"2021-07-13 12:07:35\"}, \"m7275\": {\"id\": \"m7275\", \"cpu\": 171.29, \"mem\": 47.65625, \"job\": \"cosmo\", \"state\": \"idle\", \"runtime\": \"17:48:25\", \"updated\": \"2021-07-13 12:07:36\"}, \"m7276\": {\"id\": \"m7276\", \"cpu\": 92.79, \"mem\": 15.234375, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:17:30\", \"updated\": \"2021-07-13 12:07:38\"}, \"m7277\": {\"id\": \"m7277\", \"cpu\": 127.64, \"mem\": 8.7890625, \"job\": \"SPT-0532\", \"state\": \"idle\", \"runtime\": \"2-18:02:12\", \"updated\": \"2021-07-13 12:07:40\"}, \"m7278\": {\"id\": \"m7278\", \"cpu\": 100.21, \"mem\": 69.7265625, \"job\": \"low_high\", \"state\": \"idle\", \"runtime\": \"16:35:11\", \"updated\": \"2021-07-13 12:07:41\"}, \"m7279\": {\"id\": \"m7279\", \"cpu\": 100.0, \"mem\": 68.75, \"job\": \"low_high\", \"state\": \"idle\", \"runtime\": \"16:35:13\", \"updated\": \"2021-07-13 12:07:43\"}, \"m7280\": {\"id\": \"m7280\", \"cpu\": 100.0, \"mem\": 69.140625, \"job\": \"low_high\", \"state\": \"idle\", \"runtime\": \"16:35:15\", \"updated\": \"2021-07-13 12:07:45\"}, \"m7281\": {\"id\": \"m7281\", \"cpu\": 96.46, \"mem\": 66.796875, \"job\": \"low_high\", \"state\": \"idle\", \"runtime\": \"16:35:16\", \"updated\": \"2021-07-13 12:07:46\"}, \"m7282\": {\"id\": \"m7282\", \"cpu\": 96.86, \"mem\": 66.40625, \"job\": \"low_high\", \"state\": \"idle\", \"runtime\": \"16:35:18\", \"updated\": \"2021-07-13 12:07:48\"}, \"m7283\": {\"id\": \"m7283\", \"cpu\": 96.43, \"mem\": 66.6015625, \"job\": \"low_high\", \"state\": \"idle\", \"runtime\": \"16:35:20\", \"updated\": \"2021-07-13 12:07:49\"}, \"m7284\": {\"id\": \"m7284\", \"cpu\": 96.43, \"mem\": 64.0625, \"job\": \"low_high\", \"state\": \"idle\", \"runtime\": \"16:35:21\", \"updated\": \"2021-07-13 12:07:51\"}, \"m7285\": {\"id\": \"m7285\", \"cpu\": 189.14, \"mem\": 47.4609375, \"job\": \"cosmo\", \"state\": \"idle\", \"runtime\": \"16:03:01\", \"updated\": \"2021-07-13 12:07:53\"}, \"m7286\": {\"id\": \"m7286\", \"cpu\": 96.46, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:24:00\", \"updated\": \"2021-07-13 12:07:54\"}, \"m7287\": {\"id\": \"m7287\", \"cpu\": 22.0, \"mem\": 21.6796875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:07:56\"}, \"m7288\": {\"id\": \"m7288\", \"cpu\": 30.29, \"mem\": 31.25, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:07:58\"}, \"m7289\": {\"id\": \"m7289\", \"cpu\": 34.79, \"mem\": 30.078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:07:59\"}, \"m7290\": {\"id\": \"m7290\", \"cpu\": 49.0, \"mem\": 43.9453125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:08:01\"}, \"m7291\": {\"id\": \"m7291\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:24:09\", \"updated\": \"2021-07-13 12:08:03\"}, \"m7292\": {\"id\": \"m7292\", \"cpu\": 100.11, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:53:58\", \"updated\": \"2021-07-13 12:08:04\"}, \"m7293\": {\"id\": \"m7293\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:00\", \"updated\": \"2021-07-13 12:08:06\"}, \"m7294\": {\"id\": \"m7294\", \"cpu\": 100.0, \"mem\": 5.6640625, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:02\", \"updated\": \"2021-07-13 12:08:07\"}, \"m7295\": {\"id\": \"m7295\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:03\", \"updated\": \"failed\"}, \"m7296\": {\"id\": \"m7296\", \"cpu\": 100.29, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:05\", \"updated\": \"2021-07-13 12:08:11\"}, \"m7297\": {\"id\": \"m7297\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTCRiMHD\", \"state\": \"idle\", \"runtime\": \"1-11:24:18\", \"updated\": \"2021-07-13 12:08:12\"}, \"m7298\": {\"id\": \"m7298\", \"cpu\": 3.57, \"mem\": 24.4140625, \"job\": \"PF4125_9\", \"state\": \"idle\", \"runtime\": \"24:28\", \"updated\": \"2021-07-13 12:08:14\"}, \"m7299\": {\"id\": \"m7299\", \"cpu\": 99.39, \"mem\": 15.625, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:18:08\", \"updated\": \"2021-07-13 12:08:16\"}, \"m7300\": {\"id\": \"m7300\", \"cpu\": 96.43, \"mem\": 36.71875, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:39:18\", \"updated\": \"2021-07-13 12:08:17\"}, \"m7301\": {\"id\": \"m7301\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:39:20\", \"updated\": \"2021-07-13 12:08:19\"}, \"m7302\": {\"id\": \"m7302\", \"cpu\": 91.46, \"mem\": 15.625, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:18:13\", \"updated\": \"2021-07-13 12:08:21\"}, \"m7303\": {\"id\": \"m7303\", \"cpu\": 98.36, \"mem\": 15.625, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:18:14\", \"updated\": \"2021-07-13 12:08:22\"}, \"m7304\": {\"id\": \"m7304\", \"cpu\": 100.0, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:18\", \"updated\": \"2021-07-13 12:08:24\"}, \"m7305\": {\"id\": \"m7305\", \"cpu\": 100.0, \"mem\": 5.2734375, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:20\", \"updated\": \"2021-07-13 12:08:25\"}, \"m7306\": {\"id\": \"m7306\", \"cpu\": 100.0, \"mem\": 5.2734375, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:21\", \"updated\": \"2021-07-13 12:08:27\"}, \"m7307\": {\"id\": \"m7307\", \"cpu\": 96.46, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:23\", \"updated\": \"2021-07-13 12:08:29\"}, \"m7308\": {\"id\": \"m7308\", \"cpu\": 96.57, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:24\", \"updated\": \"2021-07-13 12:08:30\"}, \"m7309\": {\"id\": \"m7309\", \"cpu\": 96.46, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:26\", \"updated\": \"2021-07-13 12:08:32\"}, \"m7310\": {\"id\": \"m7310\", \"cpu\": 96.43, \"mem\": 5.2734375, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:28\", \"updated\": \"2021-07-13 12:08:34\"}, \"m7311\": {\"id\": \"m7311\", \"cpu\": 96.43, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:54:29\", \"updated\": \"2021-07-13 12:08:35\"}, \"m7312\": {\"id\": \"m7312\", \"cpu\": 100.0, \"mem\": 37.109375, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:37:25\", \"updated\": \"2021-07-13 12:08:37\"}, \"m7313\": {\"id\": \"m7313\", \"cpu\": 100.0, \"mem\": 36.9140625, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:37:27\", \"updated\": \"2021-07-13 12:08:39\"}, \"m7314\": {\"id\": \"m7314\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:39:41\", \"updated\": \"2021-07-13 12:08:40\"}, \"m7315\": {\"id\": \"m7315\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:39:43\", \"updated\": \"2021-07-13 12:08:42\"}, \"m7316\": {\"id\": \"m7316\", \"cpu\": 96.43, \"mem\": 36.5234375, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:39:45\", \"updated\": \"2021-07-13 12:08:44\"}, \"m7317\": {\"id\": \"m7317\", \"cpu\": 0.0, \"mem\": 5.2734375, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"23:27\", \"updated\": \"2021-07-13 12:08:45\"}, \"m7318\": {\"id\": \"m7318\", \"cpu\": 0.0, \"mem\": 5.2734375, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"23:29\", \"updated\": \"2021-07-13 12:08:47\"}, \"m7319\": {\"id\": \"m7319\", \"cpu\": 3.57, \"mem\": 6.4453125, \"job\": \"SPT-0532\", \"state\": \"idle\", \"runtime\": \"1-02:46:20\", \"updated\": \"2021-07-13 12:08:48\"}, \"m7320\": {\"id\": \"m7320\", \"cpu\": 100.29, \"mem\": 35.15625, \"job\": \"new_3_hi\", \"state\": \"idle\", \"runtime\": \"21:05:51\", \"updated\": \"2021-07-13 12:08:50\"}, \"m7321\": {\"id\": \"m7321\", \"cpu\": 97.82, \"mem\": 15.8203125, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:18:44\", \"updated\": \"2021-07-13 12:08:52\"}, \"m7322\": {\"id\": \"m7322\", \"cpu\": 84.89, \"mem\": 16.015625, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:18:45\", \"updated\": \"2021-07-13 12:08:53\"}, \"m7323\": {\"id\": \"m7323\", \"cpu\": 0.0, \"mem\": 5.2734375, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"23:37\", \"updated\": \"2021-07-13 12:08:55\"}, \"m7324\": {\"id\": \"m7324\", \"cpu\": 0.0, \"mem\": 5.2734375, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"23:39\", \"updated\": \"2021-07-13 12:08:57\"}, \"m7325\": {\"id\": \"m7325\", \"cpu\": 25.32, \"mem\": 31.640625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:08:58\"}, \"m7326\": {\"id\": \"m7326\", \"cpu\": 15.96, \"mem\": 6.4453125, \"job\": \"star001\", \"state\": \"idle\", \"runtime\": \"3:39:22\", \"updated\": \"2021-07-13 12:09:00\"}, \"m7327\": {\"id\": \"m7327\", \"cpu\": 0.0, \"mem\": 5.46875, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"23:44\", \"updated\": \"2021-07-13 12:09:02\"}, \"m7328\": {\"id\": \"m7328\", \"cpu\": 0.0, \"mem\": 5.46875, \"job\": \"Gadget4_\", \"state\": \"idle\", \"runtime\": \"23:45\", \"updated\": \"2021-07-13 12:09:03\"}, \"m7329\": {\"id\": \"m7329\", \"cpu\": 95.25, \"mem\": 15.8203125, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:37:54\", \"updated\": \"2021-07-13 12:09:05\"}, \"m7330\": {\"id\": \"m7330\", \"cpu\": 93.89, \"mem\": 15.625, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:37:56\", \"updated\": \"2021-07-13 12:09:07\"}, \"m7331\": {\"id\": \"m7331\", \"cpu\": 94.75, \"mem\": 15.8203125, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:37:57\", \"updated\": \"2021-07-13 12:09:09\"}, \"m7332\": {\"id\": \"m7332\", \"cpu\": 95.0, \"mem\": 15.4296875, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:37:59\", \"updated\": \"2021-07-13 12:09:11\"}, \"m7333\": {\"id\": \"m7333\", \"cpu\": 92.39, \"mem\": 15.8203125, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:38:00\", \"updated\": \"2021-07-13 12:09:12\"}, \"m7334\": {\"id\": \"m7334\", \"cpu\": 97.21, \"mem\": 15.4296875, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:38:02\", \"updated\": \"2021-07-13 12:09:14\"}, \"m7335\": {\"id\": \"m7335\", \"cpu\": 89.43, \"mem\": 16.015625, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:38:04\", \"updated\": \"2021-07-13 12:09:16\"}, \"m7336\": {\"id\": \"m7336\", \"cpu\": 92.96, \"mem\": 15.8203125, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:38:05\", \"updated\": \"2021-07-13 12:09:17\"}, \"m7337\": {\"id\": \"m7337\", \"cpu\": 91.61, \"mem\": 15.4296875, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:38:07\", \"updated\": \"2021-07-13 12:09:19\"}, \"m7338\": {\"id\": \"m7338\", \"cpu\": 92.46, \"mem\": 14.6484375, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:38:09\", \"updated\": \"2021-07-13 12:09:21\"}, \"m7339\": {\"id\": \"m7339\", \"cpu\": 96.43, \"mem\": 36.328125, \"job\": \"RTnsCRiM\", \"state\": \"idle\", \"runtime\": \"1-21:40:23\", \"updated\": \"2021-07-13 12:09:22\"}, \"m7340\": {\"id\": \"m7340\", \"cpu\": 100.0, \"mem\": 17.7734375, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"40:31\", \"updated\": \"2021-07-13 12:09:24\"}, \"m7341\": {\"id\": \"m7341\", \"cpu\": 100.0, \"mem\": 26.7578125, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"40:33\", \"updated\": \"2021-07-13 12:09:25\"}, \"m7342\": {\"id\": \"m7342\", \"cpu\": 100.14, \"mem\": 17.7734375, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"40:34\", \"updated\": \"2021-07-13 12:09:27\"}, \"m7343\": {\"id\": \"m7343\", \"cpu\": 100.0, \"mem\": 36.9140625, \"job\": \"F4125_69\", \"state\": \"idle\", \"runtime\": \"40:36\", \"updated\": \"2021-07-13 12:09:29\"}, \"m7344\": {\"id\": \"m7344\", \"cpu\": 61.54, \"mem\": 15.8203125, \"job\": \"run0023I\", \"state\": \"idle\", \"runtime\": \"23:19:22\", \"updated\": \"2021-07-13 12:09:30\"}, \"m7345\": {\"id\": \"m7345\", \"cpu\": 14.25, \"mem\": 21.09375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:09:32\"}, \"m7346\": {\"id\": \"m7346\", \"cpu\": 26.29, \"mem\": 30.2734375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:09:34\"}, \"m7347\": {\"id\": \"m7347\", \"cpu\": 26.57, \"mem\": 28.90625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:09:35\"}, \"m7348\": {\"id\": \"m7348\", \"cpu\": 49.89, \"mem\": 42.96875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:09:37\"}, \"m7349\": {\"id\": \"m7349\", \"cpu\": 100.29, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:45:17\", \"updated\": \"2021-07-13 12:09:39\"}, \"m7350\": {\"id\": \"m7350\", \"cpu\": 100.29, \"mem\": 34.765625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:45:18\", \"updated\": \"2021-07-13 12:09:40\"}, \"m7351\": {\"id\": \"m7351\", \"cpu\": 100.89, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:45:20\", \"updated\": \"2021-07-13 12:09:42\"}, \"m7352\": {\"id\": \"m7352\", \"cpu\": 101.14, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:45:22\", \"updated\": \"2021-07-13 12:09:44\"}, \"m7353\": {\"id\": \"m7353\", \"cpu\": 96.43, \"mem\": 26.5625, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:03:06\", \"updated\": \"2021-07-13 12:09:45\"}, \"m7354\": {\"id\": \"m7354\", \"cpu\": 96.43, \"mem\": 26.5625, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:03:08\", \"updated\": \"2021-07-13 12:09:47\"}, \"m7355\": {\"id\": \"m7355\", \"cpu\": 96.43, \"mem\": 26.5625, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:03:10\", \"updated\": \"2021-07-13 12:09:48\"}, \"m7356\": {\"id\": \"m7356\", \"cpu\": 96.43, \"mem\": 26.5625, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:03:11\", \"updated\": \"2021-07-13 12:09:50\"}, \"m7357\": {\"id\": \"m7357\", \"cpu\": 96.43, \"mem\": 26.7578125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:03:13\", \"updated\": \"2021-07-13 12:09:52\"}, \"m7358\": {\"id\": \"m7358\", \"cpu\": 25.18, \"mem\": 8.59375, \"job\": \"PFM4O0\", \"state\": \"idle\", \"runtime\": \"2:58:52\", \"updated\": \"2021-07-13 12:09:53\"}, \"m7359\": {\"id\": \"m7359\", \"cpu\": 100.0, \"mem\": 36.9140625, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:38:43\", \"updated\": \"2021-07-13 12:09:55\"}, \"m7360\": {\"id\": \"m7360\", \"cpu\": 99.79, \"mem\": 36.9140625, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:38:45\", \"updated\": \"2021-07-13 12:09:57\"}, \"m7361\": {\"id\": \"m7361\", \"cpu\": 100.0, \"mem\": 37.109375, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:38:46\", \"updated\": \"2021-07-13 12:09:58\"}, \"m7362\": {\"id\": \"m7362\", \"cpu\": 23.18, \"mem\": 22.0703125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:00\"}, \"m7363\": {\"id\": \"m7363\", \"cpu\": 35.71, \"mem\": 32.03125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:02\"}, \"m7364\": {\"id\": \"m7364\", \"cpu\": 35.32, \"mem\": 30.6640625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:03\"}, \"m7365\": {\"id\": \"m7365\", \"cpu\": 67.0, \"mem\": 44.921875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:05\"}, \"m7366\": {\"id\": \"m7366\", \"cpu\": 92.86, \"mem\": 25.78125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:03:28\", \"updated\": \"2021-07-13 12:10:06\"}, \"m7367\": {\"id\": \"m7367\", \"cpu\": 199.39, \"mem\": 47.4609375, \"job\": \"cosmo\", \"state\": \"idle\", \"runtime\": \"14:32:19\", \"updated\": \"2021-07-13 12:10:08\"}, \"m7368\": {\"id\": \"m7368\", \"cpu\": 99.14, \"mem\": 5.6640625, \"job\": \"HOD_Fitt\", \"state\": \"idle\", \"runtime\": \"1-16:09:40\", \"updated\": \"2021-07-13 12:10:10\"}, \"m7369\": {\"id\": \"m7369\", \"cpu\": 13.96, \"mem\": 20.3125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:11\"}, \"m7370\": {\"id\": \"m7370\", \"cpu\": 19.57, \"mem\": 30.078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:13\"}, \"m7371\": {\"id\": \"m7371\", \"cpu\": 21.04, \"mem\": 28.3203125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:15\"}, \"m7372\": {\"id\": \"m7372\", \"cpu\": 36.14, \"mem\": 44.53125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:16\"}, \"m7373\": {\"id\": \"m7373\", \"cpu\": 100.0, \"mem\": 34.765625, \"job\": \"new_3_hi\", \"state\": \"idle\", \"runtime\": \"21:07:19\", \"updated\": \"2021-07-13 12:10:18\"}, \"m7374\": {\"id\": \"m7374\", \"cpu\": 6.32, \"mem\": 8.984375, \"job\": \"99b\", \"state\": \"idle\", \"runtime\": \"2-10:21:47\", \"updated\": \"2021-07-13 12:10:20\"}, \"m7375\": {\"id\": \"m7375\", \"cpu\": 0.0, \"mem\": 5.078125, \"job\": \"bash\", \"state\": \"idle\", \"runtime\": \"14:14:37\", \"updated\": \"2021-07-13 12:10:21\"}, \"m7376\": {\"id\": \"m7376\", \"cpu\": 100.0, \"mem\": 36.9140625, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:39:11\", \"updated\": \"2021-07-13 12:10:23\"}, \"m7377\": {\"id\": \"m7377\", \"cpu\": 100.0, \"mem\": 36.9140625, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:39:13\", \"updated\": \"2021-07-13 12:10:24\"}, \"m7378\": {\"id\": \"m7378\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"MHD_6mg\", \"state\": \"idle\", \"runtime\": \"17:39:14\", \"updated\": \"failed\"}, \"m7379\": {\"id\": \"m7379\", \"cpu\": 96.43, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:56:22\", \"updated\": \"2021-07-13 12:10:28\"}, \"m7380\": {\"id\": \"m7380\", \"cpu\": 96.71, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:56:23\", \"updated\": \"2021-07-13 12:10:29\"}, \"m7381\": {\"id\": \"m7381\", \"cpu\": 96.43, \"mem\": 5.2734375, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:56:25\", \"updated\": \"2021-07-13 12:10:31\"}, \"m7382\": {\"id\": \"m7382\", \"cpu\": 96.5, \"mem\": 5.6640625, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:56:27\", \"updated\": \"2021-07-13 12:10:33\"}, \"m7383\": {\"id\": \"m7383\", \"cpu\": 96.57, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:56:28\", \"updated\": \"2021-07-13 12:10:34\"}, \"m7384\": {\"id\": \"m7384\", \"cpu\": 96.54, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:56:30\", \"updated\": \"2021-07-13 12:10:36\"}, \"m7385\": {\"id\": \"m7385\", \"cpu\": 96.43, \"mem\": 5.46875, \"job\": \"3Donec51\", \"state\": \"idle\", \"runtime\": \"1-21:56:32\", \"updated\": \"2021-07-13 12:10:38\"}, \"m7386\": {\"id\": \"m7386\", \"cpu\": 100.0, \"mem\": 9.5703125, \"job\": \"seed1\", \"state\": \"idle\", \"runtime\": \"2-12:10:19\", \"updated\": \"2021-07-13 12:10:39\"}, \"m7387\": {\"id\": \"m7387\", \"cpu\": 100.0, \"mem\": 9.765625, \"job\": \"seed1\", \"state\": \"idle\", \"runtime\": \"2-12:10:21\", \"updated\": \"2021-07-13 12:10:41\"}, \"m7388\": {\"id\": \"m7388\", \"cpu\": 51.57, \"mem\": 49.4140625, \"job\": \"swift\", \"state\": \"idle\", \"runtime\": \"2-00:20:30\", \"updated\": \"2021-07-13 12:10:43\"}, \"m7389\": {\"id\": \"m7389\", \"cpu\": 19.57, \"mem\": 20.5078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:44\"}, \"m7390\": {\"id\": \"m7390\", \"cpu\": 23.21, \"mem\": 30.078125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:46\"}, \"m7391\": {\"id\": \"m7391\", \"cpu\": 23.68, \"mem\": 28.3203125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:47\"}, \"m7392\": {\"id\": \"m7392\", \"cpu\": 47.46, \"mem\": 43.75, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:10:49\"}, \"m7393\": {\"id\": \"m7393\", \"cpu\": 56.29, \"mem\": 9.375, \"job\": \"SPT-0532\", \"state\": \"idle\", \"runtime\": \"2-00:22:09\", \"updated\": \"2021-07-13 12:10:51\"}, \"m7394\": {\"id\": \"m7394\", \"cpu\": 100.0, \"mem\": 60.7421875, \"job\": \"low_low_\", \"state\": \"idle\", \"runtime\": \"17:01:52\", \"updated\": \"2021-07-13 12:10:52\"}, \"m7395\": {\"id\": \"m7395\", \"cpu\": 100.0, \"mem\": 62.890625, \"job\": \"low_low_\", \"state\": \"idle\", \"runtime\": \"17:01:54\", \"updated\": \"2021-07-13 12:10:54\"}, \"m7396\": {\"id\": \"m7396\", \"cpu\": 100.07, \"mem\": 62.5, \"job\": \"low_low_\", \"state\": \"idle\", \"runtime\": \"17:01:56\", \"updated\": \"2021-07-13 12:10:56\"}, \"m7397\": {\"id\": \"m7397\", \"cpu\": 86.43, \"mem\": 15.0390625, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:45\", \"updated\": \"2021-07-13 12:10:57\"}, \"m7398\": {\"id\": \"m7398\", \"cpu\": 87.32, \"mem\": 14.6484375, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:47\", \"updated\": \"2021-07-13 12:10:59\"}, \"m7399\": {\"id\": \"m7399\", \"cpu\": 99.54, \"mem\": 14.84375, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:49\", \"updated\": \"2021-07-13 12:11:01\"}, \"m7400\": {\"id\": \"m7400\", \"cpu\": 94.79, \"mem\": 14.6484375, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:50\", \"updated\": \"2021-07-13 12:11:02\"}, \"m7401\": {\"id\": \"m7401\", \"cpu\": 85.21, \"mem\": 15.4296875, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:52\", \"updated\": \"2021-07-13 12:11:04\"}, \"m7402\": {\"id\": \"m7402\", \"cpu\": 83.96, \"mem\": 14.84375, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:54\", \"updated\": \"2021-07-13 12:11:05\"}, \"m7403\": {\"id\": \"m7403\", \"cpu\": 89.14, \"mem\": 15.4296875, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:55\", \"updated\": \"2021-07-13 12:11:07\"}, \"m7404\": {\"id\": \"m7404\", \"cpu\": 84.64, \"mem\": 15.234375, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:57\", \"updated\": \"2021-07-13 12:11:09\"}, \"m7405\": {\"id\": \"m7405\", \"cpu\": 83.79, \"mem\": 15.4296875, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:39:58\", \"updated\": \"2021-07-13 12:11:10\"}, \"m7406\": {\"id\": \"m7406\", \"cpu\": 75.32, \"mem\": 15.4296875, \"job\": \"run0035I\", \"state\": \"idle\", \"runtime\": \"17:40:00\", \"updated\": \"2021-07-13 12:11:12\"}, \"m7407\": {\"id\": \"m7407\", \"cpu\": 101.25, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:46:52\", \"updated\": \"2021-07-13 12:11:14\"}, \"m7408\": {\"id\": \"m7408\", \"cpu\": 101.18, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:46:53\", \"updated\": \"2021-07-13 12:11:15\"}, \"m7409\": {\"id\": \"m7409\", \"cpu\": 101.07, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:46:55\", \"updated\": \"2021-07-13 12:11:17\"}, \"m7410\": {\"id\": \"m7410\", \"cpu\": 100.61, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:46:57\", \"updated\": \"2021-07-13 12:11:19\"}, \"m7411\": {\"id\": \"m7411\", \"cpu\": 100.64, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:46:58\", \"updated\": \"2021-07-13 12:11:20\"}, \"m7412\": {\"id\": \"m7412\", \"cpu\": 100.86, \"mem\": 34.9609375, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:47:00\", \"updated\": \"2021-07-13 12:11:22\"}, \"m7413\": {\"id\": \"m7413\", \"cpu\": 101.04, \"mem\": 34.765625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:47:02\", \"updated\": \"2021-07-13 12:11:24\"}, \"m7414\": {\"id\": \"m7414\", \"cpu\": 101.0, \"mem\": 34.765625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:47:03\", \"updated\": \"2021-07-13 12:11:25\"}, \"m7415\": {\"id\": \"m7415\", \"cpu\": 22.46, \"mem\": 20.1171875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:27\"}, \"m7416\": {\"id\": \"m7416\", \"cpu\": 28.61, \"mem\": 30.2734375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:28\"}, \"m7417\": {\"id\": \"m7417\", \"cpu\": 27.96, \"mem\": 28.125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:30\"}, \"m7418\": {\"id\": \"m7418\", \"cpu\": 42.25, \"mem\": 45.1171875, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:32\"}, \"m7419\": {\"id\": \"m7419\", \"cpu\": 15.79, \"mem\": 19.53125, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:33\"}, \"m7420\": {\"id\": \"m7420\", \"cpu\": 26.25, \"mem\": 28.90625, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:35\"}, \"m7421\": {\"id\": \"m7421\", \"cpu\": 25.29, \"mem\": 27.34375, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:37\"}, \"m7422\": {\"id\": \"m7422\", \"cpu\": 37.0, \"mem\": 43.75, \"job\": \"None\", \"state\": \"idle\", \"runtime\": \"n/a\", \"updated\": \"2021-07-13 12:11:38\"}, \"m7423\": {\"id\": \"m7423\", \"cpu\": 100.0, \"mem\": 37.890625, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:33:48\", \"updated\": \"2021-07-13 12:11:40\"}, \"m7424\": {\"id\": \"m7424\", \"cpu\": 100.0, \"mem\": 37.6953125, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:33:51\", \"updated\": \"2021-07-13 12:11:43\"}, \"m7425\": {\"id\": \"m7425\", \"cpu\": 100.0, \"mem\": 37.890625, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:33:52\", \"updated\": \"2021-07-13 12:11:44\"}, \"m7426\": {\"id\": \"m7426\", \"cpu\": 0.0, \"mem\": 0.0, \"job\": \"MHD_lowZ\", \"state\": \"idle\", \"runtime\": \"17:33:54\", \"updated\": \"failed\"}, \"m7427\": {\"id\": \"m7427\", \"cpu\": 92.86, \"mem\": 25.78125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:05:09\", \"updated\": \"2021-07-13 12:11:47\"}, \"m7428\": {\"id\": \"m7428\", \"cpu\": 92.86, \"mem\": 25.78125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:05:10\", \"updated\": \"2021-07-13 12:11:49\"}, \"m7429\": {\"id\": \"m7429\", \"cpu\": 92.86, \"mem\": 25.78125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:05:12\", \"updated\": \"2021-07-13 12:11:51\"}, \"m7430\": {\"id\": \"m7430\", \"cpu\": 93.14, \"mem\": 25.78125, \"job\": \"CRiMHD+S\", \"state\": \"idle\", \"runtime\": \"12:05:13\", \"updated\": \"2021-07-13 12:11:52\"}, \"m7431\": {\"id\": \"m7431\", \"cpu\": 101.04, \"mem\": 34.765625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:47:32\", \"updated\": \"2021-07-13 12:11:54\"}, \"m7432\": {\"id\": \"m7432\", \"cpu\": 100.36, \"mem\": 34.765625, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:47:34\", \"updated\": \"2021-07-13 12:11:56\"}, \"m7433\": {\"id\": \"m7433\", \"cpu\": 100.39, \"mem\": 34.5703125, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:47:35\", \"updated\": \"2021-07-13 12:11:57\"}, \"m7434\": {\"id\": \"m7434\", \"cpu\": 100.64, \"mem\": 34.5703125, \"job\": \"m200n204\", \"state\": \"idle\", \"runtime\": \"2-12:47:37\", \"updated\": \"2021-07-13 12:11:59\"}, \"m7435\": {\"id\": \"m7435\", \"cpu\": 96.39, \"mem\": 15.625, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:40:49\", \"updated\": \"2021-07-13 12:12:01\"}, \"m7436\": {\"id\": \"m7436\", \"cpu\": 96.68, \"mem\": 15.4296875, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:40:50\", \"updated\": \"2021-07-13 12:12:02\"}, \"m7437\": {\"id\": \"m7437\", \"cpu\": 97.79, \"mem\": 15.625, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:40:52\", \"updated\": \"2021-07-13 12:12:04\"}, \"m7438\": {\"id\": \"m7438\", \"cpu\": 98.21, \"mem\": 16.015625, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:40:54\", \"updated\": \"2021-07-13 12:12:05\"}, \"m7439\": {\"id\": \"m7439\", \"cpu\": 97.75, \"mem\": 15.8203125, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:40:55\", \"updated\": \"2021-07-13 12:12:07\"}, \"m7440\": {\"id\": \"m7440\", \"cpu\": 96.54, \"mem\": 15.4296875, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:40:57\", \"updated\": \"2021-07-13 12:12:09\"}, \"m7441\": {\"id\": \"m7441\", \"cpu\": 97.82, \"mem\": 15.4296875, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:40:58\", \"updated\": \"2021-07-13 12:12:10\"}, \"m7442\": {\"id\": \"m7442\", \"cpu\": 92.21, \"mem\": 14.453125, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:00\", \"updated\": \"2021-07-13 12:12:12\"}, \"m7443\": {\"id\": \"m7443\", \"cpu\": 95.43, \"mem\": 14.6484375, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:02\", \"updated\": \"2021-07-13 12:12:14\"}, \"m7444\": {\"id\": \"m7444\", \"cpu\": 93.04, \"mem\": 14.453125, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:03\", \"updated\": \"2021-07-13 12:12:15\"}, \"m7445\": {\"id\": \"m7445\", \"cpu\": 98.39, \"mem\": 14.84375, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:05\", \"updated\": \"2021-07-13 12:12:17\"}, \"m7446\": {\"id\": \"m7446\", \"cpu\": 93.54, \"mem\": 14.84375, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:07\", \"updated\": \"2021-07-13 12:12:19\"}, \"m7447\": {\"id\": \"m7447\", \"cpu\": 92.39, \"mem\": 15.0390625, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:08\", \"updated\": \"2021-07-13 12:12:20\"}, \"m7448\": {\"id\": \"m7448\", \"cpu\": 78.14, \"mem\": 15.234375, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:10\", \"updated\": \"2021-07-13 12:12:22\"}, \"m7449\": {\"id\": \"m7449\", \"cpu\": 90.46, \"mem\": 15.625, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:12\", \"updated\": \"2021-07-13 12:12:23\"}, \"m7450\": {\"id\": \"m7450\", \"cpu\": 84.25, \"mem\": 15.4296875, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:13\", \"updated\": \"2021-07-13 12:12:25\"}, \"m7451\": {\"id\": \"m7451\", \"cpu\": 57.89, \"mem\": 15.8203125, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:15\", \"updated\": \"2021-07-13 12:12:27\"}, \"m7452\": {\"id\": \"m7452\", \"cpu\": 35.86, \"mem\": 15.8203125, \"job\": \"run0028I\", \"state\": \"idle\", \"runtime\": \"17:41:16\", \"updated\": \"2021-07-13 12:12:28\"}}";
        return stringpart1 + stringpart2;
    }


}
