package com.benmatt.moretools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Shared logic for tools that break more than one block at a time. */
public final class AreaMining {
    private AreaMining() {}

    /** Set while we are breaking extra blocks, so those breaks don't trigger another round. */
    private static final ThreadLocal<Boolean> BUSY = ThreadLocal.withInitial(() -> false);

    public static final int MAX_TREE_LOGS = 256;

    /** True if the area effect should run for this break. */
    private static boolean shouldRun(Level level, Object miner) {
        return !level.isClientSide()
            && !BUSY.get()
            && miner instanceof ServerPlayer player
            && !player.isShiftKeyDown();
    }

    /**
     * Breaks the 3x3 square around {@code origin}, facing the side of the block the player was mining.
     * Only blocks this tool can properly mine are broken, and blocks much harder than the
     * original block (e.g. obsidian next to stone) are skipped.
     */
    public static void breakSquare(ItemStack stack, Level level, BlockState originState, BlockPos origin, Object miner) {
        if (!shouldRun(level, miner)) return;
        ServerPlayer player = (ServerPlayer) miner;

        Direction face = facingSide(player);
        float originHardness = originState.getDestroySpeed(level, origin);

        List<BlockPos> targets = new ArrayList<>();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos pos = switch (face.getAxis()) {
                    case X -> origin.offset(0, a, b);
                    case Y -> origin.offset(a, 0, b);
                    case Z -> origin.offset(a, b, 0);
                };
                targets.add(pos);
            }
        }

        BUSY.set(true);
        try {
            for (BlockPos pos : targets) {
                if (stack.isEmpty()) break; // tool broke
                BlockState state = level.getBlockState(pos);
                if (state.isAir()) continue;
                float hardness = state.getDestroySpeed(level, pos);
                if (hardness < 0) continue; // unbreakable, like bedrock
                if (hardness > originHardness + 3.0F) continue;
                if (!stack.isCorrectToolForDrops(state) || stack.getDestroySpeed(state) <= 1.0F) continue;
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            BUSY.set(false);
        }
    }

    /**
     * Chops down the whole tree that {@code origin} belongs to. Only runs when natural
     * (non player-placed) leaves are touching the logs, so log cabins are safe.
     */
    public static void fellTree(ItemStack stack, Level level, BlockState originState, BlockPos origin, Object miner) {
        if (!shouldRun(level, miner)) return;
        if (!originState.is(BlockTags.LOGS)) return;
        ServerPlayer player = (ServerPlayer) miner;

        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> logs = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(origin);
        queue.add(origin);
        boolean foundLeaves = false;

        while (!queue.isEmpty() && logs.size() < MAX_TREE_LOGS) {
            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos next = current.offset(dx, dy, dz);
                        if (!seen.add(next)) continue;
                        BlockState state = level.getBlockState(next);
                        if (state.is(BlockTags.LOGS)) {
                            logs.add(next);
                            queue.add(next);
                        } else if (!foundLeaves && isNaturalLeaves(state)) {
                            foundLeaves = true;
                        }
                    }
                }
            }
        }

        if (!foundLeaves || logs.isEmpty()) return;

        logs.sort(Comparator.comparingDouble(p -> p.distSqr(origin)));
        BUSY.set(true);
        try {
            for (BlockPos pos : logs) {
                if (stack.isEmpty()) break; // tool broke
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            BUSY.set(false);
        }
    }

    private static boolean isNaturalLeaves(BlockState state) {
        if (!state.is(BlockTags.LEAVES)) return false;
        return !state.hasProperty(LeavesBlock.PERSISTENT) || !state.getValue(LeavesBlock.PERSISTENT);
    }

    /** Which side of the block the player is looking at. */
    private static Direction facingSide(ServerPlayer player) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getDirection();
        }
        return Direction.orderedByNearest(player)[0];
    }
}
