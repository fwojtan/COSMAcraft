package net.fwojtan.cosmacraft.common.tileentity;

import net.fwojtan.cosmacraft.init.ModTileEntities;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class ExchangerTileEntity extends ParentTileEntity{

    public ExchangerTileEntity(TileEntityType<?> type){super(type);}
    public ExchangerTileEntity(){this(ModTileEntities.EXCHANGER_TILE_ENTITY.get());}

    @Override
    public List<BlockPos> createChildPositonList() {
        List<Vector3i> offsetList = new ArrayList<Vector3i>();
        List<BlockPos> retList = new ArrayList<BlockPos>();
        offsetList.add(new Vector3i(0, 1, 0));
        offsetList.add(new Vector3i(0, 2, 0));
        offsetList.add(new Vector3i(1, 0, 0));
        offsetList.add(new Vector3i(1, 1, 0));
        offsetList.add(new Vector3i(1, 2, 0));
        offsetList.add(new Vector3i(0, 0, 1));
        offsetList.add(new Vector3i(0, 1, 1));
        offsetList.add(new Vector3i(0, 2, 1));
        offsetList.add(new Vector3i(1, 0, 1));
        offsetList.add(new Vector3i(1, 1, 1));
        offsetList.add(new Vector3i(1, 2, 1));


        for (Vector3i vec : offsetList){
            retList.add(getChildPosition(vec));
        }
        return retList;
    }

}
