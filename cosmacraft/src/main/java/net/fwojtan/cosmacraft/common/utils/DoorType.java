package net.fwojtan.cosmacraft.common.utils;

import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.IStringSerializable;

import java.util.List;

import static net.fwojtan.cosmacraft.common.block.DoorBlock.RENDER_CHOICE;

public enum DoorType implements IStringSerializable {

    C7_BACK(0, "cosma7", true),
    C8_BACK(1, "cosma8", false),
    C6_BACK(2, "cosma6", false),
    STORAGE_BACK(3, "storage", false),
    NONE(4, "none", false);

    private final String name;
    public boolean shouldRender;
    private int index;

    private DoorType(int index, String name, boolean shouldRender){
        this.name = name;
        this.shouldRender = shouldRender;
        this.index = index;
    }

    @Override
    public String getSerializedName() {return this.name;}

    public IBakedModel getModel() {return Minecraft.getInstance().getBlockRenderer().getBlockModel(ModBlocks.COSMA_DOOR_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this));    }

    public BlockState getState() {return ModBlocks.COSMA_DOOR_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this);}

    public int getIndex(){return this.index;}

    public static DoorType getTypeFromIndex(int index){
        for (DoorType doorType : DoorType.values()){
            if (doorType.index == index){
                return doorType;
            }
        } return null;
    }
}
