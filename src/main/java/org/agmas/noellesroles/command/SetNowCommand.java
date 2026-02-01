package org.agmas.noellesroles.command;

import com.mojang.brigadier.CommandDispatcher;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.harpymodloader.commands.argument.RoleArgumentType; // 导入自定义的职业参数类型
import dev.doctor4t.wathe.api.Role;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.harpymodloader.events.ModdedRoleRemoved;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SetNowCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("setnow")
                .requires(src -> src.hasPermissionLevel(2))
                .then(argument("player", EntityArgumentType.player())
                        .then(literal("set")
                                .then(argument("role", RoleArgumentType.create()) // 核心：使用自定义参数解析器
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

                                            // 核心：直接获取 Role 对象，无需手动查找
                                            Role targetRole = RoleArgumentType.getRole(context, "role");

                                            // 执行设置
                                            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
                                            gameWorldComponent.addRole(player, targetRole);

                                            ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, targetRole);

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("已为 " + player.getName().getString() + " 设置职业: " + targetRole.identifier().getPath())
                                                            .formatted(Formatting.GREEN),
                                                    true
                                            );
                                            player.sendMessage(Text.literal("你的职业已被设置为: " + targetRole.identifier().getPath()).formatted(Formatting.GOLD));
                                            return 1;
                                        })
                                )
                        )
                        .then(literal("remove")
                                .then(argument("role", RoleArgumentType.create()) // 核心：使用自定义参数解析器
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

                                            // 核心：直接获取 Role 对象
                                            Role targetRole = RoleArgumentType.getRole(context, "role");
                                            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
                                            gameWorldComponent.addRole(player, WatheRoles.CIVILIAN);
;
                                            ModdedRoleRemoved.EVENT.invoker().removeModdedRole(player, targetRole);

                                            context.getSource().sendFeedback(
                                                    () -> Text.literal("已为 " + player.getName().getString() + " 删除职业: " + targetRole.identifier().getPath())
                                                            .formatted(Formatting.RED),
                                                    true
                                            );
                                            player.sendMessage(Text.literal("你的职业 '" + targetRole.identifier().getPath() + "' 已被移除").formatted(Formatting.GRAY));
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}