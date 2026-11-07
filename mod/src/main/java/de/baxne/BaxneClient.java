package de.baxne;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class BaxneClient implements ClientModInitializer {
    public static final String MOD_ID = "baxnes_crosshair";
    private static final KeyMapping.Category CAT =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static KeyMapping MENU_KEY, SPEAR_KEY, STUN_KEY, ELYTRA_KEY, CLICKER_KEY, XRAY_KEY, AIM_KEY;
    public static KeyMapping[] ALL_KEYS;

    private static KeyMapping reg(String name, int key) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.baxne." + name, InputConstants.Type.KEYSYM, key, CAT));
    }

    @Override
    public void onInitializeClient() {
        MENU_KEY    = reg("menu",    GLFW.GLFW_KEY_RIGHT_SHIFT);
        SPEAR_KEY   = reg("spear",   GLFW.GLFW_KEY_R);
        STUN_KEY    = reg("stun",    GLFW.GLFW_KEY_G);
        ELYTRA_KEY  = reg("elytra",  GLFW.GLFW_KEY_H);
        CLICKER_KEY = reg("clicker", GLFW.GLFW_KEY_J);
        XRAY_KEY    = reg("xray",    GLFW.GLFW_KEY_X);
        AIM_KEY     = reg("aim",     GLFW.GLFW_KEY_K);
        ALL_KEYS = new KeyMapping[]{MENU_KEY, SPEAR_KEY, STUN_KEY, ELYTRA_KEY, CLICKER_KEY, XRAY_KEY, AIM_KEY};

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (MENU_KEY.consumeClick()) mc.setScreen(new MenuScreen());
            while (AIM_KEY.consumeClick()) Config.aimbot = !Config.aimbot;
            Aimbot.tick();
            Modules.tick();
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "shape_crosshair"), CrosshairHud::render);
    }
}
