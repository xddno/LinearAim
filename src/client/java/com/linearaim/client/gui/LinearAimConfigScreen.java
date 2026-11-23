package com.linearaim.client.gui;

import com.linearaim.client.config.AimMath;
import com.linearaim.client.config.LinearAimConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * LinearAim 的配置界面，分「灵敏度」和「高级」两页。
 */
public class LinearAimConfigScreen extends Screen {
	private static final int ROW_WIDTH = 200;
	private static final int PAGE_BASIC = 0;
	private static final int PAGE_ADVANCED = 1;

	private final Screen parent;
	private final LinearAimConfig config = LinearAimConfig.get();

	private int page = PAGE_BASIC;
	private int top;

	private StepSlider sensitivitySlider;
	private StepSlider yawSlider;
	private StepSlider pitchSlider;
	private StepSlider yScaleSlider;
	private EditBox sensitivityBox;
	private Checkbox invertYBox;
	private Checkbox enabledBox;

	private boolean typing;

	public LinearAimConfigScreen(Screen parent) {
		super(Component.translatable("linearaim.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.typing = false;
		int cx = this.width / 2;
		this.top = Math.max(4, (this.height - 212) / 2);

		this.addTabs(cx);

		if (this.page == PAGE_BASIC) {
			this.initBasicPage(cx);
		} else {
			this.initAdvancedPage(cx);
		}

		int bottomY = Math.min(this.top + 178, this.height - 42);
		this.addRenderableWidget(Button.builder(Component.translatable("linearaim.button.reset"),
						button -> this.resetToDefaults())
				.bounds(cx - 101, bottomY, 100, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
						button -> this.onClose())
				.bounds(cx + 1, bottomY, 100, 20).build());
	}

	private void addTabs(int cx) {
		int tabY = this.top + 18;
		Component basic = Component.translatable("linearaim.tab.basic");
		Component advanced = Component.translatable("linearaim.tab.advanced");

		Button basicTab = Button.builder(
						this.page == PAGE_BASIC ? wrap(basic) : basic,
						button -> this.switchPage(PAGE_BASIC))
				.bounds(cx - 101, tabY, 100, 18).build();
		basicTab.active = this.page != PAGE_BASIC;
		this.addRenderableWidget(basicTab);

		Button advancedTab = Button.builder(
						this.page == PAGE_ADVANCED ? wrap(advanced) : advanced,
						button -> this.switchPage(PAGE_ADVANCED))
				.bounds(cx + 1, tabY, 100, 18).build();
		advancedTab.active = this.page != PAGE_ADVANCED;
		this.addRenderableWidget(advancedTab);
	}

	private static Component wrap(Component component) {
		return Component.literal("【").append(component).append("】");
	}

	private void switchPage(int newPage) {
		if (this.page != newPage) {
			this.page = newPage;
			this.rebuildWidgets();
		}
	}

	private void initBasicPage(int cx) {
		int y0 = this.top + 46;

		this.sensitivitySlider = new StepSlider(cx - ROW_WIDTH / 2 + 2, y0, ROW_WIDTH - 4, 20,
				"linearaim.option.sensitivity",
				LinearAimConfig.MIN_SENSITIVITY, LinearAimConfig.MAX_SENSITIVITY,
				LinearAimConfig.SENSITIVITY_STEP, 3, this.config.getSensitivity(),
				value -> {
					this.config.sensitivity = value;
					this.config.save();
					this.syncSensitivityBox(false);
				});
		this.addRenderableWidget(this.sensitivitySlider);

		this.addRenderableWidget(Button.builder(Component.literal("−"),
						button -> this.nudge(-LinearAimConfig.SENSITIVITY_STEP))
				.bounds(cx - ROW_WIDTH / 2 - 22, y0, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"),
						button -> this.nudge(LinearAimConfig.SENSITIVITY_STEP))
				.bounds(cx + ROW_WIDTH / 2 + 2, y0, 20, 20).build());

		int y1 = y0 + 26;
		this.sensitivityBox = new EditBox(this.font, cx - ROW_WIDTH / 2, y1, ROW_WIDTH - 66, 20,
				Component.translatable("linearaim.option.sensitivity"));
		this.sensitivityBox.setMaxLength(12);
		this.sensitivityBox.setValue(format(this.config.getSensitivity(), 3));
		this.sensitivityBox.setResponder(this::onSensitivityTyped);
		this.addRenderableWidget(this.sensitivityBox);

		this.addRenderableWidget(Button.builder(Component.translatable("linearaim.button.apply"),
						button -> this.commitSensitivityBox())
				.bounds(cx + ROW_WIDTH / 2 - 62, y1, 62, 20).build());
	}

	private void initAdvancedPage(int cx) {
		int y0 = this.top + 46;

		this.yawSlider = new StepSlider(cx - ROW_WIDTH / 2, y0, ROW_WIDTH, 20,
				"linearaim.option.mYaw",
				LinearAimConfig.MIN_M, LinearAimConfig.MAX_M,
				LinearAimConfig.M_STEP, 3, this.config.getMYaw(),
				value -> {
					this.config.mYaw = value;
					this.config.save();
				});
		this.addRenderableWidget(this.yawSlider);

		int y1 = y0 + 24;
		this.pitchSlider = new StepSlider(cx - ROW_WIDTH / 2, y1, ROW_WIDTH, 20,
				"linearaim.option.mPitch",
				LinearAimConfig.MIN_M, LinearAimConfig.MAX_M,
				LinearAimConfig.M_STEP, 3, this.config.getMPitch(),
				value -> {
					this.config.mPitch = value;
					this.config.save();
				});
		this.addRenderableWidget(this.pitchSlider);

		int y2 = y1 + 24;
		this.yScaleSlider = new StepSlider(cx - ROW_WIDTH / 2, y2, ROW_WIDTH, 20,
				"linearaim.option.yScale",
				LinearAimConfig.MIN_Y_SCALE, LinearAimConfig.MAX_Y_SCALE,
				LinearAimConfig.Y_SCALE_STEP, 2, this.config.getSensitivityYScale(),
				value -> {
					this.config.sensitivityYScale = value;
					this.config.save();
				});
		this.addRenderableWidget(this.yScaleSlider);

		int y3 = y2 + 26;
		this.invertYBox = Checkbox.builder(Component.translatable("linearaim.option.invertY"), this.font)
				.pos(cx - ROW_WIDTH / 2, y3)
				.maxWidth(ROW_WIDTH)
				.selected(this.config.invertY)
				.onValueChange((checkbox, selected) -> {
					this.config.invertY = selected;
					this.config.save();
				})
				.build();
		this.addRenderableWidget(this.invertYBox);

		int y4 = y3 + 22;
		this.enabledBox = Checkbox.builder(Component.translatable("linearaim.option.enabled"), this.font)
				.pos(cx - ROW_WIDTH / 2, y4)
				.maxWidth(ROW_WIDTH)
				.selected(this.config.enabled)
				.onValueChange((checkbox, selected) -> {
					this.config.enabled = selected;
					this.config.save();
				})
				.build();
		this.addRenderableWidget(this.enabledBox);
	}

	private void nudge(double delta) {
		this.config.nudgeSensitivity(delta);
		this.config.save();
		if (this.sensitivitySlider != null) {
			this.sensitivitySlider.setCurrentValue(this.config.getSensitivity());
		}
		this.syncSensitivityBox(true);
	}

	private void onSensitivityTyped(String text) {
		this.typing = true;
		try {
			double parsed = Double.parseDouble(text.trim());
			if (parsed > 0.0D) {
				this.config.sensitivity = AimMath.clamp(
						AimMath.snap(parsed, LinearAimConfig.SENSITIVITY_STEP),
						LinearAimConfig.MIN_SENSITIVITY, LinearAimConfig.MAX_SENSITIVITY);
				this.config.save();
				if (this.sensitivitySlider != null) {
					this.sensitivitySlider.setCurrentValue(this.config.getSensitivity());
				}
			}
		} catch (NumberFormatException ignored) {
			// 输入过程中的中间状态（例如 "1." 或空串），忽略即可
		} finally {
			this.typing = false;
		}
	}

	private void commitSensitivityBox() {
		if (this.sensitivityBox == null) {
			return;
		}
		try {
			double parsed = Double.parseDouble(this.sensitivityBox.getValue().trim());
			if (parsed > 0.0D) {
				this.config.sensitivity = AimMath.clamp(
						AimMath.snap(parsed, LinearAimConfig.SENSITIVITY_STEP),
						LinearAimConfig.MIN_SENSITIVITY, LinearAimConfig.MAX_SENSITIVITY);
				this.config.save();
			}
		} catch (NumberFormatException ignored) {
			// 非法输入则回退到当前值
		}
		if (this.sensitivitySlider != null) {
			this.sensitivitySlider.setCurrentValue(this.config.getSensitivity());
		}
		this.syncSensitivityBox(true);
	}

	private void syncSensitivityBox(boolean force) {
		if (this.sensitivityBox == null || (this.typing && !force)) {
			return;
		}
		this.sensitivityBox.setValue(format(this.config.getSensitivity(), 3));
	}

	private void resetToDefaults() {
		this.config.resetToDefaults();
		this.config.save();
		this.rebuildWidgets();
	}

	private static String format(double value, int decimals) {
		return String.format(Locale.ROOT, "%." + decimals + "f", value);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		int cx = this.width / 2;
		graphics.drawCenteredString(this.font, this.title, cx, this.top + 4, 0xFFFFFF);

		Component hint;
		if (!this.config.enabled) {
			hint = Component.translatable("linearaim.hint.disabled");
		} else if (this.page == PAGE_BASIC) {
			hint = Component.translatable("linearaim.hint.enabled",
					format(this.config.getSensitivity(), 3),
					format(this.config.degreesPerCount(), 3));
		} else {
			hint = Component.translatable("linearaim.hint.advanced",
					format(this.config.getPitchScale(), 2));
		}
		graphics.drawCenteredString(this.font, hint, cx, this.height - 16, 0xFFA0A0A0);
	}

	@Override
	public void onClose() {
		this.config.save();
		this.minecraft.setScreen(this.parent);
	}
}
