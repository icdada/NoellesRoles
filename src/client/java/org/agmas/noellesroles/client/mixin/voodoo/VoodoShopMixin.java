package org.agmas.noellesroles.client.mixin.voodoo;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.item.SPLASH_POTION;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class VoodoShopMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow
    @Final
    public ClientPlayerEntity player;

    public VoodoShopMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    void bartenderShopRenderer(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player,Noellesroles.VOODOO)) {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(SPLASH_POTION.stack.copy(), 300, ShopEntry.Type.TOOL));
            int apart = 36;
            int x = width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (((LimitedInventoryScreen)(Object)this).height - 32) / 2;
            int y = shouldBeY - 46;

            for(int i = 0; i < entries.size(); ++i) {
                addDrawableChild(new LimitedInventoryScreen.StoreItemWidget((LimitedInventoryScreen) (Object)this, x + apart * i, y, (ShopEntry)entries.get(i), i));
            }
        }
    }

}
