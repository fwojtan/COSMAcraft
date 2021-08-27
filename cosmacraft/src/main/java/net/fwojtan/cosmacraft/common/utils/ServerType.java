package net.fwojtan.cosmacraft.common.utils;

import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.IStringSerializable;


import java.util.*;

import static net.fwojtan.cosmacraft.common.block.ServerBlock.RENDER_CHOICE;

public enum ServerType implements IStringSerializable {
    ONE_U_GAP(0, "one_u_gap", "Empy space", "A gap between servers", 1, false, Collections.emptyList(), Collections.emptyList()),
    TWO_U_HEX(1, "two_u_hex", "General Purpose Dell Server","This server could be performing a number of tasks e.g. managing users, compiling code, managing storage systems or handling other administrative functions.",
            2, true, Collections.emptyList(), Arrays.asList(
            new CableProperties(0.75, 0.075, 1.05, 0.95, 0.2, 1.5, 0.8f, 0.3f, 0.5f),
            new CableProperties(0.25, 0.075, 1.05, 0.95, 0.2, 1.2, 0.8f, 0.3f, 0.5f),
            new CableProperties(0.85, 0.075, 1.05, 0.05, 1.8, 1.5, 0.4f, 0.4f, 0.4f),
            new CableProperties(0.5, 0.075, 1.05, 0.95, 0.2, 1.5, 0.3f, 0.3f, 0.6f)
            )),
    ONE_U_HORIZONTAL_DRIVES(2, "one_u_horizontal_drives", "Server with Drives", "This server could be used for different purposes, but most likely it is being used currently to provide disk space for COSMA's storage sysyem.",
            1, true, Collections.emptyList(), Arrays.asList(
            new CableProperties(0.15, 0.075, 1.05, 0.95, 2.4, 1.5, 0.8f, 0.1f, 0.1f),
            new CableProperties(0.4, 0.075, 1.05, 0.95, 0.2, 1.2, 0.4f, 0.3f, 0.3f),
            new CableProperties(0.25, 0.075, 1.05, 0.95, 1.4, 1.5, 0.4f, 0.4f, 0.4f),
            new CableProperties(0.5, 0.075, 1.05, 0.4, 0.2, 1.5, 0.4f, 0.8f, 0.5f)
    )),
    C_6525(3, "c_6525", "Compute Node Chassis", "This chassis houses four compute nodes. For COSMA8 these have very high performance AMD processors and a terabyte of memory.",
            2, true, Collections.emptyList(), Arrays.asList(
            new CableProperties(0.85, 0.075, 1.05, 0.05, 1.8, 1.5, 0.4f, 0.4f, 0.4f),
            new CableProperties(0.185, 0.075, 1.05, 0.95, 0.4, 1.5, 1.0f, 0.4f, 0.4f)
            )),
    ME_484(4, "me484", "Storage Server", "This server is used to provide disk space for COSMA's file system.",
            4, true, Collections.emptyList(), Collections.emptyList()),
    MD_3420(5, "md3420", "Storage Server", "This server is used to provide disk space for COSMA's file system.",
            4, true, Collections.emptyList(), Collections.emptyList()),
    R740_HEX_FLAT(6, "r740_hex_flat", "General Purpose Dell Server", "This server could be performing a number of tasks e.g. managing users, compiling code, managing storage systems or handling other administrative functions.",
            2, true, Collections.emptyList(), Collections.emptyList()), // this exists for direct comparison for benchmarking
    MELLANOX_EDR(7, "mellanox_edr_switch", "Mellanox Infiniband Switch (EDR)", "This switch has a very high bandwidth connection (100Gb/s) which is used to share data between compute nodes during simulations on COSMA7.",
            1, true, Arrays.asList(
            new CableProperties(0.75, 0.075, 0.15, 0.95, 0.2, 0.05, 0.5f, 0.5f, 0.5f),
            new CableProperties(0.25, 0.075, 0.15, 0.05, 0.2, 0.05, 0.5f, 0.5f, 0.5f)),
            Arrays.asList(
            new CableProperties(0.75, 0.075, 1.05, 0.95, 0.2, 0.05, 0.5f, 0.5f, 0.5f),
            new CableProperties(0.15, 0.075, 1.05, 0.95, 0.2, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.25, 0.075, 1.05, 0.95, 0.6, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.35, 0.075, 1.05, 0.95, 1.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.45, 0.075, 1.05, 0.95, 1.4, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.55, 0.075, 1.05, 0.95, 1.8, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.65, 0.075, 1.05, 0.95, 2.2, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.75, 0.075, 1.05, 0.95, 2.5, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.85, 0.075, 1.05, 0.95, 0.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.15, 0.1, 1.05, 0.05, 0.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.25, 0.1, 1.05, 0.05, 2.5, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.35, 0.1, 1.05, 0.05, 2.2, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.45, 0.1, 1.05, 0.05, 1.8, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.55, 0.1, 1.05, 0.05, 1.4, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.65, 0.1, 1.05, 0.05, 1.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.75, 0.1, 1.05, 0.05, 0.6, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.85, 0.1, 1.05, 0.05, 0.2, 1.5, 0.1f, 0.8f, 0.8f)
    )),

