package com.linearaim.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.linearaim.LinearAim;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * LinearAim 的配置，保存在 {@code config/linearaim.json}。
 *
 * <p>参数语义完全对齐主流 FPS：
 * <pre>
 *   每计数转角(水平) = sensitivity * m_yaw
 *   每计数转角(垂直) = sensitivity * m_pitch * sensitivity_y_scale
 * </pre>
 * 其中 {@code sensitivity} 就是主流 FPS 里那个数，{@code m_yaw} / {@code m_pitch} 是"度/计数"，
 * 默认 0.022。所以在主流 FPS 里用 6.25，这里也填 6.25 即可。
 *
 * <p>Minecraft 的 {@code Entity#turn} 会把 turn 参数乘以 0.15 得到度数，因此这里
 * 需要把主流 FPS 的度数换算成乘数：{@code multiplier = sensitivity * m / 0.15}。
 */
public final class LinearAimConfig {
	/** 当前配置格式版本：2 起 sensitivity 使用角度语义。 */
	private static final int CONFIG_VERSION = 2;

	/** 灵敏度可调范围与步长（1 step = 0.005）。 */
	public static final double MIN_SENSITIVITY = 0.05D;
	public static final double MAX_SENSITIVITY = 30.0D;
	public static final double SENSITIVITY_STEP = 0.005D;

	/** sensitivity_y_scale 范围与步长。 */
	public static final double MIN_Y_SCALE = 0.1D;
	public static final double MAX_Y_SCALE = 3.0D;
	public static final double Y_SCALE_STEP = 0.01D;

	/** m_yaw / m_pitch 范围与步长。 */
	public static final double MIN_M = 0.001D;
	public static final double MAX_M = 0.100D;
	public static final double M_STEP = 0.001D;

	public static final double DEFAULT_M = AimMath.DEFAULT_DEGREES_PER_COUNT;
	public static final double DEFAULT_Y_SCALE = 1.0D;
	/** 原版默认手感对应的线性灵敏度（约 6.82）。 */
	public static final double DEFAULT_SENSITIVITY = 6.82D;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("linearaim.json");

	private static LinearAimConfig instance;

	/** 配置格式版本，用于把旧配置迁移到角度语义。 */
	public int configVersion = 0;

	/** 是否启用线性灵敏度。关闭后完全回到原版行为。 */
	public boolean enabled = true;

	/** 线性灵敏度：每计数转角 = sensitivity * m_yaw 度。 */
	public double sensitivity = DEFAULT_SENSITIVITY;

	/** 垂直方向额外倍率（主流 FPS 的 sensitivity_y_scale）。 */
	public double sensitivityYScale = DEFAULT_Y_SCALE;

	/** 水平角度系数（主流 FPS 的 m_yaw），单位：度/计数。 */
	public double mYaw = DEFAULT_M;

	/** 垂直角度系数（主流 FPS 的 m_pitch），单位：度/计数。 */
	public double mPitch = DEFAULT_M;

	/** 反转 Y 轴（会接管原版的"反转鼠标"设置）。 */
	public boolean invertY = false;

	/** 旧版本的字段名，仅用于把老配置迁移到 sensitivityYScale。 */
	@SerializedName("verticalSensitivity")
	private Double legacyVerticalSensitivity;

	public static LinearAimConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static LinearAimConfig load() {
		if (Files.exists(PATH)) {
			try {
				LinearAimConfig loaded = GSON.fromJson(Files.readString(PATH), LinearAimConfig.class);
				if (loaded != null) {
					loaded.migrateLegacy();
					loaded.sanitize();
					return loaded;
				}
			} catch (Exception e) {
				LinearAim.LOGGER.warn("读取 LinearAim 配置失败，将使用默认值", e);
			}
		}

		LinearAimConfig fresh = new LinearAimConfig();
		fresh.configVersion = CONFIG_VERSION;
		fresh.migrateFromVanilla();
		fresh.save();
		return fresh;
	}

	/**
	 * 旧格式（v1）的 sensitivity 是"原版转速乘数"，与主流 FPS 的数值没有对应关系
	 * （旧值 6.25 在新语义下等于 42.6），等价迁移只会让人以为"还是太快"。
	 * 因此这里先备份旧文件，再把 sensitivity 重置为默认值，其余参数保留。
	 */
	private void migrateLegacy() {
		if (this.legacyVerticalSensitivity != null) {
			this.sensitivityYScale = this.legacyVerticalSensitivity;
			this.legacyVerticalSensitivity = null;
		}
		if (this.configVersion < CONFIG_VERSION) {
			backupLegacyFile();
			this.sensitivity = DEFAULT_SENSITIVITY;
			this.configVersion = CONFIG_VERSION;
			LinearAim.LOGGER.info("检测到旧版配置，已将 sensitivity 重置为默认的线性数值 {}（旧文件备份为 linearaim.json.v1.bak）",
					String.format(Locale.ROOT, "%.3f", DEFAULT_SENSITIVITY));
		}
	}

	/** 迁移前把旧配置复制一份，任何情况下都不丢数据。 */
	private static void backupLegacyFile() {
		try {
			Path backup = PATH.resolveSibling("linearaim.json.v1.bak");
			if (!Files.exists(backup) && Files.exists(PATH)) {
				Files.copy(PATH, backup);
			}
		} catch (IOException e) {
			LinearAim.LOGGER.warn("备份旧配置失败", e);
		}
	}

	/**
	 * 首次使用（还没有配置文件）时，把原版灵敏度换算成等效的线性灵敏度，
	 * 这样装上模组后手感不会突然变化。
	 */
	private void migrateFromVanilla() {
		try {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft != null && minecraft.options != null) {
				double vanilla = minecraft.options.sensitivity().get();
				this.sensitivity = AimMath.clamp(
						AimMath.snap(AimMath.vanillaMultiplier(vanilla)
								* AimMath.vanillaEquivalentSensitivity(), SENSITIVITY_STEP),
						MIN_SENSITIVITY, MAX_SENSITIVITY);
				LinearAim.LOGGER.info("已从原版灵敏度 {} 换算为线性灵敏度 {}",
						String.format(Locale.ROOT, "%.3f", vanilla),
						String.format(Locale.ROOT, "%.3f", this.sensitivity));
			}
		} catch (Throwable throwable) {
			LinearAim.LOGGER.warn("无法读取原版灵敏度，使用默认值", throwable);
		}
	}

	/** 校正越界或未吸附的数值。 */
	public void sanitize() {
		this.sensitivity = AimMath.clamp(
				AimMath.snap(this.sensitivity, SENSITIVITY_STEP), MIN_SENSITIVITY, MAX_SENSITIVITY);
		this.sensitivityYScale = AimMath.clamp(
				AimMath.snap(this.sensitivityYScale, Y_SCALE_STEP), MIN_Y_SCALE, MAX_Y_SCALE);
		this.mYaw = AimMath.clamp(AimMath.snap(this.mYaw, M_STEP), MIN_M, MAX_M);
		this.mPitch = AimMath.clamp(AimMath.snap(this.mPitch, M_STEP), MIN_M, MAX_M);
	}

	public double getSensitivity() {
		return AimMath.clamp(AimMath.snap(this.sensitivity, SENSITIVITY_STEP),
				MIN_SENSITIVITY, MAX_SENSITIVITY);
	}

	public double getSensitivityYScale() {
		return AimMath.clamp(AimMath.snap(this.sensitivityYScale, Y_SCALE_STEP), MIN_Y_SCALE, MAX_Y_SCALE);
	}

	public double getMYaw() {
		return AimMath.clamp(AimMath.snap(this.mYaw, M_STEP), MIN_M, MAX_M);
	}

	public double getMPitch() {
		return AimMath.clamp(AimMath.snap(this.mPitch, M_STEP), MIN_M, MAX_M);
	}

	/**
	 * 垂直方向相对于水平的额外倍率。
	 * 水平倍率已经包含在 {@code sensitivity * m_yaw} 里，这里只处理剩下的部分。
	 */
	public double getPitchScale() {
		return (getMPitch() / getMYaw()) * getSensitivityYScale();
	}

	/** 每计数转多少度（水平）。 */
	public double degreesPerCount() {
		return getSensitivity() * getMYaw();
	}

	/** 以 0.005 为单位步进（step 可以是负数）。 */
	public void nudgeSensitivity(double step) {
		this.sensitivity = AimMath.clamp(
				AimMath.snap(this.sensitivity + step, SENSITIVITY_STEP),
				MIN_SENSITIVITY, MAX_SENSITIVITY);
	}

	public void resetToDefaults() {
		this.enabled = true;
		this.sensitivity = DEFAULT_SENSITIVITY;
		this.sensitivityYScale = DEFAULT_Y_SCALE;
		this.mYaw = DEFAULT_M;
		this.mPitch = DEFAULT_M;
		this.invertY = false;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(this));
		} catch (IOException e) {
			LinearAim.LOGGER.error("保存 LinearAim 配置失败: {}", PATH, e);
		}
	}
}
