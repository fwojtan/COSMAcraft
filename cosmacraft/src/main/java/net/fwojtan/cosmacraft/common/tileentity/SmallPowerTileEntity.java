package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SmallPowerTileEntity extends ParentTileEntity{

    public SmallPowerTileEntity(TileEntityType<?> type){super(type);}
    public SmallPowerTileEntity(){this(ModTileEntities.SMALL_POWER_TILE_ENTITY.get());}

    @Override
    public List<BlockPos> createChildPositonList() {
        List<Vector3i> offsetList = new ArrayList<Vector3i>();
        List<BlockPos> retList = new ArrayList<BlockPos>();
        offsetList.add(new Vector3i(1, 0, 0));
        offsetList.add(new Vector3i(0, 1, 0));
        offsetList.add(new Vector3i(1, 1, 0));


        for (Vector3i vec : offsetList){
            retList.add(getChildPosition(vec));
        }
        return retList;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (parentDirection != null) {
            Vector3d inflation = getBBInflate();
            return super.getRenderBoundingBox().inflate(inflation.x, inflation.y, inflation.z).move(getBBOffset());
        } else {return super.getRenderBoundingBox();}
    }

    private Vector3d getBBOffset(){
        switch (parentDirection) {
            case SOUTH:
                return new Vector3d(-0.5d, 0.5d, 0d);
            case EAST:
                return new Vector3d(0d, 0.5d, -0.5d);

            case WEST:
                return new Vector3d(0d, 0.5d, 0.5d);
            default:
                return new Vector3d(0.5d, 0.5d, 0d);
        }
    }

    private Vector3d getBBInflate(){
        switch (parentDirection) {

            case EAST:
            case WEST:
                return new Vector3d(0d, 0.5d, 0.5d);
            default:
                return new Vector3d(0.5d, 0.5d, 0d);
        }
    }
    public void sendInfo(PlayerEntity player){
        String message = "Power Unit \nThis power supply is one of many around the datacentre that ensure the flow of electricity " +
                "to COSMA is kept stable.";
        player.sendMessage(new TranslationTextComponent(message, new Object()), new UUID(16, 0));
    }
}
