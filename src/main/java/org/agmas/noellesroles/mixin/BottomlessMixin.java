package org.agmas.noellesroles.mixin;

import dev.doctor4t.wathe.block.FoodPlatterBlock;
import dev.doctor4t.wathe.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.index.WatheDataComponentTypes;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FoodPlatterBlock.class)
public class BottomlessMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected ActionResult onUse(BlockState state, @NotNull World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        if (!(world.getBlockEntity(pos) instanceof BeveragePlateBlockEntity blockEntity)) return ActionResult.PASS;

        if (player.isCreative()) {
            ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);
            if (!heldItem.isEmpty()) {
                blockEntity.addItem(heldItem);
                return ActionResult.SUCCESS;
            }
        }
        if (player.getStackInHand(Hand.MAIN_HAND).isOf(WatheItems.POISON_VIAL) && blockEntity.getPoisoner() == null) {
            blockEntity.setPoisoner(player.getUuidAsString());
            player.getStackInHand(Hand.MAIN_HAND).decrement(1);
            player.playSoundToPlayer(SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.5f, 1f);
            return ActionResult.SUCCESS;
        }
        if (player.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
            List<ItemStack> platter = blockEntity.getStoredItems();
            if (platter.isEmpty()) return ActionResult.SUCCESS;


            boolean hasPlatterItem = false;
            for (ItemStack platterItem : platter) {
                for (int i = 0; i < player.getInventory().size(); i++) {
                    ItemStack invItem = player.getInventory().getStack(i);
                    if (invItem.getItem() == platterItem.getItem()) {
                        hasPlatterItem = true;
                        break;
                    }
                }
                if (hasPlatterItem) break;
            }
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());

            if (!hasPlatterItem || worldModifierComponent.isModifier(player,Noellesroles.BOTTOMLESS)) {
                ItemStack randomItem = platter.get(world.random.nextInt(platter.size())).copy();
                randomItem.setCount(1);
                randomItem.set(DataComponentTypes.MAX_STACK_SIZE, 1);
                String poisoner = blockEntity.getPoisoner();
                if (poisoner != null) {
                    randomItem.set(WatheDataComponentTypes.POISONER, poisoner);
                    blockEntity.setPoisoner(null);
                }
                player.playSoundToPlayer(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
                player.setStackInHand(Hand.MAIN_HAND, randomItem);
            }
        }

        return ActionResult.PASS;
    }
}