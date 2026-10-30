package com.benmatt.moretools.item;

import com.benmatt.moretools.MoreTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class ModToolMaterials {
    private ModToolMaterials() {}

    public static final TagKey<Item> EMERALD_REPAIR = itemTag("emerald_tool_materials");
    public static final TagKey<Item> OBSIDIAN_REPAIR = itemTag("obsidian_tool_materials");
    public static final TagKey<Item> AMETHYST_REPAIR = itemTag("amethyst_tool_materials");

    /** Diamond-level mining, a bit faster than diamond, very easy to enchant. */
    public static final ToolMaterial EMERALD = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 8.5F, 3.0F, 22, EMERALD_REPAIR);

    /** Diamond-level mining, slow but extremely durable and hits hard. */
    public static final ToolMaterial OBSIDIAN = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2800, 6.5F, 3.5F, 8, OBSIDIAN_REPAIR);

    /** Iron-level mining, but the fastest tools in the game. Low durability. */
    public static final ToolMaterial AMETHYST = new ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL, 400, 11.0F, 2.0F, 18, AMETHYST_REPAIR);

    /** Big tools (hammers, excavators, lumber axes) last 3x longer but dig a little slower. */
    public static ToolMaterial big(ToolMaterial base) {
        return new ToolMaterial(base.incorrectBlocksForDrops(), base.durability() * 3,
            base.speed() * 0.75F, base.attackDamageBonus(), base.enchantmentValue(), base.repairItems());
    }

    /** Paxels last twice as long as the tools they combine. */
    public static ToolMaterial paxel(ToolMaterial base) {
        return new ToolMaterial(base.incorrectBlocksForDrops(), base.durability() * 2,
            base.speed(), base.attackDamageBonus(), base.enchantmentValue(), base.repairItems());
    }

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MoreTools.MODID, name));
    }
}
