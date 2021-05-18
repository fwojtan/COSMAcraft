package net.fwojtan.cosmacraft.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.common.block.ServerBlock;
import net.fwojtan.cosmacraft.common.tileentity.ParentTileEntity;
import net.fwojtan.cosmacraft.common.tileentity.RackTileEntity;
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
        for (ServerType serverType : rackTileEntity.serverTypes){
            if (serverType.shouldRender) {
                mc.getBlockRenderer().getModelRenderer().renderModel(rackTileEntity.getLevel(), serverType.getModel(), serverType.getState(), rackTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
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

    private Vector3d translation(Direction direction, double x, double y, double z){
        Vector3d vec = new Vector3d(x, y, z);
        switch (direction) {
            case SOUTH:
                return vec.yRot(180f);
            case WEST:
                return vec.yRot(90f);
            case EAST:
                return vec.yRot(270f);
            default:
                return vec;
        }
    }

    private Vector3f scale(Direction direction, float xscale, float yscale, float zscale) {
        switch (direction) {
            case WEST:
            case EAST:
                return new Vector3f(zscale, yscale, xscale);
            default:
                return new Vector3f(xscale, yscale, zscale);
        }
    }
}
