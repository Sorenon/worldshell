package net.snakefangox.worldshell.mixin.passthrough;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Set;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

	@Shadow
	@Final
	private float power;

	@Shadow
	@Final
	private boolean createFire;

	@Shadow
	@Final
	private double x;

	@Shadow
	@Final
	private double y;

	@Shadow
	@Final
	private double z;

	@Shadow
	@Final
	private Explosion.DestructionType destructionType;

	@Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;squaredDistanceTo(Lnet/minecraft/util/math/Vec3d;)D", ordinal = 0),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	public void collectBlocksAndDamageEntities(CallbackInfo ci, Set set, int q, float r, int s, int t, int u, int v, int w, int l, List list, Vec3d vec3d, int k, Entity entity) {
		if (entity instanceof WorldShellEntity) {
			((WorldShellEntity) entity).passThroughExplosion(x, y, z, power, createFire, destructionType);
		}
	}
}
