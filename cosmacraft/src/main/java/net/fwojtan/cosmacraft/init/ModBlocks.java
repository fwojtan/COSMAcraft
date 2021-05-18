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

    public static final RegistryObject<Block> DUMMY_BLOCK = register("dummy_block", () -> new DummyBlock());
    public static final RegistryObject<Block> PARENT_BLOCK = register("parent_block", () -> new ParentBlock());
    public static final RegistryObject<Block> RACK_BLOCK = register("rack_block", () -> new RackBlock());
    public static final RegistryObject<Block> COSMA_CONTROL_BLOCK = register("cosma_control_block", () -> new CosmaControlBlock());
    public static final RegistryObject<Block> SERVER_MODEL_BLOCK = registerNoItem("server_model_block", () -> new ServerBlock());


    static void register() {}

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> block){
        return Registration.BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> block) {
        RegistryObject<T> ret = registerNoItem(name, block);
        Registration.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));
        return ret;
    }
}
