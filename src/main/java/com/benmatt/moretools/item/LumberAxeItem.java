package com.benmatt.moretools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** An axe that chops down a whole tree at once. Sneak to chop a single log. Still strips logs. */
public class LumberAxeItem extends Item {
    public LumberAxeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);
        AreaMining.fellTree(stack, level, state, pos, miner);
        return result;
    }
}
