package net.snakefangox.worldshell_fork.mixinextras;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.snakefangox.worldshell_fork.util.DynamicWorldRegister;
import net.snakefangox.worldshell_fork.world.ServerWorldSupplier;

/**
 * Contains all the methods used to register dynamic dimensions, check the static helper class for more info: @{@link DynamicWorldRegister}
 */
public interface DynamicWorldGen {

	ServerWorld worldshell$createDynamicWorld(RegistryKey<World> worldRegistryKey, DimensionOptions dimensionOptions);

	ServerWorld worldshell$createDynamicWorld(RegistryKey<World> worldRegistryKey, RegistryKey<DimensionType> dimensionTypeKey, ChunkGenerator chunkGenerator);

	ServerWorld worldshell$createDynamicWorld(RegistryKey<World> worldRegistryKey, DimensionOptions dimensionOptions, ServerWorldSupplier worldSupplier);
}
