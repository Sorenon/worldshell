package net.snakefangox.worldshell_fork.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.snakefangox.worldshell_fork.WorldShellMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public class DimTypeMixin {

    @Inject(method = "addRegistryDefaults", at = @At("RETURN"))
    private static void registerCustomDim(DynamicRegistryManager.Impl registryManager, CallbackInfoReturnable<DynamicRegistryManager.Impl> cir) {
        MutableRegistry<DimensionType> mutableRegistry = registryManager.getMutable(Registry.DIMENSION_TYPE_KEY);

        mutableRegistry.add(WorldShellMain.STORAGE_DIM_TYPE_KEY, WorldShellMain.STORAGE_DIM_TYPE, Lifecycle.stable());
    }
}
