package com.linearaim.client.config;

/**
 * 灵敏度换算工具。
 *
 * <p>原版的鼠标转向速度由 {@code MouseHandler#turnPlayer} 计算：
 * <pre>
 *   f = 原版滑块值 * 0.6 + 0.2
 *   最终乘数 = 8 * f^3
 * </pre>
 * 这条三次曲线就是原版"灵敏度不线性"的根源（滑块 0.5 时乘数恰好为 1.0）。
 *
 * <p>LinearAim 的做法是把这个乘数直接当成线性值使用：把目标乘数反解成一个
 * "等效的原版滑块值"喂回原版公式，原版就会算出我们想要的线性乘数。这样
 * 开镜降敏（1/8）、平滑视角、反转轴等原版行为全部保持原样。
 */
public final class AimMath {
	private AimMath() {
	}

	/** 原版灵敏度滑块默认档位（0.5），对应乘数 1.0。 */
	public static final double VANILLA_DEFAULT_SENSITIVITY = 0.5D;

	/**
	 * {@code Entity#turn(dx, dy)} 里 turn 参数到度数的换算系数：
	 * 实际转角（度）= turn 参数 * 0.15。
	 */
	public static final double DEGREES_PER_TURN_UNIT = 0.15D;

	/** CS2 默认的 m_yaw / m_pitch（度/计数）。 */
	public static final double CS_DEFAULT_M = 0.022D;

	/**
	 * 把 CS2 语义（sensitivity * m_yaw = 每计数转多少度）换算成 Minecraft 的转速乘数。
	 */
	public static double csToMultiplier(double sensitivity, double m) {
		return sensitivity * m / DEGREES_PER_TURN_UNIT;
	}

	/**
	 * 原版默认（乘数 1.0，即 0.15 度/计数）对应的 CS2 灵敏度，约 6.818。
	 */
	public static double vanillaEquivalentSensitivity() {
		return DEGREES_PER_TURN_UNIT / CS_DEFAULT_M;
	}

	/** 原版公式：滑块值 -> 最终转速乘数。 */
	public static double vanillaMultiplier(double sensitivity) {
		double f = sensitivity * 0.6F + 0.2F;
		return 8.0D * f * f * f;
	}

	/** 反解原版公式：目标乘数 -> 等效的原版滑块值。 */
	public static double toVanillaSensitivity(double multiplier) {
		double f = Math.cbrt(multiplier / 8.0D);
		return (f - 0.2F) / 0.6F;
	}

	/** 按给定步长吸附。 */
	public static double snap(double value, double step) {
		if (step <= 0.0D) {
			return value;
		}
		return Math.round(value / step) * step;
	}

	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		return value > max ? max : value;
	}
}
