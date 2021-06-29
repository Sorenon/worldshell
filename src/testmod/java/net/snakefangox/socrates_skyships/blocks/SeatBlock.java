package net.snakefangox.socrates_skyships.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.ShellAwareBlock;

public class SeatBlock extends Block implements ShellAwareBlock {
    public SeatBlock() {
        super(Settings.of(Material.WOOD));
    }

    @Override
    public ActionResult onUseWorldshell(WorldShellEntity parent, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            return player.startRiding(parent) ? ActionResult.CONSUME : ActionResult.PASS;
        } else {
            return ActionResult.SUCCESS;
        }
    }
}
