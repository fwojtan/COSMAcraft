package net.fwojtan.cosmacraft.common.utils;

import net.minecraft.util.IStringSerializable;

public enum ServerType implements IStringSerializable {
    ONE_U_GAP("one_u_gap", 1),
    TWO_U_HEX("two_u_hex", 2),
    ONE_U_HORIZONTAL_DRIVES("one_u_horizontal_drives", 1);

    private final String name;
    private final int u_height;

    private ServerType(String name, int u_height){
        this.name = name;
        this.u_height = u_height;
    }

    @Override
    public String getSerializedName() {return this.name;}

    public int getU_height() {return this.u_height;}
}
