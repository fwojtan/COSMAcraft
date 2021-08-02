package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.client.ter.RackTileEntityRenderer;
import net.fwojtan.cosmacraft.common.tileentity.ChildTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.ParentTileEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CosmaCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandler {

    @SubscribeEvent
    public static void setupRenderLayers(FMLClientSetupEvent event) {
        System.out.println("Fin's registry event happened");
        RenderTypeLookup.setRenderLayer(ModBlocks.DUMMY_BLOCK.get(), RenderType.cutout());

        ClientRegistry.bindTileEntityRenderer(ModTileEntities.RACK_TILE_ENTITY.get(), RackTileEntityRenderer::new);

        //ResourceLocation modelLocation = new ResourceLocation(CosmaCraft.MOD_ID, "models/block/basic_rack.obj");

        //ResourceLocation materialLocation = new ResourceLocation(CosmaCraft.MOD_ID, "models/block");
        //OBJModel.ModelSettings settings = new OBJModel.ModelSettings(modelLocation, true, true, false, false, null);
        //OBJLoader.INSTANCE.loadModel(settings);
        //OBJLoader.INSTANCE.loadMaterialLibrary(materialLocation);
    }

    @SubscribeEvent
    public void onBlockBroken(BlockEvent.BreakEvent event){

        checkAndDestroyParent(event.getWorld(), event.getPos());

        if (event.getWorld().getBlockEntity(event.getPos()) instanceof ChildTileEntity){
            IWorld world = event.getWorld();
            ChildTileEntity childTileEntity = (ChildTileEntity)  event.getWorld().getBlockEntity(event.getPos());

            ParentTileEntity parentTileEntity = (ParentTileEntity)  event.getWorld().getBlockEntity(childTileEntity.parentPosition);

            for (BlockPos pos : parentTileEntity.getChildPositionList()){
                world.destroyBlock(pos, false);
            }
            world.destroyBlock(childTileEntity.parentPosition, true);
        }
    }


    private void checkAndDestroyParent(IWorld world, BlockPos pos){
        if (world.getBlockEntity(pos) instanceof ParentTileEntity){
            ParentTileEntity entity = (ParentTileEntity)  world.getBlockEntity(pos);
            if (entity.getChildPositionList() != null){
                for (BlockPos childPos : entity.getChildPositionList()){
                    checkAndDestroyParent(world, childPos);
                    world.destroyBlock(childPos, false);
                }
            }

        }
    }



}
