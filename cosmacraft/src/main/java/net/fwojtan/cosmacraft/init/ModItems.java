package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.common.CosmaCraftItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class ModItems {

    public static final RegistryObject<Item> INFO_TOOL = Registration.ITEMS.register("info_tool", () -> new Item(new Item.Properties().tab(Registration.cosmaCraftItemGroup)));
    public static final RegistryObject<Item> EJECT_TOOL = Registration.ITEMS.register("eject_tool", () -> new Item(new Item.Properties().tab(Registration.cosmaCraftItemGroup)));
    public static final RegistryObject<Item> DATA_TOOL = Registration.ITEMS.register("data_tool", () -> new Item(new Item.Properties().tab(Registration.cosmaCraftItemGroup)));
    public static final RegistryObject<Item> DOOR_TOOL = Registration.ITEMS.register("door_tool", () -> new Item(new Item.Properties().tab(Registration.cosmaCraftItemGroup)));
    public static final RegistryObject<Item> CABLE_TOGGLE_TOOL = Registration.ITEMS.register("cable_toggle", () -> new Item(new Item.Properties().tab(Registration.cosmaCraftItemGroup)));
    public static final RegistryObject<Item> MEMORY_CHIP = Registration.ITEMS.register("memory_chip", () -> new Item(new Item.Properties().tab(Registration.cosmaCraftItemGroup)));
    static void register() {}

}