    // the below need adding
    MAD_03(8, "mad_03_server", "Mad Server", "This sever does something",
            2, true, Collections.emptyList(), Collections.emptyList()), // 7525 - same as TWO_U_GA_SERVER (LOOKS LIKE 2U HEX)
    ETH_SWITCH(9, "ethernet_switch", "Ethernet Switch", "This Dell ethernet switch is used to connect parts of COSMA's internal network. For example, login nodes with compute nodes and the management system.",
            1, true, Arrays.asList(
            new CableProperties(0.75, 0.1, 0.135, 0.05, 0.015, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.70, 0.1, 0.135, 0.05, 0.015, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.65, 0.1, 0.135, 0.05, 0.015, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.60, 0.1, 0.135, 0.05, 0.020, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.55, 0.1, 0.135, 0.05, 0.020, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.50, 0.1, 0.135, 0.05, 0.020, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.45, 0.1, 0.135, 0.05, 0.020, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.40, 0.1, 0.135, 0.05, 0.025, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.35, 0.1, 0.135, 0.05, 0.025, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.30, 0.1, 0.135, 0.05, 0.025, 0.1, 0.3f, 0.3f, 5.0f),

            new CableProperties(0.75, 0.075, 0.135, 0.95, 0.025, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.70, 0.075, 0.135, 0.95, 0.025, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.65, 0.075, 0.135, 0.95, 0.025, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.60, 0.075, 0.135, 0.95, 0.020, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.55, 0.075, 0.135, 0.95, 0.020, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.50, 0.075, 0.135, 0.95, 0.020, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.45, 0.075, 0.135, 0.95, 0.020, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.40, 0.075, 0.135, 0.95, 0.015, 0.1, 0.3f, 0.3f, 6.0f),
            new CableProperties(0.35, 0.075, 0.135, 0.95, 0.015, 0.1, 0.3f, 0.3f, 5.0f),
            new CableProperties(0.30, 0.075, 0.135, 0.95, 0.015, 0.1, 0.3f, 0.3f, 6.0f)),
            Arrays.asList(
            new CableProperties(0.75, 0.075, 1.05, 0.95, 1.2, 0.05, 0.5f, 0.5f, 0.5f),
            new CableProperties(0.25, 0.075, 1.05, 0.95, 1.2, 0.05, 0.8f, 0.4f, 0.0f)

            )), //texture and model done
    DELL_PSU(10, "dell_psu", "Dell Redundant Power Supply", "This rack-mounted power supply provides redundancy for the switch in case the switch's internal power supply fails.",
            1, true, Collections.emptyList(),
            Arrays.asList(new CableProperties(0.25, 0.075, 1.05, 0.95, 1.4, 1.5, 0.4f, 0.4f, 0.4f))), // texture and model done
    HANDLEBAR_2U(11, "handlebar_2u", "General Purpose Dell Server", "This server could be performing a number of tasks e.g. managing users, compiling code, managing storage systems or handling other administrative functions.",
            2, true, Collections.emptyList(), Collections.emptyList()), //texture and model done
    ME_484_2U(12, "me484_2u", "Storage Server", "This server is used to provide disk space for COSMA's file system.",
            2, true, Collections.emptyList(), Collections.emptyList()), // texture and model done
    LCDKVM(13, "lcdkvm", "Rack Mounted Management Console", "This console that looks a bit like a laptop is used to provide easy admin access to the system directly from the datacentre.",
            1, true, Collections.emptyList(), Collections.emptyList()),

