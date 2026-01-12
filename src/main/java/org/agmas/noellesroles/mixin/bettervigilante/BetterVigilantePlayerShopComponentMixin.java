package org.agmas.noellesroles.mixin.bettervigilante;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import dev.doctor4t.wathe.game.GameConstants;
import dev.doctor4t.wathe.index.WatheItems;
import dev.doctor4t.wathe.index.WatheSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class BetterVigilantePlayerShopComponentMixin {
    @Shadow public int balance;
    @Shadow @Final private PlayerEntity player;
    @Shadow public abstract void sync();

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    void betterVigilanteShop(int index, CallbackInfo ci) {
        GameWorldComponent gwc = GameWorldComponent.KEY.get(player.getWorld());
        if (!gwc.isRole(player, Noellesroles.BETTER_VIGILANTE)) return;

        // 槽位 0：300 金币买手榴弹
        if (index == 0) {
            if (balance >= 300) {
                balance -= 300;
                sync();
                player.giveItemStack(WatheItems.GRENADE.getDefaultStack());
                playSoundToPlayer(WatheSounds.UI_SHOP_BUY);
            } else {
                sendFail();
            }
            ci.cancel();
            return;
        }

        // 槽位 1：800 金币买狂暴药剂
        if (index == 1) {
            if (balance >= 800) {
                balance -= 800;
                sync();
                PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(player);
                    component.startPsycho();
                    component.psychoTicks = GameConstants.getInTicks(0, 45);
                    component.armour = 0;
                playSoundToPlayer(WatheSounds.UI_SHOP_BUY);
            } else {
                sendFail();
            }
            ci.cancel();
        }
    }

    /* ---------------- 工具方法 ---------------- */
    private void playSoundToPlayer(net.minecraft.sound.SoundEvent sound) {
        if (player instanceof ServerPlayerEntity sp) {
            sp.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(
                            Registries.SOUND_EVENT.getEntry(sound),
                            SoundCategory.PLAYERS,
                            sp.getX(), sp.getY(), sp.getZ(),
                            1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F,
                            player.getRandom().nextLong()
                    )
            );
        }
    }

    private void sendFail() {
        player.sendMessage(Text.literal("Purchase Failed").formatted(Formatting.DARK_RED), true);
        playSoundToPlayer(WatheSounds.UI_SHOP_BUY_FAIL);
    }
}