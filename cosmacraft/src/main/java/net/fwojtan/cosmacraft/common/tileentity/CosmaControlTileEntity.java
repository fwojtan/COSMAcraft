package net.fwojtan.cosmacraft.common.tileentity;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.common.utils.RackConfig;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CosmaControlTileEntity extends ParentTileEntity{

    public CosmaControlTileEntity(TileEntityType<?> type){super(type);}
    public CosmaControlTileEntity(){this(ModTileEntities.COSMA_CONTROL_TILE_ENTITY.get());}

    @Override
    public void tick() {
        if(!childrenPlaced){
            if (!this.level.isClientSide()){
                placeChildren();
            }
            childrenPlaced=true;
        }

    }

    @Override
    public void placeChildren() {

        List<RackConfig> rackConfigList;



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
                RackConfig rackConfig = new Gson().fromJson(reader, RackConfig.class);
                reader.close();
                System.out.println(rackConfig.id);

            }
        } catch (IOException e){
            e.printStackTrace();
        }










/*
        try {
            //Reader reader = Files.newBufferedReader(FMLPaths.getOrCreateGameRelativePath(Paths.get("util/cosma_config.json"), "rack_config_location"));

            InputStream resource = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
            String contents = new Gson().fromJson(String.valueOf(resource),String.class);
            System.out.println(contents);


        } catch (IOException e) {
            e.printStackTrace();
        }

*/
        // iterate through top level list

        // place racks with position/direction

        // set RackTE serverType List

        // set other properties (e.g. doors)



    }

    @Override
    public List<BlockPos> createChildPositonList() {



        return super.createChildPositonList();
    }





    private String createDefaultConfigJson(){
        return "{\n" +
                "  \"id\": \"H1\",\n" +
                "  \"x\": 1,\n" +
                "  \"y\": 1,\n" +
                "  \"z\": 1,\n" +
                "  \"facing\": \"north\",\n" +
                "  \"servers\": [0, 1, 2, 3],\n" +
                "  \"frontDoor\": \"none\",\n" +
                "  \"backDoor\": \"none\",\n" +
                "  \"rackStyle\": \"cosma7\"\n" +
                "}";
    }


}
