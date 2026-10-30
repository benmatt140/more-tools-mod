package com.benmatt.moretools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** A shovel that digs a 3x3 area. Sneak to dig a single block. Still makes paths. */
public class ExcavatorItem extends Item {
    public ExcavatorItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        AreaMining.breakSquare(stack, level, state, pos, miner);
        return result;
    }
}
