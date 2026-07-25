package com.mo.totemofsoulkeeping;

import com.mo.totemofsoulkeeping.item.TotemOfSoulKeepingItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TotemOfSoulKeeping.MOD_ID);

    public static final DeferredItem<Item> TOTEM_OF_SOUL_KEEPING =
            ITEMS.registerItem("totem_of_soul_keeping", TotemOfSoulKeepingItem::new);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
