package org.agmas.noellesroles.client.mixin.bettervigilante;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class BetterVigilanteShopMixin extends LimitedHandledScreen<PlayerScreenHandler> {

    @Shadow @Final
    public ClientPlayerEntity player;

    public BetterVigilanteShopMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    void betterVigilanteShopRenderer(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player, Noellesroles.BETTER_VIGILANTE)) {
            List<ShopEntry> entries = new ArrayList<>();
            // 0 手榴弹 300
            entries.add(new ShopEntry(WatheItems.GRENADE.getDefaultStack(), 300, ShopEntry.Type.TOOL));
            // 1 狂暴药剂 800
            entries.add(new ShopEntry(WatheItems.PSYCHO_MODE.getDefaultStack(), 800, ShopEntry.Type.TOOL));

            int apart = 36;
            int x = width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (((LimitedInventoryScreen) (Object) this).height - 32) / 2;
            int y = shouldBeY - 46;

            for (int i = 0; i < entries.size(); i++) {
                addDrawableChild(new LimitedInventoryScreen.StoreItemWidget(
                        (LimitedInventoryScreen) (Object) this,
                        x + apart * i, y,
                        entries.get(i), i));
            }
        }
    }
}