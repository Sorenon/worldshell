package net.snakefangox.worldshell.storage;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;

/**
 * Should be implemented by any block you want to receive worldshell related events and notifications.
 * Simply implement any of the default methods here and they will be called when appropriate by the
 * Worldshell or it's constructor.
 */
public interface ShellAwareBlock {

    default ActionResult onUseWorldshell(WorldShellEntity parent, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        return ActionResult.PASS;
    }

    default void onUseInShell(World world, WorldShellEntity entity, PlayerEntity player, Hand hand, BlockHitResult hit){}
}
