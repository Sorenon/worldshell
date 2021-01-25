package net.snakefangox.worldshell.storage;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.WSUniversal;
import net.snakefangox.worldshell.data.RelativeBlockPos;
import net.snakefangox.worldshell.entity.WorldLinkEntity;
import net.snakefangox.worldshell.util.ShellTransferHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A bay in the ship storage sense
 * Stores all the data needed to keep track of a shell in the storage dimension
 */
public class ShellBay {

    //The center of the shell
    private RelativeBlockPos center;

    //Defines the box the shell fits within
    private BlockBox bounds;

    //The entity changes to this shell should propagate to
    private Optional<WorldLinkEntity> linkedEntity = Optional.empty();

    public ShellBay(RelativeBlockPos center, BlockBox bounds) {
        this.center = center;
        this.bounds = bounds;
    }

    public ShellBay(CompoundTag tag) {
        fromTag(tag);
    }

    public void sendToClient(PlayerEntity target, MinecraftServer server) {
        World world = server.getWorld(WSUniversal.STORAGE_DIM);
        Map<BlockState, List<BlockPos>> stateListMap = new HashMap<>();
        List<BlockEntity> blockEntities = new ArrayList<>();
        ShellTransferHandler.forEachInBox(bounds, (bp) -> {
            BlockState state = world.getBlockState(bp);
            if (!state.isAir()) {
            	if (stateListMap.containsKey(state)){
            		stateListMap.get(state).add(bp.toImmutable());
				}else{
            		List<BlockPos> list = new ArrayList<BlockPos>();
            		list.add(bp.toImmutable());
            		stateListMap.put(state, list);
				}
				if (state.hasBlockEntity()){
					BlockEntity be = world.getBlockEntity(bp);
					if (be != null && be.toUpdatePacket() != null) blockEntities.add(be);
				}
            }
        });
        
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("center", center.asLong());
        tag.put("bounds", bounds.toNbt());
        return tag;
    }

    public void fromTag(CompoundTag tag) {
        center = RelativeBlockPos.fromLong(tag.getLong("center"));
        bounds = new BlockBox(((IntArrayTag) tag.get("bounds")).getIntArray());
    }

    public BlockBox getBox() {
        return bounds;
    }

    public void linkEntity(@NotNull WorldLinkEntity worldLinkEntity) {
        linkedEntity = Optional.of(worldLinkEntity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShellBay)) return false;

        ShellBay shellBay = (ShellBay) o;

        if (center != null ? !center.equals(shellBay.center) : shellBay.center != null) return false;
        if (bounds != null ? !bounds.equals(shellBay.bounds) : shellBay.bounds != null) return false;
        return linkedEntity.equals(shellBay.linkedEntity);
    }

    @Override
    public int hashCode() {
        int result = center != null ? center.hashCode() : 0;
        result = 31 * result + (bounds != null ? bounds.hashCode() : 0);
        result = 31 * result + linkedEntity.hashCode();
        return result;
    }
}
