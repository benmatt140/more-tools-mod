package com.benmatt.moretools.item;

import com.benmatt.moretools.MoreTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MoreTools.MODID);

    /** Blocks a paxel can mine: everything a pickaxe, axe or shovel can. */
    public static final TagKey<Block> MINEABLE_WITH_PAXEL =
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MoreTools.MODID, "mineable/paxel"));

    /** Every item in registration order, used for the creative tab. */
    private static final List<RegistryObject<Item>> ALL = new ArrayList<>();

    public static List<RegistryObject<Item>> all() {
        return Collections.unmodifiableList(ALL);
    }

    // ---- New material tool sets ----
    static {
        toolSet("emerald", ModToolMaterials.EMERALD);
        toolSet("obsidian", ModToolMaterials.OBSIDIAN);
        toolSet("amethyst", ModToolMaterials.AMETHYST);
    }

    // ---- Hammers: mine 3x3 ----
    public static final RegistryObject<Item> IRON_HAMMER = hammer("iron_hammer", ToolMaterial.IRON, false);
    public static final RegistryObject<Item> DIAMOND_HAMMER = hammer("diamond_hammer", ToolMaterial.DIAMOND, false);
    public static final RegistryObject<Item> NETHERITE_HAMMER = hammer("netherite_hammer", ToolMaterial.NETHERITE, true);

    // ---- Excavators: dig 3x3 ----
    public static final RegistryObject<Item> IRON_EXCAVATOR = excavator("iron_excavator", ToolMaterial.IRON, false);
    public static final RegistryObject<Item> DIAMOND_EXCAVATOR = excavator("diamond_excavator", ToolMaterial.DIAMOND, false);
    public static final RegistryObject<Item> NETHERITE_EXCAVATOR = excavator("netherite_excavator", ToolMaterial.NETHERITE, true);

    // ---- Lumber axes: chop whole trees ----
    public static final RegistryObject<Item> IRON_LUMBER_AXE = lumberAxe("iron_lumber_axe", ToolMaterial.IRON, false);
    public static final RegistryObject<Item> DIAMOND_LUMBER_AXE = lumberAxe("diamond_lumber_axe", ToolMaterial.DIAMOND, false);
    public static final RegistryObject<Item> NETHERITE_LUMBER_AXE = lumberAxe("netherite_lumber_axe", ToolMaterial.NETHERITE, true);

    // ---- Paxels: pickaxe + axe + shovel in one ----
    public static final RegistryObject<Item> DIAMOND_PAXEL = paxel("diamond_paxel", ToolMaterial.DIAMOND, false);
    public static final RegistryObject<Item> NETHERITE_PAXEL = paxel("netherite_paxel", ToolMaterial.NETHERITE, true);

    private static void toolSet(String name, ToolMaterial material) {
        register(name + "_sword", props -> new Item(props.sword(material, 3.0F, -2.4F)));
        register(name + "_pickaxe", props -> new Item(props.pickaxe(material, 1.0F, -2.8F)));
        register(name + "_axe", props -> new AxeItem(material, 5.0F, -3.0F, props));
        register(name + "_shovel", props -> new ShovelItem(material, 1.5F, -3.0F, props));
        register(name + "_hoe", props -> new HoeItem(material, -3.0F, 0.0F, props));
    }

    private static RegistryObject<Item> hammer(String name, ToolMaterial material, boolean fireproof) {
        ToolMaterial big = ModToolMaterials.big(material);
        return register(name, props -> new HammerItem(fire(props, fireproof).pickaxe(big, 5.0F, -3.3F)));
    }

    private static RegistryObject<Item> excavator(String name, ToolMaterial material, boolean fireproof) {
        ToolMaterial big = ModToolMaterials.big(material);
        return register(name, props -> new ExcavatorItem(big, 2.5F, -3.2F, fire(props, fireproof)));
    }

    private static RegistryObject<Item> lumberAxe(String name, ToolMaterial material, boolean fireproof) {
        ToolMaterial big = ModToolMaterials.big(material);
        return register(name, props -> new LumberAxeItem(big, 6.0F, -3.3F, fire(props, fireproof)));
    }

    private static RegistryObject<Item> paxel(String name, ToolMaterial material, boolean fireproof) {
        ToolMaterial combined = ModToolMaterials.paxel(material);
        return register(name, props -> new Item(
            combined.applyToolProperties(fire(props, fireproof), MINEABLE_WITH_PAXEL, 4.0F, -2.8F, 0.0F)));
    }

    private static Item.Properties fire(Item.Properties props, boolean fireproof) {
        return fireproof ? props.fireResistant() : props;
    }

    private static RegistryObject<Item> register(String name, Function<Item.Properties, Item> factory) {
        RegistryObject<Item> item = ITEMS.register(name, () -> factory.apply(new Item.Properties().setId(ITEMS.key(name))));
        ALL.add(item);
        return item;
    }
}
