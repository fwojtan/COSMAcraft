package net.fwojtan.cosmacraft.common;

import net.fwojtan.cosmacraft.init.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class CosmaCraftItemGroup extends ItemGroup {


    public CosmaCraftItemGroup(String label) {
        super(label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(ModItems.DATA_TOOL.get());
    }
}
