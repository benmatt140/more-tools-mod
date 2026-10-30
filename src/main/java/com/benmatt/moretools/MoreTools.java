package com.benmatt.moretools;

import com.benmatt.moretools.item.ModCreativeTab;
import com.benmatt.moretools.item.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MoreTools.MODID)
public final class MoreTools {
    public static final String MODID = "moretools";

    public MoreTools(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        ModItems.ITEMS.register(modBusGroup);
        ModCreativeTab.TABS.register(modBusGroup);
    }
}
