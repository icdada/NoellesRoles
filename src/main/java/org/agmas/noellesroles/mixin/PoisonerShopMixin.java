package org.agmas.noellesroles.mixin;

import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import dpm.harpysimpleroles.index.HSRConstants;
import dpm.harpysimpleroles.index.HSRItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import dpm.harpysimpleroles.item.ToxinItem;
@Mixin(
        value = dpm.harpysimpleroles.HarpySimpleRoles.class,
        remap = false
)
public abstract class PoisonerShopMixin {

    // ✅ 核心注入配置：注入到目标模组的onInitialize方法头部，TAIL=方法执行完毕前最后一刻
    // 此时HSRConstants.POISONER_SHOP_ENTRIES已经初始化完成，是最佳修改时机
    @Inject(
            method = "onInitialize",
            at = @At("TAIL"),
            remap = false,
            require = 0,
            allow = 0
    )
    private void rewritePoisonerShopList(CallbackInfo ci) {
        // 获取目标列表，这行代码和你写的完全一致
        List<ShopEntry> shopEntries = HSRConstants.POISONER_SHOP_ENTRIES;

        // 1. 清空原有所有商店商品【核心第一步】
        shopEntries.clear();

        // 2. 全量添加你自定义的商品列表【核心第二步】，完全按你的需求编写
        shopEntries.add(new ShopEntry(WatheItems.KNIFE.getDefaultStack(), 150, ShopEntry.Type.WEAPON));
        shopEntries.add(new ShopEntry(HSRItems.TOXIN.getDefaultStack(), 50, ShopEntry.Type.POISON));
        shopEntries.add(new ShopEntry(WatheItems.POISON_VIAL.getDefaultStack(), 75, ShopEntry.Type.POISON));
        shopEntries.add(new ShopEntry(WatheItems.SCORPION.getDefaultStack(), 50, ShopEntry.Type.POISON));
        shopEntries.add(new ShopEntry(WatheItems.FIRECRACKER.getDefaultStack(), 10, ShopEntry.Type.TOOL));
        shopEntries.add(new ShopEntry(WatheItems.LOCKPICK.getDefaultStack(), 100, ShopEntry.Type.TOOL));
        // 带onBuy回调的Blackout商品，写法完全匹配原代码
        shopEntries.add(new ShopEntry(WatheItems.BLACKOUT.getDefaultStack(), 150, ShopEntry.Type.TOOL) {
            @Override
            public boolean onBuy(@NotNull PlayerEntity player) {
                return PlayerShopComponent.useBlackout(player);
            }
        });
        shopEntries.add(new ShopEntry(new ItemStack(WatheItems.NOTE, 4), 10, ShopEntry.Type.TOOL));
    }
}