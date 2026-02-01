package org.agmas.noellesroles.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;


@Mixin(Item.class)
public class MixinItem {

    @Inject(method="getMaxUseTime", at=@At(value="HEAD"), cancellable = true)
    private void getMaxUseTime(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof ServerPlayerEntity player) {
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
            if (worldModifierComponent.isModifier(player,Noellesroles.GLUTTON)&& Objects.requireNonNull(stack.get(DataComponentTypes.FOOD)).getEatTicks() >= 1) cir.setReturnValue(1);
        }
    }
}