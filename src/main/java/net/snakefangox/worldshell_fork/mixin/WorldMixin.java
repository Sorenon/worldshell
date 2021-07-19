package net.snakefangox.worldshell_fork.mixin;

import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.snakefangox.worldshell_fork.kevlar.PhysicsWorld;
import net.snakefangox.worldshell_fork.mixinextras.WorldExt;
import net.snakefangox.worldshell_fork.world.DelegateWorld;
import net.snakefangox.worldshell_fork.world.ShellStorageWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(World.class)
public class WorldMixin implements WorldExt {

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("GrappleShip");

    @Unique
    private PhysicsWorld physicsWorld = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    void init(MutableWorldProperties properties, RegistryKey<World> registryRef, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, CallbackInfo ci) {
        //noinspection ConstantConditions
        if (!(
                (Object) this instanceof DelegateWorld || (Object) this instanceof ShellStorageWorld
        )) {
            physicsWorld = new PhysicsWorld();

            LOGGER.info("Creating physics world:" + registryRef.toString() + "#" + this.getClass());
        }
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    void tick(CallbackInfo ci) {
        if (physicsWorld != null) {
            physicsWorld.dynamicsWorld.stepSimulation(1/20f, 0, 1 / 20f);
        }
    }

    @Inject(method = "close", at = @At("RETURN"))
    void close(CallbackInfo ci) {
        if (physicsWorld != null) {

            LOGGER.info("Closing  physics world:" + toString() + "#" + this.getClass());

            physicsWorld.dispose();
        }
    }

    @Override
    public PhysicsWorld getPhysics() {
        return physicsWorld;
    }
}
