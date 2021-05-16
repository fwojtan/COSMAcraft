package net.fwojtan.cosmacraft.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.fwojtan.cosmacraft.CosmaCraft;
import net.fwojtan.cosmacraft.common.block.ServerBlock;
import net.fwojtan.cosmacraft.common.tileentity.ParentTileEntity;
import net.fwojtan.cosmacraft.common.utils.ServerType;
import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
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

import java.util.Random;

import static net.fwojtan.cosmacraft.common.block.ParentBlock.SHOULD_RENDER;
import static net.fwojtan.cosmacraft.common.block.ServerBlock.RENDER_CHOICE;

public class RackTileEntityRenderer extends TileEntityRenderer<ParentTileEntity> {

    public RackTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
    }

    private Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(ParentTileEntity parentTileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffers,
                       int combinedLight, int combinedOverlay) {

        matrixStack.pushPose();


        Direction direction = parentTileEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        BlockRendererDispatcher dispatcher = mc.getBlockRenderer();


        IVertexBuilder vertexBuffer = renderBuffers.getBuffer(RenderType.cutoutMipped());
        Random random = new Random();



        rotateStack(direction, matrixStack);
        renderServerFrame(parentTileEntity, matrixStack, vertexBuffer, random, combinedLight, combinedOverlay);

        BlockState hexState =  ModBlocks.SERVER_MODEL_BLOCK.get().defaultBlockState().setValue(RENDER_CHOICE, ServerType.TWO_U_HEX);
        IBakedModel hexServerModel = dispatcher.getBlockModel(hexState);

        for (int i=0; i<21; i++){
            dispatcher.getModelRenderer().renderModel(parentTileEntity.getLevel(), hexServerModel, hexState, parentTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
            matrixStack.translate(0.0d, 0.11573d, 0.0d);
        }
        matrixStack.popPose();




    }

    private void renderServers(ParentTileEntity parentTileEntity, MatrixStack matrixStack, IVertexBuilder vertexBuffer, Random random, int combinedLight, int combinedOverlay){
        ServerType[] serverTypes = ParentTileEntity.serverTypes;

        for (ServerType serverType : serverTypes){
            matrixStack.translate(0.0d, 0.057865d, 0.0d);

            switch (serverType) {
                case TWO_U_HEX:

                case NONE:
                default:
                    break;
            }
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

    private void renderServerFrame(ParentTileEntity parentTileEntity, MatrixStack matrixStack, IVertexBuilder vertexBuffer, Random random, int combinedLight, int combinedOverlay) {
        BlockState state = ModBlocks.PARENT_BLOCK.get().defaultBlockState().setValue(SHOULD_RENDER, true);
        BlockRendererDispatcher dispatcher = mc.getBlockRenderer();
        IBakedModel model = dispatcher.getBlockModel(state);

        dispatcher.getModelRenderer().renderModel(parentTileEntity.getLevel(), model, state, parentTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

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
