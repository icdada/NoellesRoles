package org.agmas.noellesroles.mixin.golden;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import dev.doctor4t.wathe.util.ShopEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerShopComponent.class,priority = 999)
public abstract class GoldenPlayerShopComponentMixin {
    @Shadow
    public int balance;

    @Shadow @Final
    private PlayerEntity player;
    @Unique
    private int back_balance = -1;

    @Shadow public abstract void sync();

    @Inject(method = "tryBuy", at = @At("HEAD"))
    void goldenTryBuy(int index, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
        if (worldModifierComponent.isModifier(player,Noellesroles.GOLDEN)) {
            if (balance != 99999) {
                this.back_balance = balance;
            }
            this.balance = 99999;
        }
    }
    @Inject(method = "sync", at = @At("HEAD"))
    void sync(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
        if (worldModifierComponent.isModifier(player,Noellesroles.GOLDEN)) {
            if (back_balance != -1) {
                this.balance = back_balance;
                back_balance = -1;
            }
        }
    }

}
