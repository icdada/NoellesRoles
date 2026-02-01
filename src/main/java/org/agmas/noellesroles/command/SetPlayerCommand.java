package org.agmas.noellesroles.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.doctor4t.wathe.cca.PlayerPsychoComponent;
import dev.doctor4t.wathe.game.GameConstants;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.bartender.BartenderPlayerComponent;
import pro.fazeclan.river.stupid_express.cca.AbilityCooldownComponent;

import java.lang.reflect.Method;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SetPlayerCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("setplayer")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("player", EntityArgumentType.player())
                                .then(literal("armor")
                                        .then(argument("value", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

                                                    // 获取酒保组件并设置护甲
                                                    BartenderPlayerComponent bartenderComponent = BartenderPlayerComponent.KEY.get(player);
                                                    int armorValue = IntegerArgumentType.getInteger(context, "value");
                                                    bartenderComponent.setArmor(armorValue);

                                                    // 反馈信息
                                                    context.getSource().sendFeedback(
                                                            () -> Text.literal("✅ 已为 " + player.getName().getString() + " 设置护甲: " + armorValue)
                                                                    .formatted(Formatting.GREEN),
                                                            true
                                                    );
                                                    player.sendMessage(Text.literal("你的护甲被管理员设置为 " + armorValue).formatted(Formatting.GOLD), false);
                                                    return 1;
                                                })
                                        )
                                )
                                // --- PSYCHO 子命令 (接受两个 Int 参数) ---
                                .then(literal("psycho")
                                        .then(argument("ticks_upper_limit", IntegerArgumentType.integer(0))
                                                .then(argument("armour_value", IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            ServerPlayerEntity victim = EntityArgumentType.getPlayer(context, "player");
                                                            PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(victim);

                                                            // 获取参数
                                                            int ticksUpperLimit = IntegerArgumentType.getInteger(context, "ticks_upper_limit");
                                                            int armourValue = IntegerArgumentType.getInteger(context, "armour_value");

                                                            // 核心逻辑：如果不在精神错乱状态
                                                            if (component.getPsychoTicks() <= 0) {
                                                                component.startPsycho();

                                                                // 1. 设置 Ticks: 使用参数作为上限
                                                                component.psychoTicks = GameConstants.getInTicks(0, ticksUpperLimit);

                                                                // 2. 设置 Armour: 使用第二个参数
                                                                component.armour = armourValue;

                                                                context.getSource().sendFeedback(
                                                                        () -> Text.literal("⚠️ 已触发 " + victim.getName().getString() + " 的精神错乱: 持续～" + ticksUpperLimit + "秒, 护甲=" + armourValue)
                                                                                .formatted(Formatting.RED),
                                                                        true
                                                                );
                                                                victim.sendMessage(Text.literal("你进入了疯魔状态！").formatted(Formatting.DARK_RED), false);
                                                            } else {
                                                                context.getSource().sendError(Text.literal(victim.getName().getString() + " 已处于疯魔中。"));
                                                            }
                                                            return 1;
                                                        })
                                                )
                                        )
                                )


                                        // --- 通用 COOLDOWN 命令 ---
                                .then(literal("cooldown")
                                                .then(argument("ticks", IntegerArgumentType.integer(0))
                                                        .executes(context -> {
                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                            int cooldownTicks = IntegerArgumentType.getInteger(context, "ticks");

                                                            return 1;
                                                        })
                                                )
                                )
                        // --- 未来扩展示例 (Boolean & String) ---
                    /*
                    .then(literal("toggle")
                        .then(argument("feature", StringArgumentType.word())
                            .then(argument("enable", BoolArgumentType.bool())
                                .executes(context -> {
                                    ServerPlayerEntity p = EntityArgumentType.getPlayer(context, "player");
                                    String feature = StringArgumentType.getString(context, "feature");
                                    boolean enable = BoolArgumentType.getBool(context, "enable");

                                    // 这里可以写入特定的 NBT 标签，如 "Feature_Flight": 1b

                                    return 1;
                                })
                            )
                        )
                    )
                    */
                )
        );
    }
}