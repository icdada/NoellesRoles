package org.agmas.noellesroles.client.mixin.the_insane_damned_paranoid_killer;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.client.ui.SwapperPlayerWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;


@Mixin(LimitedInventoryScreen.class)
public abstract class ScreenMixin extends LimitedHandledScreen<PlayerScreenHandler>{
    @Shadow @Final public ClientPlayerEntity player;

    public ScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @Inject(method = "render", at = @At("HEAD"))
    void renderSwapperText(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player,Noellesroles.SWAPPER)) {
            int y = (height- 32) / 2;
            int x = width / 2;
            if (SwapperPlayerWidget.playerChoiceOne == null) {
                Text name = Text.translatable("hud.swapper.first_player_selection");
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, name, x - (MinecraftClient.getInstance().textRenderer.getWidth(name)/2), y + 40, Color.CYAN.getRGB());
            }
        }
    }
    @Inject(method = "init", at = @At("HEAD"))
    void renderSwapperHeads(CallbackInfo ci) {
        SwapperPlayerWidget.playerChoiceOne = null;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        if (gameWorldComponent.isRole(player,Noellesroles.SWAPPER)) {
            ClientWorld clientWorld = MinecraftClient.getInstance().world;
            if (clientWorld == null) return;
            List<AbstractClientPlayerEntity> entries = clientWorld.getPlayers();
            if (!entries.contains(player)) entries.add(player);
            int apart = 36;
            int x = width / 2 - (entries.size()) * apart / 2 + 9;
            int shouldBeY = (height - 32) / 2;
            int y = shouldBeY + 80;

            for(int i = 0; i < entries.size(); ++i) {
                SwapperPlayerWidget child = new SwapperPlayerWidget(((LimitedInventoryScreen)(Object)this), x + apart * i, y, entries.get(i), i);
                addDrawableChild(child);
            }
        }
    }

}
