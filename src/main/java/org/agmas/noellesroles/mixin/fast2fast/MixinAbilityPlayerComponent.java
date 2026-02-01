package org.agmas.noellesroles.mixin.fast2fast;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbilityPlayerComponent.class)
public class MixinAbilityPlayerComponent {

    @Shadow
    public int cooldown;
    @Shadow @Final
    private PlayerEntity player;
    @Inject(method = "sync", at = @At("HEAD"))
    void sync(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
        if (worldModifierComponent.isModifier(player, Noellesroles.FAST2FAST)) {
            cooldown = 0;
        }
    }
}

