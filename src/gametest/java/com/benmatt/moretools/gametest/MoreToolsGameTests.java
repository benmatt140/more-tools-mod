package com.benmatt.moretools.gametest;

import com.benmatt.moretools.MoreTools;
import com.benmatt.moretools.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Consumer;

/**
 * Automated in-game tests. These are NOT part of the released mod: the CI build copies this
 * folder into the project only when running Minecraft's game test server.
 */
public final class MoreToolsGameTests {
    private MoreToolsGameTests() {}

    public static void register(BusGroup modBus) {
        RegisterEvent.getBus(modBus).addListener(event -> {
            if (event.getRegistryKey() != Registries.TEST_FUNCTION) return;
            add(event, "hammer_mines_3x3", MoreToolsGameTests::hammerMines3x3);
            add(event, "hammer_sneak_mines_one", MoreToolsGameTests::hammerSneakMinesOne);
            add(event, "excavator_digs_3x3", MoreToolsGameTests::excavatorDigs3x3);
            add(event, "lumber_axe_fells_tree", MoreToolsGameTests::lumberAxeFellsTree);
            add(event, "lumber_axe_spares_cabins", MoreToolsGameTests::lumberAxeSparesCabins);
            add(event, "paxel_mines_everything", MoreToolsGameTests::paxelMinesEverything);
            add(event, "tier_tools_work", MoreToolsGameTests::tierToolsWork);
        });
    }

    private static void add(RegisterEvent event, String name, Consumer<GameTestHelper> test) {
        event.register(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(MoreTools.MODID, name), () -> test);
    }

