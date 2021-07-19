package net.snakefangox.worldshell_fork.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.snakefangox.worldshell_fork.WorldShellMain;
import net.snakefangox.worldshell_fork.mixin.entitytracking.ClientEntityManagerAccess;
import net.snakefangox.worldshell_fork.mixin.entitytracking.ClientWorldAccess;
import net.snakefangox.worldshell_fork.mixin.entitytracking.ServerEntityManagerAccess;
import net.snakefangox.worldshell_fork.mixin.entitytracking.ServerWorldAccess;
import net.snakefangox.worldshell_fork.mixinextras.WorldShellEntityTracker;

public class SidedEntityManagerHandler {

	public static void addWorldShellEntity(World world, WorldShellEntity entity, long pos) {
		if (world.isClient()) {
			addWorldShellEntityClient((ClientWorld) world, entity, pos);
		} else {
			addWorldShellEntityServer((ServerWorld) world, entity, pos);
		}
	}

	private static void addWorldShellEntityClient(ClientWorld world, WorldShellEntity entity, long pos) {
		ClientEntityManager<Entity> entityManager = ((ClientWorldAccess) world).getEntityManager();
		EntityTrackingSection<? extends EntityLike> section = ((ClientEntityManagerAccess) entityManager).getCache().getTrackingSection(pos);
		((WorldShellEntityTracker) section).addWorldShellEntity(entity);
	}

	private static void addWorldShellEntityServer(ServerWorld world, WorldShellEntity entity, long pos) {
		ServerEntityManager<Entity> entityManager = ((ServerWorldAccess) world).getEntityManager();
		EntityTrackingSection<? extends EntityLike> section = ((ServerEntityManagerAccess) entityManager).getCache().getTrackingSection(pos);
		((WorldShellEntityTracker) section).addWorldShellEntity(entity);
	}

	public static void removeWorldShellEntity(World world, WorldShellEntity entity, long pos) {
		if (world.isClient()) {
			removeWorldShellEntityClient((ClientWorld) world, entity, pos);
		} else {
			removeWorldShellEntityServer((ServerWorld) world, entity, pos);
		}
	}

	@Environment(EnvType.CLIENT)
	private static void removeWorldShellEntityClient(ClientWorld world, WorldShellEntity entity, long pos) {
		ClientEntityManager<Entity> entityManager = ((ClientWorldAccess) world).getEntityManager();
		EntityTrackingSection<? extends EntityLike> section = ((ClientEntityManagerAccess) entityManager).getCache().getTrackingSection(pos);
		boolean success = ((WorldShellEntityTracker) section).removeWorldShellEntity(entity);
		((ClientEntityManagerAccess) entityManager).invokeRemoveIfEmpty(pos, section);
		if (!success) WorldShellMain.LOGGER.warn("Worldshell {} wasn't found in section {}", entity, section);
	}

	private static void removeWorldShellEntityServer(ServerWorld world, WorldShellEntity entity, long pos) {
		ServerEntityManager<Entity> entityManager = ((ServerWorldAccess) world).getEntityManager();
		EntityTrackingSection<? extends EntityLike> section = ((ServerEntityManagerAccess) entityManager).getCache().getTrackingSection(pos);
		boolean success = ((WorldShellEntityTracker) section).removeWorldShellEntity(entity);
		((ServerEntityManagerAccess) entityManager).invokeEntityLeftSection(pos, section);
		if (!success) WorldShellMain.LOGGER.warn("Worldshell {} wasn't found in section {}", entity, section);
	}
}
