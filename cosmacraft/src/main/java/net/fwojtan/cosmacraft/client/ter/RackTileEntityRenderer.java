package net.fwojtan.cosmacraft.client.ter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.fwojtan.cosmacraft.common.tileentity.RackTileEntity;
import net.fwojtan.cosmacraft.common.utils.*;
import net.fwojtan.cosmacraft.init.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.LightType;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;


import static net.fwojtan.cosmacraft.common.block.RackBlock.SHOULD_RENDER;
import static net.fwojtan.cosmacraft.common.block.DoorBlock.RENDER_CHOICE;

public class RackTileEntityRenderer extends TileEntityRenderer<RackTileEntity> {

    public RackTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
        super(p_i226006_1_);
    }

    private Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(RackTileEntity rackTileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderBuffers,
                       int combinedLight, int combinedOverlay) {

        // this combined light stuff is a bit nonsense atm
        combinedLight = Math.max(combinedLight, 10000000);
        //if (rackTileEntity.serverStates.size()>0){
            //System.out.println(rackTileEntity.serverStates.get(0));
        //}
        //System.out.println(combinedLight);
        //System.out.println(combinedOverlay);

        matrixStack.pushPose();

        IVertexBuilder vertexBuffer = renderBuffers.getBuffer(RenderType.cutoutMipped());
        Random random = new Random();

        rotateStack(rackTileEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), matrixStack);
        renderServerFrame(rackTileEntity, matrixStack, vertexBuffer, random, combinedLight, combinedOverlay);
        renderDoor(rackTileEntity, matrixStack, vertexBuffer, random, combinedLight, combinedOverlay);

        if (rackTileEntity.getListInitialized()) {
            renderServers(rackTileEntity, matrixStack, renderBuffers, vertexBuffer, random, combinedLight, combinedOverlay);
        }

        Vector3f cableColor = new Vector3f(0.58f,0.50f,1.00f);

        /*
        renderCable(new Vector3d(0.4, 0.5, 0.15), new Vector3d(0.05, 1,
                0.05), matrixStack, renderBuffers, rackTileEntity, cableColor);
        renderCable(new Vector3d(0.6, 0.5, 0.15), new Vector3d(0.95, 1,
                0.05), matrixStack, renderBuffers, rackTileEntity, cableColor);

        renderCable(new Vector3d(0.4, 2.0, 0.15), new Vector3d(0.05, 2.5,
                0.05), matrixStack, renderBuffers, rackTileEntity, cableColor);
        renderCable(new Vector3d(0.6, 2.0, 0.15), new Vector3d(0.95, 2.5,
                0.05), matrixStack, renderBuffers, rackTileEntity, cableColor);
        */

        matrixStack.popPose();


    }

    private void renderDoor(RackTileEntity rackTileEntity, MatrixStack matrixStack, IVertexBuilder vertexBuffer, Random random, int combinedLight, int combinedOverlay) {
        if (rackTileEntity.doorType != null) {
            if (rackTileEntity.doorType.shouldRender) {

                matrixStack.pushPose();

                if (rackTileEntity.doorOpen == 1 || rackTileEntity.doorOpenProgress>0){
                    rotateStackForDoor(rackTileEntity.doorOpenProgress, matrixStack);
                }

                mc.getBlockRenderer().getModelRenderer().renderModel(rackTileEntity.getLevel(), rackTileEntity.doorType.getModel(), rackTileEntity.doorType.getState(), rackTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);


                matrixStack.popPose();

                if (rackTileEntity.doorOpen==1 && rackTileEntity.doorOpenProgress<50){rackTileEntity.doorOpenProgress++;}
                if (rackTileEntity.doorOpen==0 && rackTileEntity.doorOpenProgress>0){rackTileEntity.doorOpenProgress--;}

            }
        }
    }

    private void renderServers(RackTileEntity rackTileEntity, MatrixStack matrixStack, IRenderTypeBuffer renderBuffers, IVertexBuilder vertexBuffer, Random random, int combinedLight, int combinedOverlay){
        if (rackTileEntity.serverStates.size() == rackTileEntity.serverTypes.size()) {
            for (int i = 0; i < rackTileEntity.serverTypes.size(); i++) {
                ServerType serverType = rackTileEntity.serverTypes.get(i);
                ServerState serverState = rackTileEntity.serverStates.get(i);
                if (serverType.shouldRender) {

                    Vector3d ejectAmount = new Vector3d(0.0d, 0.0d, 0.0d);

                    if (serverState.ejected == 1 || serverState.ejectProgress > 0) {
                        ejectAmount = new Vector3d(0.0d, 0.0d, -0.4 * (1 - Math.cos(Math.PI * serverState.ejectProgress / 50.0d)));
                        matrixStack.translate(ejectAmount.x, ejectAmount.y, ejectAmount.z);
                    }

                    if (serverType.getSerializedName() == "lcdkvm" && serverState.ejectProgress == 50){
                        mc.getBlockRenderer().getModelRenderer().renderModel(rackTileEntity.getLevel(), ServerType.LCDKVM_OPEN.getModel(), serverType.getState(), rackTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
                    } else {
                        mc.getBlockRenderer().getModelRenderer().renderModel(rackTileEntity.getLevel(), serverType.getModel(), serverType.getState(), rackTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
                    }
                    // move the matrix stack back so the rest of the servers can be rendered in the correct place
                    if (serverState.ejected == 1 || serverState.ejectProgress > 0) {
                        matrixStack.translate(0.0d, 0.0d, -1.0d * ejectAmount.z);
                    }

                    if (serverState.ejectProgress < 50 && serverState.ejected == 1) serverState.ejectProgress++;
                    if (serverState.ejected == 0 && serverState.ejectProgress > 0) serverState.ejectProgress--;

                    for (CableProperties cableProperties : serverType.frontCableList) {
                        renderCable(cableProperties.startPoint.add(ejectAmount), cableProperties.endPoint, matrixStack, renderBuffers, rackTileEntity, cableProperties.color);
                    }

                    vertexBuffer = renderBuffers.getBuffer(RenderType.cutoutMipped());

                }
                matrixStack.translate(0.0d, 0.057865d * serverType.getUHeight(), 0.0d);
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

    private void rotateStackForDoor(int progress, MatrixStack matrixStack){
        double progressFactor = (progress / 50.0d);
        float doorRotationDegrees = -90.0f* progress / 50.0f;
        double xTranslation = 1.8d * progressFactor*progressFactor;
        double yTranslation = -1.8d * progressFactor*progressFactor;

        matrixStack.mulPose(Vector3f.YP.rotationDegrees(doorRotationDegrees));

        matrixStack.translate(xTranslation, 0.0d, yTranslation);

    }

    private void renderServerFrame(RackTileEntity rackTileEntity, MatrixStack matrixStack, IVertexBuilder vertexBuffer, Random random, int combinedLight, int combinedOverlay) {
        BlockState state = ModBlocks.RACK_BLOCK.get().defaultBlockState().setValue(SHOULD_RENDER, true);
        IBakedModel model = mc.getBlockRenderer().getBlockModel(state);
        mc.getBlockRenderer().getModelRenderer().renderModel(rackTileEntity.getLevel(), model, state, rackTileEntity.getBlockPos(), matrixStack, vertexBuffer, true, random, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

    }




    // below functions are duplicated from Minecraft implementation of leash rendering

    private void renderCable(Vector3d startPos, Vector3d endPos, MatrixStack matrixStack, IRenderTypeBuffer renderBuffer,
                             RackTileEntity rackTileEntity, Vector3f color) {
        matrixStack.pushPose(); // maybe it's ok to nest multiple push poses as they just go deeper into the stack?

        matrixStack.translate(startPos.x, startPos.y, startPos.z);
        float xDiff = (float)(endPos.x - startPos.x);
        float yDiff = (float)(endPos.y - startPos.y);
        float zDiff = (float)(endPos.z - startPos.z);
        float f3 = 0.025F;
        IVertexBuilder ivertexbuilder = renderBuffer.getBuffer(RenderType.leash());
        Matrix4f matrix4f = matrixStack.last().pose();
        // I'm only guessing that these things are scale - I actually have no clue
        float scale = MathHelper.fastInvSqrt(xDiff * xDiff + zDiff * zDiff) * 0.025F / 2.0F;
        float zScale = zDiff * scale;
        float xScale = xDiff * scale;
        BlockPos blockpos = new BlockPos(rackTileEntity.getBlockPos());
        //int i = rackTileEntity.getLevel().getBlockLightLevel(mobEntity, blockpos);
        //int j = this.entityRenderDispatcher.getRenderer(leashHolderEntity).getBlockLightLevel(leashHolderEntity, blockpos);
        int k = rackTileEntity.getLevel().getBrightness(LightType.SKY, blockpos)+150;
        //int l = mobEntity.level.getBrightness(LightType.SKY, blockpos);
        int i = k+100;
        int j = k+100;
        int l = k+100;
        renderSide(ivertexbuilder, matrix4f, xDiff, yDiff, zDiff, i, j, k, l, 0.025F, 0.025F, zScale, xScale, color);
        renderSide(ivertexbuilder, matrix4f, xDiff, yDiff, zDiff, i, j, k, l, 0.025F, 0.0F, zScale, xScale, color);
        matrixStack.popPose();
    }

    public static void renderSide(IVertexBuilder iVertexBuilder, Matrix4f lastMatrixPose, float xDiff, float yDiff,
                                  float zDiff, int lightLvl1, int lightLvl2, int lightLvl3, int lightLvl4,
                                  float p_229119_9_, float p_229119_10_, float p_229119_11_, float p_229119_12_,
                                  Vector3f color) {

        int i = 8; // number of straight sections cable split into

        for(int j = 0; j < i; ++j) {
            float f = (float)j / (float)(i-1);
            int k = (int)MathHelper.lerp(f, (float)lightLvl1, (float)lightLvl2);
            int l = (int)MathHelper.lerp(f, (float)lightLvl3, (float)lightLvl4);
            int i1 = LightTexture.pack(k, l);
            addVertexPair(iVertexBuilder, lastMatrixPose, i1, xDiff, yDiff, zDiff, p_229119_9_, p_229119_10_, i, j, false, p_229119_11_, p_229119_12_, color);
            addVertexPair(iVertexBuilder, lastMatrixPose, i1, xDiff, yDiff, zDiff, p_229119_9_, p_229119_10_, i, j + 1, true, p_229119_11_, p_229119_12_, color);
        }

    }

    public static void addVertexPair(IVertexBuilder iVertexBuilder, Matrix4f lastMatrixPose, int lightTextureThing,
                                     float xDiff, float yDiff, float zDiff, float p_229120_6_, float p_229120_7_,
                                     int p_229120_8_, int p_229120_9_, boolean p_229120_10_, float p_229120_11_,
                                     float p_229120_12_, Vector3f color) {
        float f = color.x();
        float f1 = color.y();
        float f2 = color.z();
        if (p_229120_9_ % 2 == 0) {
            f *= 0.8F;
            f1 *= 0.8F;
            f2 *= 0.8F;
        }

        float f3 = (float)p_229120_9_ / (float)p_229120_8_;
        float f4 = xDiff * f3;
        float f5 = yDiff > 0.0F ? yDiff * f3 * f3 : yDiff - yDiff * (1.0F - f3) * (1.0F - f3);
        float f6 = zDiff * f3;
        if (!p_229120_10_) {
            iVertexBuilder.vertex(lastMatrixPose, f4 + p_229120_11_, f5 + p_229120_6_ - p_229120_7_, f6 - p_229120_12_).color(f, f1, f2, 1.0F).uv2(lightTextureThing).endVertex();
        }

        iVertexBuilder.vertex(lastMatrixPose, f4 - p_229120_11_, f5 + p_229120_7_, f6 + p_229120_12_).color(f, f1, f2, 1.0F).uv2(lightTextureThing).endVertex();
        if (p_229120_10_) {
            iVertexBuilder.vertex(lastMatrixPose, f4 + p_229120_11_, f5 + p_229120_6_ - p_229120_7_, f6 - p_229120_12_).color(f, f1, f2, 1.0F).uv2(lightTextureThing).endVertex();
        }

    }

}
