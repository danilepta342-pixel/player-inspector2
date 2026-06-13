package com.playerinspector.client;

import com.playerinspector.client.config.ModConfig;
import com.playerinspector.client.screen.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PlayerInspectorClient implements ClientModInitializer {

    public static PlayerInspectorHud hud;
    public static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        hud = new PlayerInspectorHud();
        HudRenderCallback.EVENT.register(hud::render);

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playerinspector.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                "Player Inspector"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfigKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new ConfigScreen(null));
            }
        });
    }
}
