package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.common.tileentity.ChildTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.CosmaControlTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.ParentTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.RackTileEntity;
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
                    ModBlocks.PARENT_BLOCK.get()).build(null));

    public static final RegistryObject<TileEntityType<RackTileEntity>> RACK_TILE_ENTITY
            = Registration.TILE_ENTITY.register("rack_block", () ->
            TileEntityType.Builder.of(RackTileEntity::new,
                    ModBlocks.RACK_BLOCK.get()).build(null));

    public static final RegistryObject<TileEntityType<CosmaControlTileEntity>> COSMA_CONTROL_TILE_ENTITY
            = Registration.TILE_ENTITY.register("cosma_control_block", () ->
            TileEntityType.Builder.of(CosmaControlTileEntity::new,
                    ModBlocks.COSMA_CONTROL_BLOCK.get()).build(null));

    static void register() {}
}
