package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.CosmaCraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
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
        System.out.println("A block was broken yay");
    }


}
