package net.snakefangox.worldshell.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.snakefangox.worldshell.mixin.rendering.GameRendererAccess;

import java.util.*;

public class WorldShellRenderCache {

	private static final List<RenderLayer> renderLayers = RenderLayer.getBlockLayers();

	private final Map<RenderLayer, VertexBuffer> bufferStorage = new HashMap<>();
	private final Map<RenderLayer, BufferBuilder> buffers = new HashMap<>();
	private final Set<RenderLayer> bufferFilled = new HashSet<>();

	public WorldShellRenderCache() {
		fillBuffers();
	}

	private void fillBuffers() {
		renderLayers.forEach(renderLayer -> {
			BufferBuilder bufferBuilder = new BufferBuilder(renderLayer.getExpectedBufferSize());
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
			buffers.put(renderLayer, bufferBuilder);
			bufferStorage.put(renderLayer, new VertexBuffer());
		});
	}

	public BufferBuilder get(RenderLayer renderLayer) {
		if (buffers.containsKey(renderLayer)) {
			bufferFilled.add(renderLayer);
			return buffers.get(renderLayer);
		} else {
			bufferFilled.add(getDefault());
			return buffers.get(getDefault());
		}
	}

	private RenderLayer getDefault() {
		return RenderLayer.getSolid();
	}

	public void upload() {
		buffers.forEach((key, entry) -> {
			entry.end();
			bufferStorage.get(key).upload(entry);
		});
	}

	public void draw(MatrixStack matrices) {
		GameRenderer gameRenderer = MinecraftClient.getInstance().gameRenderer;
		float tickDelta = MinecraftClient.getInstance().getTickDelta();
		double fov = ((GameRendererAccess) gameRenderer).invokeGetFov(gameRenderer.getCamera(), tickDelta, true);
		Matrix4f proj = gameRenderer.getBasicProjectionMatrix(fov);
		for (RenderLayer renderLayer : renderLayers) {
			if (bufferFilled.contains(renderLayer)) {
				VertexBuffer entry = bufferStorage.get(renderLayer);
				renderLayer.startDrawing();
				entry.bind();
				entry.setShader(matrices.peek().getModel(), proj, RenderSystem.getShader());
				renderLayer.endDrawing();
			}
		}
	}

	public void reset() {
		buffers.forEach((key, entry) -> entry.reset());
		bufferStorage.forEach((key, entry) -> entry.close());
		buffers.clear();
		bufferStorage.clear();
		fillBuffers();
	}
}
