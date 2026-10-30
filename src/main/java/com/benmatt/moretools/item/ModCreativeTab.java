package com.benmatt.moretools.item;

import com.benmatt.moretools.MoreTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTab {
    private ModCreativeTab() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoreTools.MODID);

    public static final RegistryObject<CreativeModeTab> MORE_TOOLS = TABS.register("more_tools", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.moretools"))
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> ModItems.DIAMOND_HAMMER.get().getDefaultInstance())
        .displayItems((params, output) -> {
            for (var item : ModItems.all()) {
                output.accept(item.get());
            }
        })
        .build());
}
