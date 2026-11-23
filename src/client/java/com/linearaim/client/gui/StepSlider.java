package com.linearaim.client.gui;

import com.linearaim.client.config.AimMath;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.DoubleConsumer;

/**
 * 带固定步长的滑块：拖动/键盘调整后数值会被吸附到 step 的整数倍。
 */
public class StepSlider extends AbstractSliderButton {
	private final double min;
	private final double max;
	private final double step;
	private final int decimals;
	private final String labelKey;
	private final DoubleConsumer onChange;

	public StepSlider(int x, int y, int width, int height, String labelKey, double min, double max,
					  double step, int decimals, double initial, DoubleConsumer onChange) {
		super(x, y, width, height, Component.empty(), toRatio(initial, min, max));
		this.min = min;
		this.max = max;
		this.step = step;
		this.decimals = decimals;
		this.labelKey = labelKey;
		this.onChange = onChange;
		this.updateMessage();
	}

	private static double toRatio(double value, double min, double max) {
		if (max <= min) {
			return 0.0D;
		}
		return AimMath.clamp((value - min) / (max - min), 0.0D, 1.0D);
	}

	/** 当前吸附后的实际值。 */
	public double snappedValue() {
		return AimMath.clamp(AimMath.snap(this.min + this.value * (this.max - this.min), this.step),
				this.min, this.max);
	}

	/** 由外部（例如精确输入框）同步滑块位置。 */
	public void setCurrentValue(double value) {
		double ratio = toRatio(value, this.min, this.max);
		if (Math.abs(ratio - this.value) < 1.0E-9D) {
			this.updateMessage();
			return;
		}
		this.value = ratio;
		this.applyValue();
		this.updateMessage();
	}

	@Override
	protected void updateMessage() {
		String text = String.format(Locale.ROOT, "%." + this.decimals + "f", this.snappedValue());
		this.setMessage(Component.translatable(this.labelKey).append(": ").append(Component.literal(text)));
	}

	@Override
	protected void applyValue() {
		this.onChange.accept(this.snappedValue());
	}

	/** 左右键以 1 step 调整，而不是原版的"按像素比例"。 */
	@Override
	public boolean keyPressed(KeyEvent keyEvent) {
		if (this.canChangeValue && (keyEvent.isLeft() || keyEvent.isRight())) {
			double delta = keyEvent.isLeft() ? -this.step : this.step;
			this.setCurrentValue(this.snappedValue() + delta);
			return true;
		}
		return super.keyPressed(keyEvent);
	}
}
