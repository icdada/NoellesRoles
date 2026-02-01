package org.agmas.noellesroles.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.harpymodloader.commands.argument.ModifierArgumentType; // 导入官方的参数类型
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.events.ModifierAssigned;
import org.agmas.harpymodloader.events.ModifierRemoved;
import org.agmas.harpymodloader.modifiers.Modifier;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SetModCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("setmod")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("player", EntityArgumentType.player())
                        .then(literal("set")
                                .then(argument("mod", ModifierArgumentType.create()) // 核心：使用官方解析器
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

                                            // 核心：直接获取对象，无需手动查找
                                            Modifier targetMod = ModifierArgumentType.getModifier(context, "mod");
                                            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
                                            worldModifierComponent.addModifier(player.getUuid(), targetMod);
                                            // 执行设置
                                            ModifierAssigned.EVENT.invoker().assignModifier(player, targetMod);

                                            // 获取显示名（如果 Modifier 类有 getName() 方法更好，这里用 ID 作为后备）
                                            String displayName = targetMod.identifier().getPath();
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("✅ 已为 " + player.getName().getString() + " 设置修饰符: " + displayName)
                                                            .formatted(Formatting.GREEN),
                                                    true
                                            );
                                            player.sendMessage(Text.literal("你获得了修饰符: " + displayName).formatted(Formatting.GOLD));
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("remove")
                                .then(argument("mod", ModifierArgumentType.create()) // 核心：使用官方解析器
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

                                            // 核心：直接获取对象
                                            Modifier targetMod = ModifierArgumentType.getModifier(context, "mod");
                                            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.getWorld());
                                            worldModifierComponent.getModifiers(player).remove(targetMod);
                                            // 执行删除
                                            ModifierRemoved.EVENT.invoker().removeModifier(player, targetMod);

                                            String displayName = targetMod.identifier().getPath();
                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("❌ 已为 " + player.getName().getString() + " 移除修饰符: " + displayName)
                                                            .formatted(Formatting.RED),
                                                    true
                                            );
                                            player.sendMessage(Text.literal("你的修饰符 '" + displayName + "' 已被移除").formatted(Formatting.GRAY));
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}