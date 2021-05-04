package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.common.tileentity.ChildTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.ParentTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class ModTileEntities {

    public static final RegistryObject<TileEntityType<ChildTileEntity>> CHILD_TILE_ENTITY
            = Registration.TILE_ENTITY.register("dummy_block", () ->
            TileEntityType.Builder.of(ChildTileEntity::new,
                    ModBlocks.DUMMY_BLOCK.get()).build(null));

    public static final RegistryObject<TileEntityType<ParentTileEntity>> PARENT_TILE_ENTITY
            = Registration.TILE_ENTITY.register("parent_block", () ->
            TileEntityType.Builder.of(ParentTileEntity::new,
                    ModBlocks.DUMMY_BLOCK.get()).build(null));

    static void register() {}
}
