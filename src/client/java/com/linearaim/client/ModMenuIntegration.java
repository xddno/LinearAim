package com.linearaim.client;

import com.linearaim.client.gui.LinearAimConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu 集成：在模组列表里给 LinearAim 提供一个"配置"按钮。
 *
 * <p>这个类只有在安装了 Mod Menu 时才会被实例化，所以 Mod Menu 属于软依赖。
 */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return LinearAimConfigScreen::new;
	}
}
