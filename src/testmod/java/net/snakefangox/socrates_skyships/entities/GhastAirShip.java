package net.snakefangox.socrates_skyships.entities;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.socrates_skyships.SRegister;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class GhastAirShip extends WorldShellEntity {
    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int fireballStrength = 1;

    public GhastAirShip(EntityType<?> type, World world) {
        super(type, world, SRegister.AIRSHIP_SETTINGS);
    }

    public boolean isShooting() {
        return this.dataTracker.get(SHOOTING);
    }

    public void setShooting(boolean shooting) {
        this.dataTracker.set(SHOOTING, shooting);
    }

    public int getFireballStrength() {
        return this.fireballStrength;
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOOTING, false);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("ExplosionPower", (byte) this.fireballStrength);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ExplosionPower", 99)) {
            this.fireballStrength = nbt.getByte("ExplosionPower");
        }
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    protected void buildHullShape(PhysicsWorld physicsWorld, Set<Map.Entry<BlockPos, BlockState>> blocks) {
        super.buildHullShape(physicsWorld, blocks);

        var transform = new Matrix4();
        var shape = physicsWorld.getOrMakeBoxShape(new Vector3(4.5f / 2, 4.5f / 2, 4.5f / 2));
        var blockOffset = this.getBlockOffset();
        transform.setTranslation((float) -blockOffset.x, (float) (-2.5f - blockOffset.y), (float) -blockOffset.z);
        btHullShape.addChildShape(transform, shape);
    }

    @Nullable
    @Override
    public Entity getPrimaryPassenger() {
        return getFirstPassenger();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isLogicalSideForUpdatingMovement()) {
            this.updateTrackedPosition(this.getPos());

//            this.updateVelocity();
//            if (this.world.isClient) {
//                this.updatePaddles();
//                this.world.sendPacket(new BoatPaddleStateC2SPacket(this.isPaddleMoving(0), this.isPaddleMoving(1)));
//            }

            Vec3d velocity = getVelocity();
            velocity = velocity.multiply(0.92);

            if (getPrimaryPassenger() instanceof PlayerEntity player) {
                Vec3d look = player.getRotationVec(1.0f);
                look = look.multiply(player.forwardSpeed * 0.04);
                velocity = velocity.add(look);
            }
            this.setVelocity(velocity);
            this.velocityDirty = true;

            this.move(MovementType.SELF, this.getVelocity());
        } else {
            this.setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        }
        if (!this.world.isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        } else {
            return ActionResult.SUCCESS;
        }
    }
}
