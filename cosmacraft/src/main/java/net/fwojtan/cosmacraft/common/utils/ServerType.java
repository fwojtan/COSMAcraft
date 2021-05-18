package net.fwojtan.cosmacraft.common.utils;

import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.IStringSerializable;


import static net.fwojtan.cosmacraft.common.block.ServerBlock.RENDER_CHOICE;

public enum ServerType implements IStringSerializable {
    ONE_U_GAP(0, "one_u_gap", 1, false),
    TWO_U_HEX(1, "two_u_hex", 2, true),
    ONE_U_HORIZONTAL_DRIVES(2, "one_u_horizontal_drives", 1, true),
    C_6525(3, "c_6525", 2, true),
    ME_484(4, "me484", 4, true),
    MD_3420(5, "md3420", 4, true),
    R740_HEX_FLAT(6, "r740_hex_flat", 2, true),
    MELLANOX_EDR(7, "mellanox_edr_switch", 1, true);

    private final String name;
    private final int uHeight;
    public boolean shouldRender;
    private int index;

    private ServerType(int index, String name, int uHeight, boolean shouldRender){
        this.name = name;
        this.uHeight = uHeight;
        this.shouldRender = shouldRender;
        this.index = index;
    }

    @Override
    public String getSerializedName() {return this.name;}

    public int getUHeight() {return this.uHeight;}

    public IBakedModel getModel(){return Minecraft.getInstance().getBlockRenderer().getBlockModel(ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this));}

    public BlockState getState(){return ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this);}

    public int getIndex(){return this.index;}

    public static ServerType getTypeFromIndex(int index){
        for (ServerType serverType : ServerType.values()){
            if (serverType.index == index){
                return serverType;
            }
        } return null;
    }
}