    DOUBLE_SQUARES(14, "double_squares", "A Server", "This sever does something",
            2, true, Collections.emptyList(), Collections.emptyList()), // texture and model done (IBM System x3650??...?)
    FOUR_U_GAP(15, "four_u_gap", "Empty space","A gap between servers", 4, false, Collections.emptyList(), Collections.emptyList()), // this is fine as is
    ONE_U_HEX(16, "one_u_hex", "General Purpose Dell Server", "This server could be performing a number of tasks e.g. managing users, compiling code, managing storage systems or handling other administrative functions.",
            1, true, Collections.emptyList(), Collections.emptyList()), // texture and model done (r6525 hex)
    THREE_U_HEX(17, "three_u_hex", "General Purpose Dell Server", "This server could be performing a number of tasks e.g. managing users, compiling code, managing storage systems or handling other administrative functions.",
            3, true, Collections.emptyList(), Collections.emptyList()), // texture and model done (flat version only)
    MELLANOX_QUANT(18, "mellanox_quantum_switch", "Mellanox Infiniband Switch (HDR)", "This switch has an extremely high bandwidth connection (200Gb/s) which is used to share data between compute nodes during simulations on COSMA8.",
            1, true, Collections.emptyList(), Arrays.asList(
            new CableProperties(0.75, 0.075, 1.05, 0.95, 0.2, 0.05, 0.5f, 0.5f, 0.5f),
            new CableProperties(0.15, 0.075, 1.05, 0.95, 0.2, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.25, 0.075, 1.05, 0.95, 0.6, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.35, 0.075, 1.05, 0.95, 1.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.45, 0.075, 1.05, 0.95, 1.4, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.55, 0.075, 1.05, 0.95, 1.8, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.65, 0.075, 1.05, 0.95, 2.2, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.75, 0.075, 1.05, 0.95, 2.5, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.85, 0.075, 1.05, 0.95, 0.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.15, 0.1, 1.05, 0.05, 0.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.25, 0.1, 1.05, 0.05, 2.5, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.35, 0.1, 1.05, 0.05, 2.2, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.45, 0.1, 1.05, 0.05, 1.8, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.55, 0.1, 1.05, 0.05, 1.4, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.65, 0.1, 1.05, 0.05, 1.0, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.75, 0.1, 1.05, 0.05, 0.6, 1.5, 0.1f, 0.8f, 0.8f),
            new CableProperties(0.85, 0.1, 1.05, 0.05, 0.2, 1.5, 0.1f, 0.8f, 0.8f)
    )), // texture and model done
    R_6525(19, "r6525", "General Purpose Dell Server", "This server could be performing a number of tasks e.g. managing users, compiling code, managing storage systems or handling other administrative functions.",
            1, true, Collections.emptyList(), Collections.emptyList()), // texture and model done

