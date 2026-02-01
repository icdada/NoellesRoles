package org.agmas.noellesroles.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.doctor4t.wathe.Wathe;
import dev.doctor4t.wathe.cca.StaminaComponent;

import dev.doctor4t.wathe.game.GameFunctions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SetRoleCommand {

    // 缓存配置实例，方便操作
    private static final NoellesRolesConfig CONFIG = NoellesRolesConfig.HANDLER.instance();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // 指令结构: /config set <field> <value>
        dispatcher.register(literal("setconfig")
                .requires(src -> src.hasPermissionLevel(2)) // 需要管理员权限
                .then(literal("set")
                        .executes(context -> listFields(context)) // 如果没有参数，列出所有字段
                        .then(argument("field", StringArgumentType.word())
                                .executes(context -> getValue(context)) // 显示当前值
                                .then(argument("value", StringArgumentType.greedyString())
                                        .executes(context -> setValue(context))
                                )
                        )
                )
        );
        dispatcher.register(literal("killme").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();

            // 1. 检查玩家是否存在（确保是在游戏中执行，而不是控制台）
            if (player == null) {
                context.getSource().sendError(Text.literal("此指令只能由玩家执行"));
                return 0;
            }
            GameFunctions.killPlayer(player, true, null, Noellesroles.VOODOO_MAGIC_DEATH_REASON);
            return 1;
        }));
        dispatcher.register(literal("setRecover").requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                        .executes(SetRoleCommand::setStamina)
                )
        );

        dispatcher.register(literal("setMaxSprintingTicks").requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                        .executes(SetRoleCommand::setMaxStamina)
                )
        );
        dispatcher.register(literal("setMinSprintingTicks").requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                        .executes(SetRoleCommand::setMinStamina)
                )
        );
    }
    private static int setStamina(CommandContext<ServerCommandSource> context) {
        try {
            // 1. 获取命令执行者 (玩家)
            ServerPlayerEntity player = context.getSource().getPlayer();

            // 2. 获取该玩家身上的 StaminaComponent
            // 注意：这里假设你的组件是挂在 Player 上的，如果是 World 上的请用 WorldComponent.KEY.get(player.getWorld())
            StaminaComponent staminaComponent = StaminaComponent.KEY.get(player.getWorld());

            // 3. 获取指令中输入的数值
            float value = FloatArgumentType.getFloat(context, "value");

            // 4. 修改组件数据 (这里会自动触发 sync 和持久化)
            staminaComponent.setStamina(value);

            // 5. 给执行者反馈
            context.getSource().sendFeedback(
                    () -> Text.literal(  "已将体力上限设置为 " + value).formatted(Formatting.GREEN),
                    true
            );

            return 1; // 返回成功状态
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("执行失败: " + e.getMessage()));
            return 0;
        }
    }
    private static int setMaxStamina(CommandContext<ServerCommandSource> context) {
        try {
            // 1. 获取命令执行者 (玩家)
            ServerPlayerEntity player = context.getSource().getPlayer();

            // 2. 获取该玩家身上的 StaminaComponent
            // 注意：这里假设你的组件是挂在 Player 上的，如果是 World 上的请用 WorldComponent.KEY.get(player.getWorld())
            StaminaComponent staminaComponent = StaminaComponent.KEY.get(player.getWorld());

            // 3. 获取指令中输入的数值
            float value = FloatArgumentType.getFloat(context, "value");

            // 4. 修改组件数据 (这里会自动触发 sync 和持久化)
            staminaComponent.setRecoverSpeed(value);

            // 5. 给执行者反馈
            context.getSource().sendFeedback(
                    () -> Text.literal(  "已将体力上线设置为 " + value).formatted(Formatting.GREEN),
                    true
            );

            return 1; // 返回成功状态
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("执行失败: " + e.getMessage()));
            return 0;
        }
    }
    private static int setMinStamina(CommandContext<ServerCommandSource> context) {
        try {
            // 1. 获取命令执行者 (玩家)
            ServerPlayerEntity player = context.getSource().getPlayer();

            // 2. 获取该玩家身上的 StaminaComponent
            // 注意：这里假设你的组件是挂在 Player 上的，如果是 World 上的请用 WorldComponent.KEY.get(player.getWorld())
            StaminaComponent staminaComponent = StaminaComponent.KEY.get(player.getWorld());

            // 3. 获取指令中输入的数值
            float value = FloatArgumentType.getFloat(context, "value");

            // 4. 修改组件数据 (这里会自动触发 sync 和持久化)
            staminaComponent.putMinStamina(value);

            // 5. 给执行者反馈
            context.getSource().sendFeedback(
                    () -> Text.literal(  "已将体力消耗设置为 " + value).formatted(Formatting.GREEN),
                    true
            );

            return 1; // 返回成功状态
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("执行失败: " + e.getMessage()));
            return 0;
        }
    }
    // 列出所有可以修改的字段
    private static int listFields(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> Text.literal("可用配置项:").formatted(Formatting.YELLOW),
                false
        );

        // 这里简单列举，实际可以通过反射获取所有 public 字段，但为了稳定建议手动列几个常用的
        context.getSource().sendFeedback(
                () -> Text.literal("- insanePlayersSeeMorphs (boolean)"),
                false
        );
        context.getSource().sendFeedback(
                () -> Text.literal("- bartenderMaxArmorSet (int)"),
                false
        );
        context.getSource().sendFeedback(
                () -> Text.literal("- generalCooldownTicks (int)"),
                false
        );
        // ... 其他字段
        return 1;
    }

    // 获取特定字段的当前值
    private static int getValue(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String fieldName = StringArgumentType.getString(context, "field");
        Field field = getField(fieldName);

        if (field == null) {
            context.getSource().sendFeedback(
                    () -> Text.literal("错误: 找不到配置项 '" + fieldName + "'").formatted(Formatting.RED),
                    true
            );
            return 0;
        }

        try {
            Object value = field.get(CONFIG);
            context.getSource().sendFeedback(
                    () -> Text.literal(fieldName + " = " + value + " (" + field.getType().getSimpleName() + ")").formatted(Formatting.AQUA),
                    true
            );
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 1;
    }

    // 设置特定字段的值并保存
    private static int setValue(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String fieldName = StringArgumentType.getString(context, "field");
        String rawValue = StringArgumentType.getString(context, "value");

        Field field = getField(fieldName);
        if (field == null) {
            context.getSource().sendFeedback(
                    () -> Text.literal("错误: 找不到配置项 '" + fieldName + "'").formatted(Formatting.RED),
                    true
            );
            return 0;
        }

        try {
            Object convertedValue = convertString(field.getType(), rawValue);
            field.set(CONFIG, convertedValue);

            // 核心：保存到文件
            // 注意：save() 是异步的
            CompletableFuture.runAsync(() -> {
                try {
                    NoellesRolesConfig.HANDLER.save();
                    context.getSource().sendFeedback(
                            () -> Text.literal("已保存配置: " + fieldName + " = " + convertedValue).formatted(Formatting.GREEN),
                            true
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    context.getSource().sendFeedback(
                            () -> Text.literal("保存失败: " + e.getMessage()).formatted(Formatting.RED),
                            true
                    );
                }
            });

        } catch (Exception e) {
            context.getSource().sendFeedback(
                    () -> Text.literal("类型错误: 无法将 '" + rawValue + "' 转换为 " + field.getType().getSimpleName()).formatted(Formatting.RED),
                    true
            );
        }
        return 1;
    }

    // 辅助方法：根据字符串获取配置类中的字段
    private static Field getField(String name) {
        try {
            Field field = NoellesRolesConfig.class.getField(name);
            // 确保是 public 的
            if (java.lang.reflect.Modifier.isPublic(field.getModifiers())) {
                return field;
            }
        } catch (NoSuchFieldException e) {
            // 字段不存在
        }
        return null;
    }

    // 辅助方法：将字符串转换为字段对应的类型
    private static Object convertString(Class<?> type, String value) throws Exception {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else {
            throw new IllegalArgumentException("不支持的数据类型: " + type.getSimpleName());
        }
    }
}