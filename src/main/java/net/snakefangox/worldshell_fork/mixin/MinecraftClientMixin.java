package net.snakefangox.worldshell_fork.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.snakefangox.worldshell_fork.mixinextras.WorldExt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow @Nullable public ClientWorld world;

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("GrappleShip");

    @Inject(method = "joinWorld", at = @At("HEAD"))
    void joinWorld(ClientWorld world, CallbackInfo ci) {
        if (this.world != null) {
            LOGGER.info("Closing physics world:" + world.toString() + "#" + world.getClass());
            ((WorldExt) this.world).getPhysics().dispose();
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    void disconnect(CallbackInfo ci) {
        if (this.world != null) {
            LOGGER.info("Closing physics world:" + world.toString() + "#" + world.getClass());
            ((WorldExt) this.world).getPhysics().dispose();
        }
    }
}