    private static ServerPlayer player(GameTestHelper helper, Item tool, Vec3 relativeEyePos, float yRot, float xRot) {
        ServerPlayer player = helper.makeMockServerPlayerFull(GameType.SURVIVAL);
        Vec3 abs = helper.absoluteVec(relativeEyePos);
        player.setPos(abs.x, abs.y - player.getEyeHeight(), abs.z);
        player.setYRot(yRot);
        player.setXRot(xRot);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(tool));
        return player;
    }

    private static void fillWallZ2(GameTestHelper helper, Block block) {
        for (int x = 0; x <= 2; x++)
            for (int y = 1; y <= 3; y++)
                helper.setBlock(new BlockPos(x, y, 2), block);
    }

    /** Hammer facing a stone wall breaks all 9 stone blocks, but skips dirt and much harder obsidian. */
    public static void hammerMines3x3(GameTestHelper helper) {
        fillWallZ2(helper, Blocks.STONE);
        helper.setBlock(new BlockPos(0, 3, 2), Blocks.DIRT);
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.OBSIDIAN);
        // stand in front of the wall, looking south (+Z) at its middle
        ServerPlayer player = player(helper, ModItems.DIAMOND_HAMMER.get(), new Vec3(1.5, 2.5, 0.3), 0F, 0F);
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 2, 2)));

        for (int x = 0; x <= 2; x++)
            for (int y = 1; y <= 3; y++) {
                BlockPos p = new BlockPos(x, y, 2);
                if (p.equals(new BlockPos(0, 3, 2))) helper.assertBlockPresent(Blocks.DIRT, p);
                else if (p.equals(new BlockPos(2, 3, 2))) helper.assertBlockPresent(Blocks.OBSIDIAN, p);
                else helper.assertBlockPresent(Blocks.AIR, p);
            }
        ItemStack held = player.getMainHandItem();
        helper.assertTrue(held.getDamageValue() == 7, "Hammer should take 1 damage per block (7), took " + held.getDamageValue());
        helper.succeed();
    }

    /** Sneaking with a hammer mines a single block. */
    public static void hammerSneakMinesOne(GameTestHelper helper) {
        fillWallZ2(helper, Blocks.STONE);
        ServerPlayer player = player(helper, ModItems.IRON_HAMMER.get(), new Vec3(1.5, 2.5, 0.3), 0F, 0F);
        player.setShiftKeyDown(true);
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 2, 2)));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 2, 2));
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, 2, 2));
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(1, 3, 2));
        helper.succeed();
    }

    /** Excavator looking down at a dirt floor digs 3x3, but leaves stone alone. */
    public static void excavatorDigs3x3(GameTestHelper helper) {
        for (int x = 0; x <= 2; x++)
            for (int z = 0; z <= 2; z++)
                helper.setBlock(new BlockPos(x, 1, z), Blocks.DIRT);
        helper.setBlock(new BlockPos(0, 1, 0), Blocks.STONE);
        ServerPlayer player = player(helper, ModItems.NETHERITE_EXCAVATOR.get(), new Vec3(1.5, 3.5, 1.5), 0F, 90F);
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 1, 1)));
        for (int x = 0; x <= 2; x++)
            for (int z = 0; z <= 2; z++) {
                BlockPos p = new BlockPos(x, 1, z);
                helper.assertBlockPresent(x == 0 && z == 0 ? Blocks.STONE : Blocks.AIR, p);
            }
        helper.succeed();
    }

    /** Lumber axe on the bottom log of a tree removes the whole trunk. */
    public static void lumberAxeFellsTree(GameTestHelper helper) {
        for (int y = 1; y <= 4; y++) helper.setBlock(new BlockPos(1, y, 1), Blocks.OAK_LOG);
        helper.setBlock(new BlockPos(2, 4, 2), Blocks.OAK_LOG); // a branch
        helper.setBlock(new BlockPos(1, 5, 1), Blocks.OAK_LEAVES);
        helper.setBlock(new BlockPos(0, 4, 1), Blocks.OAK_LEAVES);
        ServerPlayer player = player(helper, ModItems.DIAMOND_LUMBER_AXE.get(), new Vec3(1.5, 1.5, -0.5), 0F, 0F);
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 1, 1)));
        for (int y = 1; y <= 4; y++) helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, y, 1));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 4, 2));
        helper.succeed();
    }

    /** Logs with no natural leaves (e.g. a log cabin) are not chopped down. */
    public static void lumberAxeSparesCabins(GameTestHelper helper) {
        for (int y = 1; y <= 3; y++) helper.setBlock(new BlockPos(1, y, 1), Blocks.OAK_LOG);
        ServerPlayer player = player(helper, ModItems.IRON_LUMBER_AXE.get(), new Vec3(1.5, 1.5, -0.5), 0F, 0F);
        player.gameMode.destroyBlock(helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 1, 1));
        helper.assertBlockPresent(Blocks.OAK_LOG, new BlockPos(1, 2, 1));
        helper.assertBlockPresent(Blocks.OAK_LOG, new BlockPos(1, 3, 1));
        helper.succeed();
    }

    /** Paxels are the right tool for stone, wood and dirt, and mine them fast. */
    public static void paxelMinesEverything(GameTestHelper helper) {
        ItemStack paxel = new ItemStack(ModItems.DIAMOND_PAXEL.get());
        for (Block b : new Block[] {Blocks.STONE, Blocks.DIAMOND_ORE, Blocks.OAK_LOG, Blocks.DIRT, Blocks.SAND}) {
            var state = b.defaultBlockState();
            helper.assertTrue(paxel.isCorrectToolForDrops(state), "Paxel should be correct tool for " + b);
            helper.assertTrue(paxel.getDestroySpeed(state) > 1.0F, "Paxel should mine " + b + " quickly");
        }
        helper.assertFalse(paxel.isCorrectToolForDrops(Blocks.COBWEB.defaultBlockState()) && paxel.getDestroySpeed(Blocks.COBWEB.defaultBlockState()) > 10F,
            "Paxel should not act like a sword on cobwebs");
        helper.succeed();
    }

    /** Spot-check the new material tiers. */
    public static void tierToolsWork(GameTestHelper helper) {
        Item obsidianPick = ForgeRegistries.ITEMS.getValue(Identifier.fromNamespaceAndPath(MoreTools.MODID, "obsidian_pickaxe"));
        Item amethystPick = ForgeRegistries.ITEMS.getValue(Identifier.fromNamespaceAndPath(MoreTools.MODID, "amethyst_pickaxe"));
        ItemStack obs = new ItemStack(obsidianPick);
        ItemStack ame = new ItemStack(amethystPick);
        helper.assertTrue(obs.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "Obsidian pickaxe should mine diamond ore");
        helper.assertTrue(obs.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "Obsidian pickaxe should mine obsidian");
        helper.assertTrue(ame.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "Amethyst pickaxe should mine diamond ore");
        helper.assertFalse(ame.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "Amethyst pickaxe should NOT mine obsidian");
        helper.assertTrue(ame.getDestroySpeed(Blocks.STONE.defaultBlockState()) > obs.getDestroySpeed(Blocks.STONE.defaultBlockState()),
            "Amethyst should be faster than obsidian");
        helper.assertTrue(obs.getMaxDamage() == 2800, "Obsidian durability should be 2800, was " + obs.getMaxDamage());
        helper.succeed();
    }
}
