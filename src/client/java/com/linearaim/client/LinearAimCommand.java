package com.linearaim.client;

import com.linearaim.client.config.AimMath;
import com.linearaim.client.config.LinearAimConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * 主机控制台风格的客户端命令，例如：
 * <pre>
 *   /linearaim                      查看全部参数
 *   /linearaim sensitivity 6.25
 *   /linearaim y_scale 1.0
 *   /linearaim m_yaw 0.022
 *   /linearaim m_pitch 0.022
 *   /linearaim invertY true
 *   /linearaim enabled false
 *   /linearaim reset
 * </pre>
 */
public final class LinearAimCommand {
	private LinearAimCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("linearaim")
						.executes(context -> {
							printAll(context.getSource());
							return 1;
						})
						.then(ClientCommandManager.literal("sensitivity")
								.then(ClientCommandManager.argument("value",
												DoubleArgumentType.doubleArg(LinearAimConfig.MIN_SENSITIVITY,
														LinearAimConfig.MAX_SENSITIVITY))
										.executes(context -> {
											LinearAimConfig config = LinearAimConfig.get();
											double value = DoubleArgumentType.getDouble(context, "value");
											config.sensitivity = AimMath.clamp(
													AimMath.snap(value, LinearAimConfig.SENSITIVITY_STEP),
													LinearAimConfig.MIN_SENSITIVITY, LinearAimConfig.MAX_SENSITIVITY);
											config.save();
											reply(context.getSource(), "sensitivity", config.getSensitivity(), 3);
											return 1;
										})))
						.then(ClientCommandManager.literal("y_scale")
								.then(ClientCommandManager.argument("value",
												DoubleArgumentType.doubleArg(LinearAimConfig.MIN_Y_SCALE,
														LinearAimConfig.MAX_Y_SCALE))
										.executes(context -> {
											LinearAimConfig config = LinearAimConfig.get();
											double value = DoubleArgumentType.getDouble(context, "value");
											config.sensitivityYScale = AimMath.clamp(
													AimMath.snap(value, LinearAimConfig.Y_SCALE_STEP),
													LinearAimConfig.MIN_Y_SCALE, LinearAimConfig.MAX_Y_SCALE);
											config.save();
											reply(context.getSource(), "sensitivity_y_scale",
													config.getSensitivityYScale(), 2);
											return 1;
										})))
						.then(ClientCommandManager.literal("m_yaw")
								.then(ClientCommandManager.argument("value",
												DoubleArgumentType.doubleArg(LinearAimConfig.MIN_M,
														LinearAimConfig.MAX_M))
										.executes(context -> {
											LinearAimConfig config = LinearAimConfig.get();
											double value = DoubleArgumentType.getDouble(context, "value");
											config.mYaw = AimMath.clamp(
													AimMath.snap(value, LinearAimConfig.M_STEP),
													LinearAimConfig.MIN_M, LinearAimConfig.MAX_M);
											config.save();
											reply(context.getSource(), "m_yaw", config.getMYaw(), 3);
											return 1;
										})))
						.then(ClientCommandManager.literal("m_pitch")
								.then(ClientCommandManager.argument("value",
												DoubleArgumentType.doubleArg(LinearAimConfig.MIN_M,
														LinearAimConfig.MAX_M))
										.executes(context -> {
											LinearAimConfig config = LinearAimConfig.get();
											double value = DoubleArgumentType.getDouble(context, "value");
											config.mPitch = AimMath.clamp(
													AimMath.snap(value, LinearAimConfig.M_STEP),
													LinearAimConfig.MIN_M, LinearAimConfig.MAX_M);
											config.save();
											reply(context.getSource(), "m_pitch", config.getMPitch(), 3);
											return 1;
										})))
						.then(ClientCommandManager.literal("invertY")
								.then(ClientCommandManager.argument("value", BoolArgumentType.bool())
										.executes(context -> {
											LinearAimConfig config = LinearAimConfig.get();
											config.invertY = BoolArgumentType.getBool(context, "value");
											config.save();
											context.getSource().sendFeedback(Component.literal(
													"[LinearAim] invertY " + (config.invertY ? "on" : "off")));
											return 1;
										})))
						.then(ClientCommandManager.literal("enabled")
								.then(ClientCommandManager.argument("value", BoolArgumentType.bool())
										.executes(context -> {
											LinearAimConfig config = LinearAimConfig.get();
											config.enabled = BoolArgumentType.getBool(context, "value");
											config.save();
											context.getSource().sendFeedback(Component.literal(
													"[LinearAim] " + (config.enabled
															? "linear sensitivity enabled"
															: "vanilla sensitivity restored")));
											return 1;
										})))
						.then(ClientCommandManager.literal("reset")
								.executes(context -> {
									LinearAimConfig config = LinearAimConfig.get();
									config.resetToDefaults();
									config.save();
									context.getSource().sendFeedback(
											Component.literal("[LinearAim] reset to defaults"));
									printAll(context.getSource());
									return 1;
								}))
				));
	}

	private static void reply(FabricClientCommandSource source, String name, double value, int decimals) {
		source.sendFeedback(Component.literal("[LinearAim] " + name + " = "
				+ String.format(Locale.ROOT, "%." + decimals + "f", value)));
	}

	private static void printAll(FabricClientCommandSource source) {
		LinearAimConfig config = LinearAimConfig.get();
		source.sendFeedback(Component.literal("[LinearAim]"));
		reply(source, "sensitivity", config.getSensitivity(), 3);
		reply(source, "sensitivity_y_scale", config.getSensitivityYScale(), 2);
		reply(source, "m_yaw", config.getMYaw(), 3);
		reply(source, "m_pitch", config.getMPitch(), 3);
		source.sendFeedback(Component.literal("[LinearAim] invertY = " + config.invertY));
		source.sendFeedback(Component.literal("[LinearAim] enabled = " + config.enabled));
	}
}
