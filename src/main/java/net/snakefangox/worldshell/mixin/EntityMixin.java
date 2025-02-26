package net.snakefangox.worldshell.mixin;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.MovementType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.kevlar.KevlarContactResultCallback;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;
import net.snakefangox.worldshell.mixinextras.WorldExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public EntityDimensions dimensions;

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public World world;

    @Shadow
    public abstract void setPosition(Vec3d pos);

    @Shadow
    public abstract void setVelocity(Vec3d velocity);

    @Shadow
    public abstract Vec3d getVelocity();

    @Shadow
    protected boolean onGround;
    @Shadow
    public boolean verticalCollision;
    @Shadow
    private Vec3d velocity;

    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    public boolean horizontalCollision;

    @Shadow
    public abstract Box getBoundingBox();

    @Shadow
    public boolean noClip;

    @Inject(method = "move", at = @At("RETURN"))
    void colMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        //noinspection ConstantConditions
//        if ((Object) this instanceof PlayerEntity) {
//            if (colObj == null) {
//                colObj = new btCollisionObject();
//            }
        //noinspection ConstantConditions
        if (this.noClip || (Object) this instanceof WorldShellEntity ||
                world.getEntitiesByType(TypeFilter.instanceOf(WorldShellEntity.class), this.getBoundingBox(), e -> true).size() == 0)
            return;

        var colObj = new btCollisionObject();
        var callback = new KevlarContactResultCallback();
        callback.obj = colObj;

        PhysicsWorld physicsWorld = ((WorldExt) world).getPhysics();

        callback.callback = (normalFromOther, penetration) -> {
            Vector3 offset = normalFromOther.scl(penetration).scl(0, 1, 0);

            if (offset.len2() == 0) {
                return;
            }

            this.setPosition(this.getPos().add(offset.x, offset.y, offset.z));

            Matrix4 mat = colObj.getWorldTransform();
            mat.translate(offset);
            colObj.setWorldTransform(mat);

            if (offset.y > 0) {
                this.onGround = true;
            }
            if (offset.y > 0 && velocity.y < 0 || offset.y < 0 && velocity.y > 0) {
                this.setVelocity(this.getVelocity().multiply(1, 0, 1));
                this.verticalCollision = true;
            }
        };

        Box box = this.dimensions.getBoxAt(Vec3d.ZERO);
        Matrix4 translation = new Matrix4();
        translation.setToTranslation((float) this.getX(), (float) this.getY() + (float) box.getYLength() / 2, (float) this.getZ());
        colObj.setWorldTransform(translation);
        colObj.setCollisionShape(physicsWorld.getOrMakeBoxShape(box));

        physicsWorld.dynamicsWorld.contactTest(colObj, callback);

        callback.done.clear();
        callback.callback = (normalFromOther, penetration) -> {
            Vector3 offset = normalFromOther.scl(penetration);

            this.setPosition(this.getPos().add(offset.x, offset.y, offset.z));

            Matrix4 mat = colObj.getWorldTransform();
            mat.translate(offset);
            colObj.setWorldTransform(mat);

            if (offset.y > 0) {
                this.onGround = true;
            }
            if (offset.y > 0 && velocity.y < 0 || offset.y < 0 && velocity.y > 0) {
                this.setVelocity(this.getVelocity().multiply(1, 0, 1));
                this.verticalCollision = true;
            }
            if (offset.x > 0 && velocity.x < 0 || offset.x < 0 && velocity.x > 0) {
                this.setVelocity(this.getVelocity().multiply(0, 1, 1));
                this.horizontalCollision = true;
            }
            if (offset.z > 0 && velocity.z < 0 || offset.z < 0 && velocity.z > 0) {
                this.setVelocity(this.getVelocity().multiply(1, 1, 0));
                this.horizontalCollision = true;
            }
        };
        physicsWorld.dynamicsWorld.contactTest(colObj, callback);

        callback.dispose();
        colObj.dispose();

        //noinspection ConstantConditions
        if (this.onGround && (Object)this instanceof ServerPlayerEntity pe) {
            ((ServerPlayNetworkHandlerAcc)pe.networkHandler).setFloatingTicks(0);
        }
    }
}
