package com.linearaim.client.mixin;

import com.linearaim.client.config.AimMath;
import com.linearaim.client.config.LinearAimConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * 用线性灵敏度完全接管原版的鼠标转向速度。
 *
 * <p>1.21.11 的 {@code MouseHandler#turnPlayer(double)} 逻辑为：
 * <pre>
 *   e = options.sensitivity().get() * 0.6F + 0.2F   // 原版滑块 -> 曲线参数
 *   f = e * e * e
 *   g = f * 8.0                                     // 正常视角乘数
 *   ...
 *   j = accumulatedDX * g; k = accumulatedDY * g;   // 开镜时改用 f，即自动 1/8 降敏
 *   player.turn(...)
 * </pre>
 *
 * <p>我们不去重写整段逻辑，而是把 {@code sensitivity().get()} 的返回值替换成
 * "能让原版公式算出目标线性乘数"的等效滑块值（见 {@link AimMath#toVanillaSensitivity}）。
 * 这样原版的开镜降敏、平滑视角、反转轴等行为全部保留，同时最终转速严格线性。
 * 由于原版滑块值不再被读取，原版灵敏度设置从此完全失效。
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	/**
	 * 替换 turnPlayer 中第一次调用 {@code OptionInstance#get()}（即读取灵敏度）的结果。
	 */
	@Redirect(
			method = "turnPlayer(D)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
					ordinal = 0
			)
	)
	private Object linearaim$replaceSensitivity(OptionInstance<?> option) {
		LinearAimConfig config = LinearAimConfig.get();
		if (!config.enabled) {
			return option.get();
		}
		// 水平方向：每计数转角 = sensitivity * m_yaw 度，换算成 Minecraft 的转速乘数
		double multiplier = AimMath.csToMultiplier(config.getSensitivity(), config.getMYaw());
		return AimMath.toVanillaSensitivity(multiplier);
	}

	/**
	 * 垂直方向的收尾：{@code m_pitch / m_yaw} 的差值与 {@code sensitivity_y_scale}，
	 * 外加反转 Y 轴。水平方向已经在上面的基础乘数里处理完了。
	 *
	 * <p>原版此时已经按 {@code invertMouseY} 取过一次反，这里先还原成原始方向，
	 * 再按模组自己的设置重新计算，从而让模组的"反转 Y 轴"完全接管原版选项。
	 */
	@ModifyArgs(
			method = "turnPlayer(D)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
			)
	)
	private static void linearaim$applyAxisSettings(Args args) {
		LinearAimConfig config = LinearAimConfig.get();
		if (!config.enabled) {
			return;
		}

		double pitchScale = config.getPitchScale();
		boolean invert = config.invertY;
		if (pitchScale == 1.0D && !invert) {
			return;
		}

		double y = (Double) args.get(1);
		boolean vanillaInvert = Minecraft.getInstance().options.invertMouseY().get();
		double baseY = vanillaInvert ? -y : y;

		args.set(1, baseY * pitchScale * (invert ? -1.0D : 1.0D));
	}
}