    ATEMPO(20, "atempo", "Atempo Server", "This sever does something",
            1, true, Collections.emptyList(), Collections.emptyList()), //R640 (look the same as the R6525)S
    C7_FIBRE_SWITCH(21, "fibre_switch", "COSMA7 Fibre Switch", "A fibre switch for COSMA7", 1, false, Collections.emptyList(), Collections.emptyList()),
    FOUR_U_MI50(22, "mi50", "Gigabyte MI50 Server", "This sever does something",
            4, true, Collections.emptyList(), Collections.emptyList()), // texture and model done Gigabyte server
    TWO_U_GA_SERVER(23, "two_u_ga_server", "GA Server", "This sever does something",
            2, true, Collections.emptyList(), Collections.emptyList()), //7525s (TWO U HEX or just horizontal drives??)
    FOUR_U_CIRCLES(24, "four_u_circles", "A Server", "This sever does something",
            4, true, Collections.emptyList(), Collections.emptyList()), // texture and model done (DSS8440)
    GREY_4U_SERVER(25, "grey_4u_server", "A Server", "This sever does something",
            4, true, Collections.emptyList(), Collections.emptyList()), // texture and model done
    LOGIN_NODE_C8(26, "login_node_c8", "COSMA8 Login Node Server", "These servers host the login nodes for COSMA8. This is where users compile code and queue jobs for the compute nodes.",
            1, true, Collections.emptyList(), Collections.emptyList()), // texture and model done
    C8_SWITCH(27, "ethernet_switch_c8", "COSMA8 Ethernet Switch", "\"This Dell ethernet switch is used to connect parts of COSMA's internal network. For example, login nodes with compute nodes and the management system.\"",
            1, true, Arrays.asList(
            new CableProperties(0.75, 0.1, 0.135, 0.95, 0.015, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.70, 0.1, 0.135, 0.95, 0.015, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.65, 0.1, 0.135, 0.95, 0.015, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.60, 0.1, 0.135, 0.95, 0.020, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.55, 0.1, 0.135, 0.95, 0.020, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.75, 0.075, 0.135, 0.95, 0.025, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.70, 0.075, 0.135, 0.95, 0.025, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.65, 0.075, 0.135, 0.95, 0.025, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.60, 0.075, 0.135, 0.95, 0.020, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.55, 0.075, 0.135, 0.95, 0.020, 0.1, 0.5f, 0.3f, 5.0f),

            new CableProperties(0.50, 0.1, 0.135, 0.05, 0.020, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.45, 0.1, 0.135, 0.05, 0.020, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.40, 0.1, 0.135, 0.05, 0.025, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.35, 0.1, 0.135, 0.05, 0.025, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.30, 0.1, 0.135, 0.05, 0.025, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.50, 0.075, 0.135, 0.05, 0.020, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.45, 0.075, 0.135, 0.05, 0.020, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.40, 0.075, 0.135, 0.05, 0.015, 0.1, 0.5f, 0.3f, 6.0f),
            new CableProperties(0.35, 0.075, 0.135, 0.05, 0.015, 0.1, 0.5f, 0.3f, 5.0f),
            new CableProperties(0.30, 0.075, 0.135, 0.05, 0.015, 0.1, 0.5f, 0.3f, 6.0f)), Collections.emptyList()), // think this should look the same as the C7 switch?
    LCDKVM_OPEN(28, "lcdkvm_open", "Rack Mounted Management Console", "This console that looks a bit like a laptop is used to provide easy admin access to the system directly from the datacentre.",
            1, true, Collections.emptyList(), Collections.emptyList());
    private final String name;
    private final int uHeight;
    public boolean shouldRender;
    private int index;
    public List<CableProperties> frontCableList;
    public final String displayName;
    public final String description;
    public List<CableProperties> internalCableList;

    private ServerType(int index, String name, String displayName, String description, int uHeight, boolean shouldRender, List<CableProperties> frontCablePropertiesList, List<CableProperties> internalCableList){
        this.name = name;
        this.uHeight = uHeight;
        this.shouldRender = shouldRender;
        this.index = index;
        this.frontCableList = frontCablePropertiesList;
        this.displayName = displayName;
        this.description = description;
        this.internalCableList = internalCableList;
    }

    @Override
    public String getSerializedName() {return this.name;}

    public int getUHeight() {return this.uHeight;}

    public IBakedModel getModel(){
        //System.out.println(this.name);
        //System.out.println("Use ambient occlusion?"+Minecraft.getInstance().getBlockRenderer().getBlockModel(ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this)).useAmbientOcclusion());
        //System.out.println("Uses block light?"+Minecraft.getInstance().getBlockRenderer().getBlockModel(ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this)).usesBlockLight());



        return Minecraft.getInstance().getBlockRenderer().getBlockModel(ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this));}

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
