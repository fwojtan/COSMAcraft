package net.fwojtan.cosmacraft.common.utils;

import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.IStringSerializable;


import static net.fwojtan.cosmacraft.common.block.ServerBlock.RENDER_CHOICE;

public enum ServerType implements IStringSerializable {
    ONE_U_GAP("one_u_gap", 1, false),
    TWO_U_HEX("two_u_hex", 2, true),
    ONE_U_HORIZONTAL_DRIVES("one_u_horizontal_drives", 1, true),
    C_6525("c_6525", 2, true),
    ME_484("me484", 4, true),
    MD_3420("md3420", 4, true),
    R740_HEX_FLAT("r740_hex_flat", 2, true),
    MELLANOX_EDR("mellanox_edr_switch", 1, true);

    private final String name;
    private final int uHeight;
    public boolean shouldRender;

    private ServerType(String name, int uHeight, boolean shouldRender){
        this.name = name;
        this.uHeight = uHeight;
        this.shouldRender = shouldRender;
    }

    @Override
    public String getSerializedName() {return this.name;}

    public int getUHeight() {return this.uHeight;}

    public IBakedModel getModel(){return Minecraft.getInstance().getBlockRenderer().getBlockModel(ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this));}

    public BlockState getState(){return ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, this);}


}
