package de.baxne;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/** Tick-Logik: Spear-Swap, Stun Slam, Mace-Swap, Elytra, Autoclicker, Xray-Taste. */
public class Modules {
    private static final Minecraft MC = Minecraft.getInstance();

    private enum Stage { IDLE, AXE_WAIT, MACE_WAIT }
    private static Stage stage = Stage.IDLE;
    private static int timer = 0;
    private static Player stunTarget;
    private static int spearTimer = -1;
    private static int fireworkCooldown = 0;
    private static double clickAccumulator = 0;

    public static void tick() {
        if (MC.player == null || MC.level == null || MC.screen != null) return;

        // Toggles per Taste
        while (BaxneClient.CLICKER_KEY.consumeClick()) Config.autoClicker = !Config.autoClicker;
        while (BaxneClient.XRAY_KEY.consumeClick()) { Config.xray = !Config.xray; MC.levelRenderer.allChanged(); }

        // Spear: Taste -> Spear equippen, nach Delay Lunge + Angriff
        if (Config.spearSwap && BaxneClient.SPEAR_KEY.consumeClick()) {
            int slot = Config.spearSlot > 0 ? Config.spearSlot - 1 : Util.findHotbar(Util::isSpear);
            if (slot >= 0) { Util.select(slot); spearTimer = Config.spearLungeDelay; }
        }
        if (spearTimer >= 0 && spearTimer-- == 0) {
            MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
            Player t = Util.targetPlayer();
            if (t != null) Util.attack(t);
        }

        // Elytra: Brustplatte <-> Elytra
        if (Config.elytraSwap && BaxneClient.ELYTRA_KEY.consumeClick()) swapElytra();
        elytraFirework();

        // Auto Mace Swap: Ziel im Fadenkreuz + Fallen -> Breach (Rüstung) / Density (ohne)
        if (Config.autoMaceSwap && stage == Stage.IDLE && MC.player.fallDistance >= Config.minFallForSlam) {
            Player t = Util.targetPlayer();
            if (t != null) { int m = Util.chooseMace(t); if (m >= 0) Util.select(m); }
        }

        // Auto Stun Slam
        if (Config.autoStunSlam && BaxneClient.STUN_KEY.consumeClick() && stage == Stage.IDLE) {
            Player t = Util.targetPlayer();
            if (t != null && MC.player.distanceTo(t) <= Config.stunRange) {
                int axe = Util.findHotbar(Util::isAxe);
                if (axe >= 0) { Util.select(axe); stunTarget = t; stage = Stage.AXE_WAIT; timer = Config.stunDelay; }
            }
        }
        switch (stage) {
            case AXE_WAIT -> { if (timer-- <= 0) {
                if (stunTarget != null && stunTarget.isAlive()) Util.attack(stunTarget);
                int m = Util.chooseMace(stunTarget); if (m >= 0) Util.select(m);
                stage = Stage.MACE_WAIT; timer = Config.stunDelay; } }
            case MACE_WAIT -> { if (timer-- <= 0) {
                if (stunTarget != null && stunTarget.isAlive()) Util.attack(stunTarget);
                stage = Stage.IDLE; stunTarget = null; } }
            default -> {}
        }

        autoClick();
    }

    /** Autoclicker: hält man Linksklick, wird mit eingestellter CPS angegriffen. */
    private static void autoClick() {
        if (!Config.autoClicker || stage != Stage.IDLE || !MC.options.keyAttack.isDown()) { clickAccumulator = 0; return; }
        var hit = MC.hitResult;
        boolean entity = hit instanceof net.minecraft.world.phys.EntityHitResult;
        if (Config.clickerOnlyEntities && !entity) return;
        clickAccumulator += Config.cps / 20.0;
        if (clickAccumulator < 1) return;
        if (Config.clickerRespectCooldown && MC.player.getAttackStrengthScale(0f) < 0.95f) return;
        clickAccumulator -= 1;
        if (entity) Util.attack(((net.minecraft.world.phys.EntityHitResult) hit).getEntity());
    }

    private static void elytraFirework() {
        if (fireworkCooldown > 0) fireworkCooldown--;
        if (!Config.elytraAutoFirework || !MC.player.isFallFlying() || fireworkCooldown > 0) return;
        if (MC.player.getDeltaMovement().length() >= Config.fireworkMinSpeed) return;
        int fw = Util.findHotbar(s -> s.is(Items.FIREWORK_ROCKET));
        if (fw < 0) return;
        int old = MC.player.getInventory().getSelectedSlot();
        Util.select(fw);
        MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
        Util.select(old);
        fireworkCooldown = 20;
    }

    private static void swapElytra() {
        var inv = MC.player.getInventory();
        boolean wearing = MC.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).is(Items.ELYTRA);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var s = inv.getItem(i);
            boolean match = wearing ? s.getItem().toString().contains("chestplate") : s.is(Items.ELYTRA);
            if (!match) continue;
            int slot = i < 9 ? 36 + i : i;
            int id = MC.player.inventoryMenu.containerId;
            var click = net.minecraft.world.inventory.ClickType.PICKUP;
            MC.gameMode.handleInventoryMouseClick(id, slot, 0, click, MC.player);
            MC.gameMode.handleInventoryMouseClick(id, 6, 0, click, MC.player);   // 6 = Brustplatten-Slot
            MC.gameMode.handleInventoryMouseClick(id, slot, 0, click, MC.player);
            return;
        }
    }
}
