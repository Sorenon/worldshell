package net.snakefangox.worldshell_fork.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.snakefangox.worldshell_fork.WSNetworking;
import net.snakefangox.worldshell_fork.entity.WorldShellEntity;
import net.snakefangox.worldshell_fork.math.Vector3d;
import net.snakefangox.worldshell_fork.storage.Bay;
import net.snakefangox.worldshell_fork.storage.ShellStorageData;
import net.snakefangox.worldshell_fork.util.WorldShellPacketHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public class ShellStorageWorld extends ServerWorld implements Worldshell {

	private ShellStorageData cachedBayData;

	public ShellStorageWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long l, List<Spawner> spawners, boolean shouldTickTime) {
		super(server, workerExecutor, session, properties, registryKey, dimensionType, worldGenerationProgressListener, chunkGenerator, debugWorld, l, spawners, shouldTickTime);
		ServerWorldEvents.LOAD.invoker().onWorldLoad(server, this);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
		boolean changed = super.setBlockState(pos, state, flags, maxUpdateDepth);
		if (changed) {
			passCallToEntity(pos, (entity, bay) -> {
				PacketByteBuf buf = PacketByteBufs.create();
				WorldShellPacketHelper.writeBlock(buf, this, pos, entity, bay);
				boolean boundChanged = bay.updateBoxBounds(pos);
				if (boundChanged) bay.setLoadForChunks(getServer(), true);

				var blockEntity = getBlockEntity(pos);
				if (blockEntity == null) {
					entity.updateWorldShell(bay.toLocal(pos), state, new NbtCompound());
				} else {
					entity.updateWorldShell(bay.toLocal(pos), state, blockEntity.writeNbt(new NbtCompound()));
				}
				PlayerLookup.tracking(entity).forEach(player -> ServerPlayNetworking.send(player, WSNetworking.SHELL_UPDATE, buf));
			});
		}
		return changed;
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		passCallToEntity(pos, (entity, bay) -> {
			BlockPos newPos = bay.toEntityGlobalSpace(pos);
			entity.getEntityWorld().playSound(null, newPos, sound, category, volume, pitch);
		});
	}

	@Override
	public void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean bl) {
		passCallToEntity(new BlockPos(x, y, z), (entity, bay) -> {
			Vec3d newPos = bay.toEntityGlobalSpace(x, y, z);
			entity.getEntityWorld().playSound(newPos.x, newPos.y, newPos.z, sound, category, volume, pitch, bl);
		});
	}

	@Override
	public List<Entity> getOtherEntities(@Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
		return passCallToEntity(new BlockPos(box.minX, box.minY, box.minZ), new ArrayList<>(), ((entity, bay) -> {
			Vec3d newMin = bay.toEntityGlobalSpace(box.minX, box.minY, box.minZ);
			Vec3d newMax = bay.toEntityGlobalSpace(box.maxX, box.maxY, box.maxZ);
			return entity.getEntityWorld().getOtherEntities(except, new Box(newMin, newMax), predicate);
		}));
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> arg, Box box, Predicate<? super T> predicate) {
		return passCallToEntity(new BlockPos(box.minX, box.minY, box.minZ), new ArrayList<>(), ((entity, bay) -> {
			Vec3d newMin = bay.toEntityGlobalSpace(box.minX, box.minY, box.minZ);
			Vec3d newMax = bay.toEntityGlobalSpace(box.maxX, box.maxY, box.maxZ);
			return entity.getEntityWorld().getEntitiesByType(arg, new Box(newMin, newMax), predicate);
		}));
	}

	private <T> T passCallToEntity(BlockPos pos, T defaultVal, EntityPassthroughFunction<T> consumer) {
		Bay bay = cachedBayData.getBay(cachedBayData.getBayIdFromPos(pos));
		if (bay != null && bay.getLinkedEntity().isPresent()) {
			return consumer.passthrough(bay.getLinkedEntity().get(), bay);
		}
		return defaultVal;
	}

	private void passCallToEntity(BlockPos pos, EntityPassthroughConsumer consumer) {
		Bay bay = cachedBayData.getBay(cachedBayData.getBayIdFromPos(pos));
		if (bay != null && bay.getLinkedEntity().isPresent()) {
			consumer.passthrough(bay.getLinkedEntity().get(), bay);
		}
	}

	@Override
	public boolean spawnEntity(Entity entity) {
		return passCallToEntity(entity.getBlockPos(), false, (worldLinkEntity, bay) -> {
			Vec3d newPos = bay.toEntityGlobalSpace(entity.getPos());
			entity.setPosition(newPos.x, newPos.y, newPos.z);
			entity.world = worldLinkEntity.getEntityWorld();
			var vel = entity.getVelocity();
			var vec = bay.getLinkedEntity().get().getRotation().multLocal(new Vector3d(vel.x, vel.y, vel.z));
			entity.setVelocity(new Vec3d(vec.x, vec.y, vec.z));
			return worldLinkEntity.getEntityWorld().spawnEntity(entity);
		});
	}

	@Override
	public void playSound(@Nullable PlayerEntity player, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
		passCallToEntity(new BlockPos(x, y, z), (entity, bay) -> {
			Vec3d newPos = bay.toEntityGlobalSpace(x, y, z);
			entity.getEntityWorld().playSound(null, newPos.x, newPos.y, newPos.z, sound, category, volume, pitch);
		});
	}

	@Override
	public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
		super.syncGlobalEvent(eventId, pos, data);
		passCallToEntity(pos, (entity, bay) -> {
			BlockPos newPos = bay.toEntityGlobalSpace(pos);
			entity.getEntityWorld().syncGlobalEvent(eventId, newPos, data);
		});
	}

	@Override
	public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
		super.syncWorldEvent(player, eventId, pos, data);
		passCallToEntity(pos, (entity, bay) -> {
			BlockPos newPos = bay.toEntityGlobalSpace(pos);
			entity.getEntityWorld().syncWorldEvent(null, eventId, newPos, data);
		});
	}

	@Override
	public void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data) {
		super.addSyncedBlockEvent(pos, block, type, data);
		passCallToEntity(pos, ((entity, bay) -> {
			BlockPos newPos = bay.toLocal(pos);
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeInt(entity.getId());
			buf.writeBlockPos(newPos);
			buf.writeInt(type);
			buf.writeInt(data);
			PlayerLookup.tracking(entity).forEach(player -> ServerPlayNetworking.send(player, WSNetworking.SHELL_BLOCK_EVENT, buf));
		}));
	}

	@Override
	public <T extends ParticleEffect> int spawnParticles(T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
		return passCallToEntity(new BlockPos(x, y, z), 0, (entity, bay) -> {
			Vec3d newPos = bay.toEntityGlobalSpace(x, y, z);
			return ((ServerWorld) entity.getEntityWorld()).spawnParticles(particle, newPos.x, newPos.y, newPos.z, count, deltaX, deltaY, deltaZ, speed);
		});
	}

	@Override
	public <T extends ParticleEffect> boolean spawnParticles(ServerPlayerEntity viewer, T particle, boolean force, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
		return passCallToEntity(new BlockPos(x, y, z), false, (entity, bay) -> {
			Vec3d newPos = bay.toEntityGlobalSpace(x, y, z);
			return ((ServerWorld) entity.getEntityWorld()).spawnParticles(viewer, particle, force, newPos.x, newPos.y, newPos.z, count, deltaX, deltaY, deltaZ, speed);
		});
	}

	@Override
	public void syncWorldEvent(int eventId, BlockPos pos, int data) {
		super.syncWorldEvent(eventId, pos, data);
		passCallToEntity(pos, (entity, bay) -> {
			BlockPos newPos = bay.toEntityGlobalSpace(pos);
			entity.getEntityWorld().syncWorldEvent(eventId, newPos, data);
		});
	}

	public ShellStorageData getCachedBayData() {
		return cachedBayData;
	}

	public void setCachedBayData(ShellStorageData cachedBayData) {
		this.cachedBayData = cachedBayData;
	}

	public interface EntityPassthroughConsumer {
		void passthrough(WorldShellEntity entity, Bay bay);
	}

	public interface EntityPassthroughFunction<T> {
		T passthrough(WorldShellEntity entity, Bay bay);
	}
}