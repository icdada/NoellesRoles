package org.agmas.noellesroles.item;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

import java.util.List;
import java.util.Optional;

/**
 * 30 秒喷溅夜视瓶（独立物品）
 */
public class SPLASH_POTION {
    public static ItemStack stack ;
static {
    stack = new ItemStack(Items.SPLASH_POTION);
    StatusEffectInstance nightVision = new StatusEffectInstance(
            StatusEffects.NIGHT_VISION,  // 直接拿常量
            1200,
            0,
            false, true, true
    );
    PotionContentsComponent contents = new PotionContentsComponent(
            Optional.empty(),      // 不绑定任何预设药水
            Optional.<Integer>empty(), // 自定义颜色，留空就用默认蓝色
            List.of(nightVision)   // 只放我们想要的 30 秒夜视
    );

    stack.set(DataComponentTypes.POTION_CONTENTS, contents);
    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("夜视药水瓶"));
}
}