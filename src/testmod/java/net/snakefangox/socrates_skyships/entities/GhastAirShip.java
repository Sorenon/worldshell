package net.snakefangox.socrates_skyships.entities;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.socrates_skyships.SRegister;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;

import java.util.Map;
import java.util.Set;

public class GhastAirShip extends WorldShellEntity {
    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int fireballStrength = 1;

    public GhastAirShip(EntityType<?> type, World world) {
        super(type, world, SRegister.AIRSHIP_SETTINGS);
    }

    public boolean isShooting() {
        return (Boolean)this.dataTracker.get(SHOOTING);
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
        nbt.putByte("ExplosionPower", (byte)this.fireballStrength);
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
        transform.setTranslation((float) -blockOffset.x, (float) (-2.5f -blockOffset.y), (float) -blockOffset.z);
        btHullShape.addChildShape(transform, shape);
    }
}
