package net.fwojtan.cosmacraft.common.utils;

import net.minecraft.util.IStringSerializable;

public enum ColorChoice implements IStringSerializable {
    NONE("none"),
    RED("red"),
    YELLOW("yellow"),
    GREEN1("green1"),
    GREEN2("green2"),
    GREEN3("green3"),
    GREEN4("green4"),
    GREEN5("green5");

    public String name;

    private ColorChoice(String name){
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
