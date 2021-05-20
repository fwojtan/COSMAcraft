package net.fwojtan.cosmacraft.common.tileentity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.common.utils.RackConfig;
import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModBlocks;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static net.fwojtan.cosmacraft.common.block.ParentBlock.FACING;
import static net.fwojtan.cosmacraft.common.block.RackBlock.SHOULD_RENDER;

public class CosmaControlTileEntity extends ParentTileEntity{

    public CosmaControlTileEntity(TileEntityType<?> type){super(type);}
    public CosmaControlTileEntity(){this(ModTileEntities.COSMA_CONTROL_TILE_ENTITY.get());}

    @Override
    public void tick() {
        if(!childrenPlaced){

                placeChildren();

            childrenPlaced=true;
        }

    }

    @Override
    public void placeChildren() {

        childPositionList = new ArrayList<>();

        parentDirection = getBlockState().getValue(FACING);
        List<RackConfig> rackConfigList;
        Type rackConfigType = new TypeToken<ArrayList<RackConfig>>(){}.getType();
        Path configLocation = FMLPaths.getOrCreateGameRelativePath(Paths.get("util/cosma_config/"), "cosma_config_path");

        try {
            File file = new File(configLocation.toString() + "/cosma_config.json");

            // generate json in the right location if it doesn't exist
            if (file.createNewFile()) {
                System.out.println("Created: "+file.getName()+" at "+file.getPath());
                FileWriter writer = new FileWriter(file.getPath());
                writer.write(createDefaultConfigJson());
                writer.close();
            } else {System.out.println("File already exists");

                // read json
                Reader reader = Files.newBufferedReader(Paths.get(file.getPath()));
                rackConfigList = new Gson().fromJson(reader, rackConfigType);
                reader.close();

                for (RackConfig rackConfig : rackConfigList) {
                    BlockPos childPos = getChildPosition(new Vector3i(rackConfig.x, rackConfig.y, rackConfig.z));
                    BlockState childState = ModBlocks.RACK_BLOCK.get().defaultBlockState().setValue(FACING, Direction.valueOf(rackConfig.facing))
                            .setValue(SHOULD_RENDER, false);

                    // we only create these things on the server side
                    if (!this.level.isClientSide()) {
                        // place racks in required positions
                        getLevel().setBlock(childPos, childState, 3);

                        // feed the tile entity its required data
                        RackTileEntity rackTileEntity = (RackTileEntity) getLevel().getBlockEntity(childPos);
                        rackTileEntity.setControllerPosition(getBlockPos());
                        rackTileEntity.serverTypes = this.createServerList(rackConfig.servers);
                        rackTileEntity.parentDirection = childState.getValue(FACING);
                        rackTileEntity.setListInitialized(true);

                    }

                    // and then after they're created we mark them for an update on the render thread
                    getLevel().sendBlockUpdated(childPos, childState, childState, 2);
                    childPositionList.add(childPos);
                }

            }
        } catch (IOException e){
            e.printStackTrace();
        }

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


}
