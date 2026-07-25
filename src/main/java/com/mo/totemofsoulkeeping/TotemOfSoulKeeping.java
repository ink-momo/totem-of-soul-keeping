package com.mo.totemofsoulkeeping;

import com.mojang.logging.LogUtils;
import com.mo.totemofsoulkeeping.event.DeathEventHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(TotemOfSoulKeeping.MOD_ID)
public class TotemOfSoulKeeping {

    public static final String MOD_ID = "totem_of_soul_keeping";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TotemOfSoulKeeping(IEventBus modBus, ModContainer modContainer) {
        ModItems.register(modBus);
        modBus.addListener(this::onBuildCreativeTab);

        modContainer.registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC, "totem-of-soul-keeping-common.toml");

        NeoForge.EVENT_BUS.register(DeathEventHandler.class);
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.TOTEM_OF_SOUL_KEEPING);
        }
    }
}
