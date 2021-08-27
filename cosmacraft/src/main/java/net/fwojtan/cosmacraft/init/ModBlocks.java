package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.common.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final RegistryObject<Block> DUMMY_BLOCK = registerNoItem("dummy_block", () -> new DummyBlock());
    public static final RegistryObject<Block> PARENT_BLOCK = registerNoItem("parent_block", () -> new ParentBlock());
    public static final RegistryObject<Block> RACK_BLOCK = registerNoItem("rack_block", () -> new RackBlock());
    public static final RegistryObject<Block> COSMA_CONTROL_BLOCK = register("cosma_control_block", () -> new CosmaControlBlock());
    public static final RegistryObject<Block> SERVER_MODEL_BLOCK = registerNoItem("server_model_block", () -> new ServerBlock());
    public static final RegistryObject<Block> COSMA_DOOR_BLOCK = registerNoItem("cosma_door_block", () -> new DoorBlock());
    public static final RegistryObject<Block> LIGHT_BLOCK = register("light_block", () -> new LightBlock());
    public static final RegistryObject<Block> SMALL_POWER_BLOCK = register("small_power_block", () -> new SmallPowerBlock());
    public static final RegistryObject<Block> LARGE_POWER_BLOCK = register("large_power_block", () -> new LargePowerBlock());
    public static final RegistryObject<Block> EMPTY_RACK_BLOCK = register("empty_rack_block", () -> new EmptyRackBlock());
    public static final RegistryObject<Block> ARCHIVE_BLOCK = register("archive_block", () -> new ArchiveBlock());
    public static final RegistryObject<Block> PIPE_BLOCK = register("pipe_block", () -> new PipeBlock());
    public static final RegistryObject<Block> EXCHANGER_BLOCK = register("exchanger_block", () -> new ExchangerBlock());


    static void register() {}

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> block){
        return Registration.BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> ret = registerNoItem(name, block);
        Registration.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(Registration.cosmaCraftItemGroup)));
        return ret;
    }
}
