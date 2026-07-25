package com.mo.totemofsoulkeeping.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Optional;

/**
 * Curios 兼容层。
 *
 * <p>所有方法在调用前都会通过 {@link #isLoaded()} 检查 Curios 是否已安装，
 * 因此本模组不会将 Curios 作为前置依赖：未安装 Curios 时所有方法均为空操作，
 * 本模组行为与原版完全一致。</p>
 *
 * <p>NBT 结构（写入到救援数据的复合标签下）：
 * <pre>
 * CuriosSlots: {
 *     "&lt;identifier&gt;": {           // 槽位类型标识符，如 "ring"、"necklace"
 *         "&lt;index&gt;": &lt;ItemStack NBT&gt;  // 槽位索引字符串，如 "0"、"1"
 *     },
 *     ...
 * }
 * </pre>
 * 仅记录非空槽位。</p>
 */
public final class CuriosCompat {

    /** Curios 模组 ID */
    public static final String MODID = "curios";

    /** 在救援 NBT 中存放 Curios 槽位信息的键名 */
    public static final String TAG_CURIOS_SLOTS = "CuriosSlots";

    private CuriosCompat() {
    }

    /** 检查 Curios 是否已加载到当前游戏实例中 */
    public static boolean isLoaded() {
        return ModList.get() != null && ModList.get().isLoaded(MODID);
    }

    /**
     * 记录玩家当前所有 Curios 槽位中的物品到 {@code parent}。
     *
     * <p>仅在 Curios 已安装时生效；否则为空操作。</p>
     *
     * @param player 死亡的玩家
     * @param parent 用于存放 Curios 槽位 NBT 的复合标签（即救援数据）
     */
    public static void recordCuriosSlots(Player player, CompoundTag parent) {
        if (!isLoaded()) {
            return;
        }
        Optional<ICuriosItemHandler> optHandler = CuriosApi.getCuriosInventory(player).resolve();
        if (optHandler.isEmpty()) {
            return;
        }
        ICuriosItemHandler handler = optHandler.get();
        CompoundTag curiosTag = new CompoundTag();
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            String identifier = entry.getKey();
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            CompoundTag slotTag = new CompoundTag();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    slotTag.put(String.valueOf(i), stack.save(new CompoundTag()));
                }
            }
            if (!slotTag.isEmpty()) {
                curiosTag.put(identifier, slotTag);
            }
        }
        if (!curiosTag.isEmpty()) {
            parent.put(TAG_CURIOS_SLOTS, curiosTag);
        }
    }

    /**
     * 尝试将物品归还到对应的 Curios 槽位。
     *
     * <p>逻辑：遍历 {@code curiosSlots} 中记录的所有槽位，找到第一个满足以下条件的槽位——
     * <ul>
     *   <li>记录的物品与 {@code stack} 同物同标签（{@link ItemStack#isSameItemSameTags}）</li>
     *   <li>该槽位当前在玩家身上存在</li>
     *   <li>该槽位当前为空</li>
     * </ul>
     * 找到则将物品放入该槽位并返回 {@code true}；否则返回 {@code false}（由调用者继续走默认归还流程）。</p>
     *
     * @param player       重生的玩家
     * @param stack        待归还的物品（不会被修改，内部使用 {@code copy()}）
     * @param curiosSlots  ① 中记录的 Curios 槽位 NBT（即 {@link #TAG_CURIOS_SLOTS} 对应的复合标签）
     * @return 是否成功归还到 Curios 槽位
     */
    public static boolean tryReturnToCurioSlot(Player player, ItemStack stack, CompoundTag curiosSlots) {
        if (!isLoaded() || stack.isEmpty() || curiosSlots.isEmpty()) {
            return false;
        }
        Optional<ICuriosItemHandler> optHandler = CuriosApi.getCuriosInventory(player).resolve();
        if (optHandler.isEmpty()) {
            return false;
        }
        ICuriosItemHandler handler = optHandler.get();
        for (String identifier : curiosSlots.getAllKeys()) {
            CompoundTag slotTag = curiosSlots.getCompound(identifier);
            Optional<ICurioStacksHandler> optStacks = handler.getStacksHandler(identifier);
            if (optStacks.isEmpty()) {
                continue;
            }
            IDynamicStackHandler stacks = optStacks.get().getStacks();
            for (String idxStr : slotTag.getAllKeys()) {
                int index;
                try {
                    index = Integer.parseInt(idxStr);
                } catch (NumberFormatException e) {
                    continue;
                }
                ItemStack recorded = ItemStack.of(slotTag.getCompound(idxStr));
                if (recorded.isEmpty() || !ItemStack.isSameItemSameTags(stack, recorded)) {
                    continue;
                }
                if (index < 0 || index >= stacks.getSlots()) {
                    continue;
                }
                if (stacks.getStackInSlot(index).isEmpty()) {
                    handler.setEquippedCurio(identifier, index, stack.copy());
                    return true;
                }
            }
        }
        return false;
    }
}
