package org.agmas.noellesroles.client.mixin;

import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerPoisonComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.coroner.BodyDeathReasonComponent;
import dev.doctor4t.wathe.client.gui.RoundTextRenderer;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.util.AnnounceWelcomePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(WatheClient.class)
public abstract class InstinctMixin {



    // 注入到isInstinctEnabled方法头部，覆盖原有逻辑
    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void unlockInstinctForCustomRoles(CallbackInfoReturnable<Boolean> cir) {

        // 1. 安全校验：避免空指针（客户端玩家/世界为空的情况）
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        // 2. 获取游戏世界组件
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent == null) return;
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());

        // 3. 判定自定义角色：JESTER / BETTER_VIGILANTE
        boolean isJester = gameWorldComponent.isRole(player, Noellesroles.JESTER);
        boolean isBetterVigilante = gameWorldComponent.isRole(player, Noellesroles.BETTER_VIGILANTE);
        boolean isEAGLE_EYE =  worldModifierComponent.isModifier(player, Noellesroles.EAGLE_EYE);

        // 4. 角色匹配 + 本能按键按下 → 强制启用本能
        if ((isJester || isBetterVigilante || !WatheClient.gameComponent.isRunning() || isEAGLE_EYE) && WatheClient.instinctKeybind.isPressed()) {
            cir.setReturnValue(true);  // 覆盖返回值为true
            cir.cancel();              // 取消原有逻辑执行
        }
    }


    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        if (target instanceof PlayerEntity) {
            if (!((PlayerEntity)target).isSpectator()) {
                BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get((PlayerEntity) target);
                PlayerPoisonComponent playerPoisonComponent =  PlayerPoisonComponent.KEY.get((PlayerEntity) target);
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.BARTENDER) && bartenderPlayerComponent.glowTicks > 0) {
                    cir.setReturnValue(Color.GREEN.getRGB());
                }
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.BARTENDER) && bartenderPlayerComponent.armor > 0) {
                    cir.setReturnValue(Color.BLUE.getRGB());
                    cir.cancel();
                }
                if ((gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.BARTENDER) || WatheClient.isKiller())&& playerPoisonComponent.poisonTicks > 0) {
                    if (bartenderPlayerComponent.armor > 0) {
                        cir.setReturnValue(Color.MAGENTA.getRGB());
                    } else {
                        cir.setReturnValue(Color.RED.getRGB());
                    }
                }
            }
        }
        if (target instanceof ItemEntity) {
            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.CONDUCTOR)){
                cir.setReturnValue(0xDB9D00);
                cir.cancel();
                return;
            }
            if (WatheClient.isKiller() && WatheClient.isPlayerAliveAndInSurvival()&& WatheClient.isInstinctEnabled()){
                cir.setReturnValue(0xDB9D00);
                cir.cancel();
                return;
            }
        }
        if (target instanceof PlayerBodyEntity body) {
            BodyDeathReasonComponent comp = BodyDeathReasonComponent.KEY.get(body);

            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.VULTURE)
                    && !comp.vultured) {
                cir.setReturnValue(Color.YELLOW.getRGB());
                cir.cancel();
                return;
            }

            // 验尸官：开本能 + 未被吃过 → 白色
            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.CORONER)
                    && !comp.vultured) {
                cir.setReturnValue(Color.GRAY.getRGB());
                cir.cancel();
                return;
            }
            if (WatheClient.isKiller() && WatheClient.isPlayerAliveAndInSurvival()&& WatheClient.isInstinctEnabled()){
                cir.setReturnValue(Color.GRAY.getRGB());
                cir.cancel();
                return;
            }
        }

        if (target instanceof PlayerEntity player) {
            if (gameWorldComponent.isRole(player, Noellesroles.BETTER_VIGILANTE)&& WatheClient.isInstinctEnabled() && WatheClient.isKiller()) {
                cir.setReturnValue(Color.BLUE.getRGB());
                cir.cancel();
                return;
            }
            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY.get((PlayerEntity) MinecraftClient.getInstance().player);
                if (executionerPlayerComponent.target.equals(target.getUuid())) {
                    cir.setReturnValue(Color.YELLOW.getRGB());
                    cir.cancel();
                    return;
                }
            }
            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.JESTER) && WatheClient.isInstinctEnabled()) {
                    cir.setReturnValue(Color.PINK.getRGB());
                    cir.cancel();
                    return;
            }

            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.BETTER_VIGILANTE) && WatheClient.isInstinctEnabled()) {
                cir.setReturnValue(Color.GREEN.getRGB());
                cir.cancel();
                return;
            }
            if (!((PlayerEntity)target).isSpectator() && WatheClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((PlayerEntity) target, Noellesroles.MIMIC) && WatheClient.isKiller()  && WatheClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(MathHelper.hsvToRgb(0.0F, 1.0F, 0.6F));
                    cir.cancel();
                    return;
                }
            }
            if (!((PlayerEntity)target).isSpectator() && WatheClient.isInstinctEnabled()) {
                Role role = gameWorldComponent.getRole((PlayerEntity) target);
                if (role != null) {
                    if (WatheClient.isKiller() && WatheClient.isPlayerAliveAndInSurvival()) {
                        if (Noellesroles.KILLER_SIDED_NEUTRALS.contains(role)) {
                            cir.setReturnValue(role.color());
                            cir.cancel();
                            return;
                        } else if (!role.isInnocent() && !role.canUseKiller()) {
                           cir.setReturnValue(5168437);
                           cir.cancel();
                           return;
                        }
                    }
                }
            }
            if (!((PlayerEntity)target).isSpectator() && WatheClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, Noellesroles.EXECUTIONER) && WatheClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(Noellesroles.EXECUTIONER.color());
                    cir.cancel();
                }
            }
            if ((!WatheClient.gameComponent.isRunning() || worldModifierComponent.isModifier(MinecraftClient.getInstance().player, Noellesroles.EAGLE_EYE) )&& WatheClient.instinctKeybind.isPressed()){
                cir.setReturnValue(Color.GREEN.getRGB());
                cir.cancel();
            }
        }
    }
}
