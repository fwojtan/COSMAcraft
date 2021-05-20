package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class ChildTileEntity extends TileEntity {

    public BlockPos parentPosition = null;

    public ChildTileEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    public ChildTileEntity() {this(ModTileEntities.CHILD_TILE_ENTITY.get());}


    // the below two overrides handle updates from the server thread
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        //write data into nbtTag

        nbtTag.putInt("controllerXPos", this.parentPosition.getX());
        nbtTag.putInt("controllerYPos", this.parentPosition.getY());
        nbtTag.putInt("controllerZPos", this.parentPosition.getZ());

        System.out.println("Sending update tag");

        return new SUpdateTileEntityPacket(getBlockPos(), -1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbtTag = pkt.getTag();
        System.out.println("Received update tag");

        this.parentPosition = new BlockPos(
                nbtTag.getInt("controllerXPos"),
                nbtTag.getInt("controllerYPos"),
                nbtTag.getInt("controllerZPos"));




    }




}
