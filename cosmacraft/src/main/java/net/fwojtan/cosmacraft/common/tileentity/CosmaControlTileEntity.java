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
                    System.out.println(rackConfig.id);
                    BlockPos childPos = getChildPosition(new Vector3i(rackConfig.x, rackConfig.y, rackConfig.z));
                    BlockState childState = ModBlocks.RACK_BLOCK.get().defaultBlockState().setValue(FACING, Direction.valueOf(rackConfig.facing))
                            .setValue(SHOULD_RENDER, true);
                    System.out.println(childPos);

                    // we only create these things on the server side
                    if (!this.level.isClientSide()) {
                        // place racks in required positions
                        getLevel().setBlock(childPos, childState, 3);

                        // feed the tile entity its required data
                        RackTileEntity rackTileEntity = (RackTileEntity) getLevel().getBlockEntity(childPos);
                        rackTileEntity.setControllerPosition(getBlockPos());
                        rackTileEntity.serverTypes = this.createServerList(rackConfig.servers);
                        System.out.println(rackTileEntity.serverTypes);
                        rackTileEntity.setListInitialized(true);
                        System.out.println(rackTileEntity.getListInitialized());
                    }

                    // and then after they're created we mark them for an update on the render thread
                    getLevel().sendBlockUpdated(childPos, childState, childState, 2);

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
            System.out.println("Converted string "+server+" to enum val with name "+ServerType.valueOf(server).name());
        }
        return retList;
    }


    @Override
    public List<BlockPos> createChildPositonList() {



        return super.createChildPositonList();
    }



    private String createDefaultConfigJson(){
        return "[\n" +
                "  {\n" +
                "    \"id\": \"H1\",\n" +
                "    \"x\": 1,\n" +
                "    \"y\": 1,\n" +
                "    \"z\": 1,\n" +
                "    \"facing\": \"NORTH\",\n" +
                "    \"servers\": [\"ME_484\", \"MD_3420\", \"C_6525\"],\n" +
                "    \"frontDoor\": \"none\",\n" +
                "    \"backDoor\": \"none\",\n" +
                "    \"rackStyle\": \"cosma7\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"id\": \"H2\",\n" +
                "    \"x\": 2,\n" +
                "    \"y\": 1,\n" +
                "    \"z\": 1,\n" +
                "    \"facing\": \"NORTH\",\n" +
                "    \"servers\": [\"ONE_U_GAP\", \"TWO_U_HEX\"],\n" +
                "    \"frontDoor\": \"none\",\n" +
                "    \"backDoor\": \"none\",\n" +
                "    \"rackStyle\": \"cosma7\"\n" +
                "  }\n" +
                "]";
    }


}
