package net.snakefangox.worldshell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.Bullet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.snakefangox.worldshell.kevlar.FakeApp;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;
import net.snakefangox.worldshell.mixinextras.WorldExt;
import net.snakefangox.worldshell.storage.EmptyChunkGenerator;
import net.snakefangox.worldshell.storage.ShellStorageData;
import net.snakefangox.worldshell.transfer.ShellTransferHandler;
import net.snakefangox.worldshell.util.DynamicWorldRegister;
import net.snakefangox.worldshell.world.CreateWorldsEvent;
import net.snakefangox.worldshell.world.ShellStorageWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class WorldShellMain implements ModInitializer {

	public static final String MODID = "worldshell";
	public static final Logger LOGGER = LogManager.getLogger();

	public static final RegistryKey<World> STORAGE_DIM = RegistryKey.of(Registry.WORLD_KEY, new Identifier(MODID, "shell_storage"));

	public static ServerWorld getStorageDim(MinecraftServer server) {
		return server.getWorld(WorldShellMain.STORAGE_DIM);
	}

	@Override
	public void onInitialize() {
		registerStorageDim();
		WSNetworking.registerServerPackets();
		CreateWorldsEvent.EVENT.register(this::registerShellStorageDimension);
		ServerLifecycleEvents.SERVER_STARTED.register(ShellTransferHandler::serverStartTick);
		ServerTickEvents.START_SERVER_TICK.register(ShellTransferHandler::serverStartTick);
		ServerTickEvents.END_SERVER_TICK.register(ShellTransferHandler::serverEndTick);
		ServerLifecycleEvents.SERVER_STOPPING.register(ShellTransferHandler::serverStopping);

		Gdx.app = new FakeApp();
		Bullet.init();

		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			if (MinecraftClient.getInstance().options.debugEnabled) {
                var matrices = context.matrixStack();
                matrices.push();

                Vec3d camPos = context.camera().getPos();
                matrices.translate(-camPos.x, -camPos.y, -camPos.z);

                var physicsWorld = ((WorldExt)context.world()).getPhysics();

                physicsWorld.debugDrawer.consumer = context.consumers().getBuffer(RenderLayer.getLines());

                physicsWorld.debugDrawer.modelMatrix = matrices.peek().getModel();
                physicsWorld.debugDrawer.normalMatrix = matrices.peek().getNormal();

                physicsWorld.dynamicsWorld.debugDrawWorld();

                matrices.pop();
            }
		});

		LOGGER.info("Moving blocks and fudging collision!");
		LOGGER.info("(Worldshell has started successfully)");
	}

	public void registerStorageDim() {
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier(MODID, "empty"), EmptyChunkGenerator.CODEC);
	}

	public void registerShellStorageDimension(MinecraftServer server) {
		Supplier<DimensionType> typeSupplier = () -> server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY)
				.get(RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(MODID, "shell_storage_type")));
		ChunkGenerator chunkGenerator = new EmptyChunkGenerator(new FixedBiomeSource(server.getRegistryManager()
				.get(Registry.BIOME_KEY).get(BiomeKeys.THE_VOID)));
		DimensionOptions options = new DimensionOptions(typeSupplier, chunkGenerator);
		ShellStorageWorld world = (ShellStorageWorld) DynamicWorldRegister.createDynamicWorld(server, STORAGE_DIM, options, ShellStorageWorld::new);
		world.setCachedBayData(ShellStorageData.getOrCreate(server));
	}
}
