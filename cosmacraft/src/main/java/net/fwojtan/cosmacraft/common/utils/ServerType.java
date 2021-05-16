package net.fwojtan.cosmacraft.common.utils;

import net.minecraft.util.IStringSerializable;

public enum ServerType implements IStringSerializable {
    NONE("none"),
    TWO_U_HEX("two_u_hex"),
    TWO_U_HORIZONTAL_DRIVES("two_u_horizontal_drives");

    private final String name;

    private ServerType(String name){
        this.name = name;
    }

    @Override
    public String getSerializedName() {return this.name;}
}
