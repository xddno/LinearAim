package com.linearaim.client;

import com.linearaim.LinearAim;
import com.linearaim.client.config.LinearAimConfig;
import com.linearaim.client.gui.LinearAimConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class LinearAimClient implements ClientModInitializer {
	private static KeyMapping openConfigKey;

	@Override
	public void onInitializeClient() {
		// 尽早加载配置，保证 mixin 第一次读取时已经就绪
		LinearAimConfig.get();

		// CS2 风格的客户端命令
		LinearAimCommand.register();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(LinearAim.MOD_ID, "main"));

		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.linearaim.open_config",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_F10,
				category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.consumeClick()) {
				if (client.screen == null && client.player != null) {
					client.setScreen(new LinearAimConfigScreen(null));
				}
			}
		});

		LinearAim.LOGGER.info("LinearAim 已加载：原版灵敏度已被线性灵敏度接管");
	}
}
