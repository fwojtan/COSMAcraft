package net.fwojtan.cosmacraft.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.common.block.ServerBlock;
import net.fwojtan.cosmacraft.common.tileentity.ParentTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.RackTileEntity;
import net.fwojtan.cosmacraft.common.utils.ServerState;
import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.obj.OBJLoader;
import org.lwjgl.system.CallbackI;

import java.util.List;
import java.util.Random;


import static net.fwojtan.cosmacraft.common.block.RackBlock.SHOULD_RENDER;
import static net.fwojtan.cosmacraft.common.block.ServerBlock.RENDER_CHOICE;

public class RackTileEntityRenderer extends TileEntityRenderer<RackTileEntity> {

    public RackTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
    }

    private Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(RackTileEntity rackTileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffers,
                       int combinedLight, int combinedOverlay) {

        matrixStack.pushPose();

        IVertexBuilder vertexBuffer = renderBuffers.getBuffer(RenderType.cutoutMipped());
        Random random = new Random();

        rotateStack(rackTileEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), matrixStack);
        renderServerFrame(rackTileEntity, matrixStack, vertexBuffer, random, combinedLight, combinedOverlay);

        if (rackTileEntity.getListInitialized()) {
            renderServers(rackTileEntity, matrixStack, vertexBuffer, random, combinedLight, combinedOverlay);
        }
        matrixStack.popPose();


    }

    private void renderServers(RackTileEntity rackTileEntity, MatrixStack matrixStack, IVertexBuilder vertexBuffer, Random random, int combinedLight, int combinedOverlay){
        for (int i = 0; i<rackTileEntity.serverTypes.size(); i++){
            ServerType serverType = rackTileEntity.serverTypes.get(i);
            ServerState serverState = rackTileEntity.serverStates.get(i);
            if (serverType.shouldRender) {

                // move matrix stack in the right direction for the server to be ejected forwards
                if (serverState.ejected == 1 || serverState.ejectProgress>0) {
                    double ejectAmount = 0.4 * (1-Math.cos(Math.PI*serverState.ejectProgress/50.0d));
                    switch (rackTileEntity.parentDirection){
                        case EAST:
                            matrixStack.translate(0.0d, 0.0d, -ejectAmount);break;
                        case WEST:
                            matrixStack.translate(0.0d, 0.0d, ejectAmount);break;
                        case SOUTH:
                            matrixStack.translate(-ejectAmount, 0.0d, 0.0d);break;
                        default:
                            matrixStack.translate(ejectAmount, 0.0d, 0.0d);break;
                    }
                }


                mc.getBlockRenderer().getModelRenderer().renderModel(rackTileEntity.getLevel(), serverType.getModel(), serverType.getState(), rackTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);


                // move the matrix stack back so the rest of the servers can be rendered in the correct place
                if (serverState.ejected == 1 || serverState.ejectProgress>0) {
                    double ejectAmount = 0.4 * (1-Math.cos(Math.PI*serverState.ejectProgress/50.0d));
                    switch (rackTileEntity.parentDirection){
                        case EAST:
                            matrixStack.translate(0.0d, 0.0d, ejectAmount);break;
                        case WEST:
                            matrixStack.translate(0.0d, 0.0d, -ejectAmount);break;
                        case SOUTH:
                            matrixStack.translate(ejectAmount, 0.0d, 0.0d);break;
                        default:
                            matrixStack.translate(-ejectAmount, 0.0d, 0.0d);break;
                    }
                }

                if (serverState.ejectProgress<50 && serverState.ejected==1) serverState.ejectProgress++;
                if (serverState.ejected == 0 && serverState.ejectProgress>0) serverState.ejectProgress--;

            }

            matrixStack.translate(0.0d, 0.057865d*serverType.getUHeight(), 0.0d);
        }

    }


    private void rotateStack(Direction direction, MatrixStack matrixStack) {
        // includes some correction translations for some weird offsets that creeped in somewhere

        switch (direction) {
            case SOUTH:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180f));
                matrixStack.translate(-1.0d, 0.0d, -1.0d);
                break;
            case WEST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90f));
                matrixStack.translate(-1.0d, 0.0d, 0.0d);
                break;
            case EAST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(270f));
                matrixStack.translate(0.0d, 0.0d, -1.0d);
                break;
        }
    }

    private void renderServerFrame(RackTileEntity rackTileEntity, MatrixStack matrixStack, IVertexBuilder vertexBuffer, Random random, int combinedLight, int combinedOverlay) {
        BlockState state = ModBlocks.RACK_BLOCK.get().defaultBlockState().setValue(SHOULD_RENDER, true);
        IBakedModel model = mc.getBlockRenderer().getBlockModel(state);
        mc.getBlockRenderer().getModelRenderer().renderModel(rackTileEntity.getLevel(), model, state, rackTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

    }

}
