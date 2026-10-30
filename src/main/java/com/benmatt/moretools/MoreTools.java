package com.benmatt.moretools;

import com.benmatt.moretools.item.ModCreativeTab;
import com.benmatt.moretools.item.ModItems;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MoreTools.MODID)
public final class MoreTools {
    public static final String MODID = "moretools";

    public MoreTools(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        ModItems.ITEMS.register(modBusGroup);
        ModCreativeTab.TABS.register(modBusGroup);
        registerGameTestsIfPresent(modBusGroup);
    }

    /** The automated tests only exist in CI test builds; in the released mod this does nothing. */
    private static void registerGameTestsIfPresent(BusGroup modBusGroup) {
        try {
            Class.forName("com.benmatt.moretools.gametest.MoreToolsGameTests")
                .getMethod("register", BusGroup.class)
                .invoke(null, modBusGroup);
        } catch (ClassNotFoundException ignored) {
            // normal release build
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register More Tools game tests", e);
        }
    }
}
