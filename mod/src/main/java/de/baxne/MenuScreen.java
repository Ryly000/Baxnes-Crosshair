package de.baxne;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Menü (Standard: RIGHT SHIFT) mit Kopf-Logo, Tabs, Slidern und Keybind-Editor. */
public class MenuScreen extends Screen {
    private static final Identifier LOGO = Identifier.fromNamespaceAndPath("baxnes_crosshair", "textures/gui/logo.png");
    private static final String[] TABS = {"Combat", "Spear", "Elytra", "Visuals", "Aim", "Keys"};
    private static int tab = 0;
    private KeyMapping listening = null;

    public MenuScreen() { super(Component.literal("Baxne's Crosshair")); }

    @Override
    protected void init() {
        int x = width / 2 - 150, y = height / 2 - 100;
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(TABS[i]), b -> { tab = idx; listening = null; rebuild(); })
                    .bounds(x + i * 51, y, 49, 20).build());
        }
        int row = y + 30;
        switch (tab) {
            case 0 -> {
                toggle(x, row, "Auto Stun Slam", () -> Config.autoStunSlam, v -> Config.autoStunSlam = v); row += 24;
                toggle(x, row, "Auto Mace Swap", () -> Config.autoMaceSwap, v -> Config.autoMaceSwap = v); row += 24;
                toggle(x, row, "Immer Breach (sonst Auto)", () -> Config.maceForceBreach, v -> Config.maceForceBreach = v); row += 24;
                slider(x, row, "Stun Delay (Ticks)", 0, 10, Config.stunDelay, v -> Config.stunDelay = (int) Math.round(v)); row += 24;
                slider(x, row, "Reichweite", 3, 6, Config.stunRange, v -> Config.stunRange = v); row += 24;
                toggle(x, row, "Autoclicker", () -> Config.autoClicker, v -> Config.autoClicker = v); row += 24;
                slider(x, row, "CPS", 1, 20, Config.cps, v -> Config.cps = v); row += 24;
                toggle(x, row, "Cooldown beachten", () -> Config.clickerRespectCooldown, v -> Config.clickerRespectCooldown = v);
            }
            case 1 -> {
                toggle(x, row, "Spear Swap", () -> Config.spearSwap, v -> Config.spearSwap = v); row += 24;
                slider(x, row, "Spear Slot (0=Auto)", 0, 9, Config.spearSlot, v -> Config.spearSlot = (int) Math.round(v)); row += 24;
                slider(x, row, "Lunge Delay (Ticks)", 0, 10, Config.spearLungeDelay, v -> Config.spearLungeDelay = (int) Math.round(v));
            }
            case 2 -> {
                toggle(x, row, "Elytra Swap", () -> Config.elytraSwap, v -> Config.elytraSwap = v); row += 24;
                toggle(x, row, "Auto Firework", () -> Config.elytraAutoFirework, v -> Config.elytraAutoFirework = v); row += 24;
                slider(x, row, "Firework unter Speed", 0.2, 2.0, Config.fireworkMinSpeed, v -> Config.fireworkMinSpeed = v);
            }
            case 3 -> {
                toggle(x, row, "Xray", () -> Config.xray, v -> { Config.xray = v; minecraft.levelRenderer.allChanged(); }); row += 24;
                toggle(x, row, "Crosshair-Formen", () -> Config.crosshairShapes, v -> Config.crosshairShapes = v); row += 24;
                shapePicker(x, row, "Form: Gegner mit Schild", true); row += 24;
                shapePicker(x, row, "Form: Gegner ohne Schild", false); row += 24;
                slider(x, row, "Crosshair Größe", 2, 16, Config.crosshairSize, v -> Config.crosshairSize = (int) Math.round(v));
            }
            case 4 -> {
                int rx = x + 152;
                toggle(x, row, "Aimbot", () -> Config.aimbot, v -> Config.aimbot = v, 148);
                toggle(rx, row, "Nur beim Angreifen", () -> Config.aimOnlyWhileAttack, v -> Config.aimOnlyWhileAttack = v, 148); row += 24;
                toggle(x, row, "Spieler", () -> Config.aimPlayers, v -> Config.aimPlayers = v, 148);
                toggle(rx, row, "Monster", () -> Config.aimMobs, v -> Config.aimMobs = v, 148); row += 24;
                toggle(x, row, "Tiere", () -> Config.aimAnimals, v -> Config.aimAnimals = v, 148);
                toggle(rx, row, "Andere Lebewesen", () -> Config.aimOthers, v -> Config.aimOthers = v, 148); row += 24;
                toggle(x, row, "Alle Entities", () -> Config.aimAllEntities, v -> Config.aimAllEntities = v, 148);
                toggle(rx, row, "Ziel: Kopf (sonst Körper)", () -> Config.aimAtHead, v -> Config.aimAtHead = v, 148); row += 24;
                toggle(x, row, "Nur sichtbare", () -> Config.aimVisibleOnly, v -> Config.aimVisibleOnly = v, 148); row += 24;
                slider(x, row, "Reichweite", 3, 64, Config.aimRange, v -> Config.aimRange = v, 148);
                slider(rx, row, "Smoothing", 1, 20, Config.aimSmooth, v -> Config.aimSmooth = v, 148); row += 24;
                slider(x, row, "FOV", 10, 360, Config.aimFov, v -> Config.aimFov = v, 148);
            }
            case 5 -> {
                for (KeyMapping k : BaxneClient.ALL_KEYS) {
                    final KeyMapping key = k;
                    addRenderableWidget(Button.builder(keyLabel(key), b -> { listening = key; b.setMessage(Component.literal("> Taste drücken <")); })
                            .bounds(x, row, 300, 20).build());
                    row += 24;
                }
            }
        }
    }

    private Component keyLabel(KeyMapping k) {
        return Component.translatable(k.getName()).append(": ").append(k.getTranslatedKeyMessage());
    }

    /** Neue Taste für das gerade wartende Keybind setzen (ESC = abbrechen, ENTF = unbelegt). */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening != null) {
            if (event.key() == 256) {                       // ESC
            } else if (event.key() == 261) {                // ENTF
                listening.setKey(InputConstants.UNKNOWN);
            } else {
                listening.setKey(InputConstants.getKey(event));
            }
            KeyMapping.resetMapping();
            minecraft.options.save();
            listening = null;
            rebuild();
            return true;
        }
        return super.keyPressed(event);
    }

    private void rebuild() { clearWidgets(); init(); }

    private void shapePicker(int x, int y, String name, boolean shield) {
        addRenderableWidget(Button.builder(shapeLabel(name, shield), b -> {
            if (shield) Config.shapeShield = (Config.shapeShield + 1) % Shapes.NAMES.length;
            else Config.shapeEnemy = (Config.shapeEnemy + 1) % Shapes.NAMES.length;
            b.setMessage(shapeLabel(name, shield));
        }).bounds(x, y, 300, 20).build());
    }

    private Component shapeLabel(String n, boolean shield) {
        return Component.literal(n + ": " + Shapes.NAMES[shield ? Config.shapeShield : Config.shapeEnemy]);
    }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set) { toggle(x, y, name, get, set, 300); }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set, int w) {
        addRenderableWidget(Button.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
        }).bounds(x, y, w, 20).build());
    }

    private Component label(String n, boolean on) { return Component.literal(n + ": " + (on ? "§aAN" : "§cAUS")); }

    private void slider(int x, int y, String name, double min, double max, double cur, Consumer<Double> set) { slider(x, y, name, min, max, cur, set, 300); }

    private void slider(int x, int y, String name, double min, double max, double cur, Consumer<Double> set, int w) {
        addRenderableWidget(new AbstractSliderButton(x, y, w, 20,
                Component.literal(name + ": " + fmt(cur)), (cur - min) / (max - min)) {
            @Override protected void updateMessage() { setMessage(Component.literal(name + ": " + fmt(min + value * (max - min)))); }
            @Override protected void applyValue() { set.accept(min + value * (max - min)); }
        });
    }

    private static String fmt(double d) { return d == Math.rint(d) ? String.valueOf((int) d) : String.format("%.1f", d); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(width / 2 - 160, height / 2 - 150, width / 2 + 160, height / 2 + 140, 0xCC101018);
        g.blit(RenderPipelines.GUI_TEXTURED, LOGO, width / 2 - 154, height / 2 - 146, 0, 0, 32, 32, 64, 64, 64, 64);
        g.drawString(font, "Baxne's Crosshair", width / 2 - 114, height / 2 - 134, 0xFFFFFFFF, true);
        super.render(g, mx, my, delta);
    }

    @Override public boolean isPauseScreen() { return false; }
}
