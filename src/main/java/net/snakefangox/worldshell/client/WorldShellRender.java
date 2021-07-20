package net.snakefangox.worldshell.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.Microcosm;

import java.util.Map;
import java.util.Random;

/**
 * Static helper methods to render a {@link Microcosm} in different contexts.
 */
@Environment(EnvType.CLIENT)
public class WorldShellRender {

    public static void renderMicrocosm(WorldShellEntity wsEntity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        net.snakefangox.worldshell.math.Quaternion entRot = wsEntity.getRotation();
        renderMicrocosm(wsEntity.getMicrocosm(), matrices, new Quaternion((float) entRot.getX(), (float) entRot.getY(), (float) entRot.getZ(), (float) entRot.getW()),
                wsEntity.world.random, vertexConsumers, 0.0f);
    }

    public static void renderMicrocosm(Microcosm microcosm, MatrixStack matrices, Quaternion quaternion, Random random, VertexConsumerProvider vertexConsumers, float delta) {
        BlockRenderManager renderManager = MinecraftClient.getInstance().getBlockRenderManager();
        BlockEntityRenderDispatcher beRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
        matrices.push();
        matrices.multiply(quaternion);
//        if (vertexConsumers instanceof VertexConsumerProvider.Immediate) {
//            microcosm.tickCache();
//            var cache = microcosm.getCache();
//            if (!microcosm.isCacheValid()) {
//                cache.reset();
//                renderToCache(microcosm, renderManager, random);
//            }
//            for (var layer : RenderLayer.getBlockLayers()) {
//                BufferBuilder readBuffer = cache.get(layer);
//                BufferBuilder writeBuffer = (BufferBuilder) vertexConsumers.getBuffer(layer);
//                writeBuffer.
//            }
//        } else {
            renderToConsumers(microcosm, renderManager, random, vertexConsumers, matrices);
//        }
        for (Map.Entry<BlockPos, BlockEntity> entry : microcosm.getBlockEntities()) {
            matrices.push();
            BlockPos bp = entry.getKey();
            BlockEntity be = entry.getValue();
            matrices.translate(bp.getX(), bp.getY(), bp.getZ());
            {
                BlockEntityRenderer<BlockEntity> blockEntityRenderer = beRenderDispatcher.get(be);
                if (blockEntityRenderer != null) {
                    if (be.hasWorld() && be.getType().supports(be.getCachedState())) {
                        blockEntityRenderer.render(be, delta, matrices, vertexConsumers, WorldRenderer.getLightmapCoordinates(be.getWorld(), be.getPos()), OverlayTexture.DEFAULT_UV);
                    }
                }
            }
            matrices.pop();
        }
        matrices.pop();
    }

    private static void renderToConsumers(Microcosm microcosm, BlockRenderManager renderManager, Random random, VertexConsumerProvider vertexConsumers, MatrixStack matrices) {
        for (Map.Entry<BlockPos, BlockState> entry : microcosm.getBlocks()) {
            BlockState bs = entry.getValue();
            FluidState fs = bs.getFluidState();
            BlockPos bp = entry.getKey();
            matrices.push();
            matrices.translate(bp.getX(), bp.getY(), bp.getZ());
            if (!fs.isEmpty()) {
                matrices.push();
                matrices.translate(-(bp.getX() & 15), -(bp.getY() & 15), -(bp.getZ() & 15));
                renderManager.renderFluid(bp, microcosm, vertexConsumers.getBuffer(RenderLayers.getFluidLayer(fs)), fs);
                matrices.pop();
            }
            if (bs.getRenderType() != BlockRenderType.INVISIBLE) {
                renderManager.renderBlock(bs, bp, microcosm, matrices, vertexConsumers.getBuffer(RenderLayers.getBlockLayer(bs)), true, random);
            }
            matrices.pop();
        }
    }

    private static void renderToCache(Microcosm microcosm, BlockRenderManager renderManager, Random random) {
        BlockBufferBuilderStorage renderCache = microcosm.getCache();

        renderToConsumers(microcosm, renderManager, random, renderCache::get, new MatrixStack());

        microcosm.markCacheValid();
    }
}
