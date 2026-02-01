package org.agmas.noellesroles.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.util.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GunShootPayload.Receiver.class)
public class GunCooldownInject {

    @Redirect(
            method = "receive",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"
            )
    )
    private void wathe$gun3s(ItemCooldownManager manager,
                             Item item,
                             int ticks,
                             @Local(argsOnly = true) ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();   // ← 开枪的人
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
        if (worldModifierComponent.isModifier(player, Noellesroles.NATURAL_GUN)) {
            manager.set(item, 0);
        }else if (worldModifierComponent.isModifier(player, Noellesroles.MARKSMAN)){
            manager.set(item,ticks/2);
        }else {
            manager.set(item,ticks);
        }

    }
}