package net.fwojtan.cosmacraft.common.utils;

import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.IStringSerializable;


import java.util.*;

import static net.fwojtan.cosmacraft.common.block.ServerBlock.RENDER_CHOICE;

public enum ServerType implements IStringSerializable {
    ONE_U_GAP(0, "one_u_gap", 1, false, Collections.emptyList()),
    TWO_U_HEX(1, "two_u_hex", 2, true, Collections.emptyList()),
    ONE_U_HORIZONTAL_DRIVES(2, "one_u_horizontal_drives", 1, true, Collections.emptyList()),
    C_6525(3, "c_6525", 2, true, Collections.emptyList()),
    ME_484(4, "me484", 4, true, Collections.emptyList()),
    MD_3420(5, "md3420", 4, true, Collections.emptyList()),
    R740_HEX_FLAT(6, "r740_hex_flat", 2, true, Collections.emptyList()),
    MELLANOX_EDR(7, "mellanox_edr_switch", 1, true, Arrays.asList(
            new CableProperties(0.75, 0.075, 0.15, 0.95, 0.2, 0.05, 0.5f, 0.5f, 0.5f),
            new CableProperties(0.25, 0.075, 0.15, 0.05, 0.2, 0.05, 0.5f, 0.5f, 0.5f))),

    // the below need adding
    MAD_03(8, "mad_03_server", 2, false, Collections.emptyList()),
    ETH_SWITCH(9, "ethernet_switch", 1, false, Collections.emptyList()),
    DELL_PSU(10, "dell_psu", 1, false, Collections.emptyList()),
    HANDLEBAR_2U(11, "handlebar_2u", 2, false, Collections.emptyList()),
    ME_484_2U(12, "me484_2u", 2, false, Collections.emptyList()),
    LCDKVM(13, "lcdkvm", 1, false, Collections.emptyList()),
    DOUBLE_SQUARES(14, "double_squares", 2, false, Collections.emptyList()),
    FOUR_U_GAP(15, "four_u_gap", 4, false, Collections.emptyList()),
    ONE_U_HEX(16, "one_u_hex", 1, false, Collections.emptyList()),
    THREE_U_HEX(17, "three_u_hex", 3, false, Collections.emptyList()),
    MELLANOX_QUANT(18, "mellanox_quantum_switch", 1, false, Collections.emptyList()),
    R_6525(19, "r6525", 1, false, Collections.emptyList()),
    C8_SWITCH(27, "ethernet_switch_c8", 1, false, Collections.emptyList()),

    // I don't even know what the below are...
    ATEMPO(20, "atempo", 1, false, Collections.emptyList()), //R640 (look the same as the R6525)
    C7_FIBRE_SWITCH(21, "fibre_switch", 1, false, Collections.emptyList()),
    FOUR_U_MI50(22, "mi50", 4, false, Collections.emptyList()), //Gigabyte server
    TWO_U_GA_SERVER(23, "two_u_ga_server", 2, false, Collections.emptyList()), //7525s
    FOUR_U_CIRCLES(24, "four_u_circles", 4, false, Collections.emptyList()),
    GREY_4U_SERVER(25, "grey_4u_server", 4, false, Collections.emptyList()),
    LOGIN_NODE_C8(26, "login_node_c8", 1, false, Collections.emptyList());

    private final String name;
    private final int uHeight;
    public boolean shouldRender;
    private int index;
    public List<CableProperties> frontCableList;

    private ServerType(int index, String name, int uHeight, boolean shouldRender, List<CableProperties> frontCablePropertiesList){
        this.name = name;
        this.uHeight = uHeight;
        this.shouldRender = shouldRender;
        this.index = index;
        this.frontCableList = frontCablePropertiesList;
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
