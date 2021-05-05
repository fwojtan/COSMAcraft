package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.common.tileentity.ChildTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.ParentTileEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CosmaCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandler {

    @SubscribeEvent
    public static void setupRenderLayers(FMLClientSetupEvent event) {
        System.out.println("Fin's registry event happened");
        RenderTypeLookup.setRenderLayer(ModBlocks.DUMMY_BLOCK.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public void onBlockBroken(BlockEvent.BreakEvent event){

        if (event.getWorld().getBlockEntity(event.getPos()) instanceof ParentTileEntity){
            IWorld world = event.getWorld();
            ParentTileEntity entity = (ParentTileEntity)  event.getWorld().getBlockEntity(event.getPos());

            for (BlockPos pos : entity.getChildPositionList()){
                world.destroyBlock(pos, false);
            }

        }

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



}
