package org.agmas.noellesroles.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.wathe.client.WatheClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets = "dev.doctor4t.wathe.mixin.client.restrictions.KeyBindingMixin")
public abstract class KeyBindingMixin {
    /*@Inject(method = "shouldSuppressKey", at = @At("HEAD"), cancellable = true)
    private void agmas$injectShouldSuppressKey(CallbackInfoReturnable<Boolean> cir , @Local(argsOnly = true) KeyBinding self) {
        if (!WatheClient.gameComponent.isRunning()) {
            var opt = MinecraftClient.getInstance().options;
            boolean suppress = false;
            boolean isPermanentBlockKey = self.equals(opt.advancementsKey);

            // 条件屏蔽的按键 - 游戏运行时禁用、游戏未运行时开放
            // 最终屏蔽判断

            cir.setReturnValue(!suppress);
            cir.cancel();
        }
    }*/
}