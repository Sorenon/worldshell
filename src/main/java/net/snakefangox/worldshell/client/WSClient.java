package net.snakefangox.worldshell.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.WSNetworking;
import net.snakefangox.worldshell.mixinextras.WorldExt;

@Environment(EnvType.CLIENT)
public class WSClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		WSNetworking.registerClientPackets();

		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			if (MinecraftClient.getInstance().options.debugEnabled) {
				var matrices = context.matrixStack();
				matrices.push();

				Vec3d camPos = context.camera().getPos();
				matrices.translate(-camPos.x, -camPos.y, -camPos.z);

				var physicsWorld = ((WorldExt)context.world()).getPhysics();
//				var physicsWorld = ((WorldExt)MinecraftClient.getInstance().getServer().getOverworld()).getPhysics();

				physicsWorld.debugDrawer.consumer = context.consumers().getBuffer(RenderLayer.getLines());

				physicsWorld.debugDrawer.modelMatrix = matrices.peek().getModel();
				physicsWorld.debugDrawer.normalMatrix = matrices.peek().getNormal();

				physicsWorld.dynamicsWorld.debugDrawWorld();

				matrices.pop();
			}
		});
	}
}
