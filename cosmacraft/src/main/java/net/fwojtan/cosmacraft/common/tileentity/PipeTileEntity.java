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

public class PipeTileEntity extends ParentTileEntity{

    public PipeTileEntity(TileEntityType<?> type){super(type);}
    public PipeTileEntity(){this(ModTileEntities.PIPE_TILE_ENTITY.get());}

    @Override
    public List<BlockPos> createChildPositonList() {
        List<Vector3i> offsetList = new ArrayList<Vector3i>();
        List<BlockPos> retList = new ArrayList<BlockPos>();
        offsetList.add(new Vector3i(0, 1, 0));
        offsetList.add(new Vector3i(0, 2, 0));
        offsetList.add(new Vector3i(0, 3, 0));
        offsetList.add(new Vector3i(0, 4, 0));
        offsetList.add(new Vector3i(0, 5, 0));
        offsetList.add(new Vector3i(1, 0, 0));
        offsetList.add(new Vector3i(1, 1, 0));
        offsetList.add(new Vector3i(1, 2, 0));
        offsetList.add(new Vector3i(1, 3, 0));
        offsetList.add(new Vector3i(1, 4, 0));
        offsetList.add(new Vector3i(1, 5, 0));



        for (Vector3i vec : offsetList){
            retList.add(getChildPosition(vec));
        }
        return retList;
    }

    public void sendInfo(PlayerEntity player){
        String message = "Coolant Pipes \nThese pipes take coolant from the heat exchangers and send it to the roof to be cooled down using radiators." +
                "Without active cooling COSMA would overheat and would be forced to slow down. ";
        player.sendMessage(new TranslationTextComponent(message, new Object()), new UUID(16, 0));
    }

}
