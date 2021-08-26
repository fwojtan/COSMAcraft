package net.fwojtan.cosmacraft.init;

import net.fwojtan.cosmacraft.common.tileentity.*;
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

    public static final RegistryObject<TileEntityType<SmallPowerTileEntity>> SMALL_POWER_TILE_ENTITY
            = Registration.TILE_ENTITY.register("small_power_block", () ->
            TileEntityType.Builder.of(SmallPowerTileEntity::new,
                    ModBlocks.SMALL_POWER_BLOCK.get()).build(null));

    public static final RegistryObject<TileEntityType<LargePowerTileEntity>> LARGE_POWER_TILE_ENTITY
            = Registration.TILE_ENTITY.register("large_power_block", () ->
            TileEntityType.Builder.of(LargePowerTileEntity::new,
                    ModBlocks.LARGE_POWER_BLOCK.get()).build(null));

    public static final RegistryObject<TileEntityType<EmptyRackTileEntity>> EMPTY_RACK_TILE_ENTITY
            = Registration.TILE_ENTITY.register("empty_rack_block", () ->
            TileEntityType.Builder.of(EmptyRackTileEntity::new,
                    ModBlocks.EMPTY_RACK_BLOCK.get()).build(null));

    public static final RegistryObject<TileEntityType<ArchiveTileEntity>> ARCHIVE_TILE_ENTITY
            = Registration.TILE_ENTITY.register("archive_block", () ->
            TileEntityType.Builder.of(ArchiveTileEntity::new,
                    ModBlocks.ARCHIVE_BLOCK.get()).build(null));

    static void register() {}
}
