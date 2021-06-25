package net.snakefangox.worldshell.mixin;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell.kevlar.KevlarContactResultCallback;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract Box getBoundingBox();

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
    public abstract void updatePosition(double x, double y, double z);

    @Shadow
    public abstract void setPos(double x, double y, double z);

    @Shadow
    public abstract void setPosition(Vec3d pos);

    @Shadow
    private Vec3d pos;

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

    @Shadow public boolean horizontalCollision;
    private static btCollisionObject playerObj = null;

    @Inject(method = "move", at = @At("RETURN"))
    void colMove(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) {
//            PhysicsWorld.STATIC.dynamicsWorld.convexSweepTest();
            if (playerObj == null) {
                playerObj = new btCollisionObject();
            }

            var callback = new KevlarContactResultCallback();

            if (world.isClient) {
                callback.obj = playerObj;
                callback.callback = (normalFromOther, distance) -> {
//                    System.out.println("dist:" + distance + ", normal:" + normalFromOther);

                    Vector3 offset = normalFromOther.scl(distance);

                    this.setPosition(this.getPos().add(offset.x, offset.y, offset.z));

                    Matrix4 mat = playerObj.getWorldTransform();
                    mat.translate(offset);
                    playerObj.setWorldTransform(mat);

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

                Box box = this.dimensions.getBoxAt(Vec3d.ZERO).expand(0.1);
                Matrix4 translation = new Matrix4();
                translation.setToTranslation((float) this.getX(), (float) this.getY() + (float) box.getYLength() / 2, (float) this.getZ());
                playerObj.setWorldTransform(translation);
                playerObj.setCollisionShape(PhysicsWorld.CLIENT.getOrMakeBoxShape(box));

                PhysicsWorld.CLIENT.dynamicsWorld.contactTest(playerObj, callback);
            }

            callback.dispose();
        }
    }
}
