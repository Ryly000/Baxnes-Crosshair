package de.baxne;

import java.util.HashSet;
import java.util.Set;

/** Alle Einstellungen (im Menü änderbar). */
public class Config {
    // Module
    public static boolean spearSwap = true;
    public static boolean autoStunSlam = true;
    public static boolean autoMaceSwap = true;
    public static boolean xray = false;
    public static boolean crosshairShapes = true;
    public static boolean elytraSwap = true;
    public static boolean autoClicker = false;

    // Aimbot
    public static boolean aimbot = false;
    public static boolean aimPlayers = true;
    public static boolean aimMobs = true;
    public static boolean aimAnimals = true;
    public static boolean aimOthers = true;
    public static boolean aimAllEntities = false;
    public static boolean aimAtHead = true;
    public static boolean aimVisibleOnly = true;
    public static boolean aimOnlyWhileAttack = false;
    public static double aimRange = 12;
    public static double aimSmooth = 4;     // 1 = sofort, höher = weicher
    public static double aimFov = 360;

    // Spear
    public static int spearSlot = 0;          // 0 = automatisch suchen, 1-9 = fester Slot
    public static int spearLungeDelay = 2;

    // Elytra
    public static boolean elytraAutoFirework = false;
    public static double fireworkMinSpeed = 0.8;   // Feuerwerk, wenn langsamer als das

    // Stun Slam
    public static int stunDelay = 3;
    public static double stunRange = 4.5;

    // Mace
    public static boolean maceForceBreach = false;   // false = automatisch Breach/Density
    public static double minFallForSlam = 1.5;

    // Autoclicker
    public static double cps = 10;
    public static boolean clickerRespectCooldown = true;
    public static boolean clickerOnlyEntities = true;

    // Crosshair-Formen (Index in Shapes.NAMES)
    public static int shapeShield = 2;   // Gegner blockt  -> X
    public static int shapeEnemy = 4;    // Gegner ohne Schild -> Raute
    public static int crosshairSize = 7;
    public static int crosshairColor = 0xFFFFFFFF;

    // Xray
    public static final Set<String> xrayBlocks = new HashSet<>(Set.of(
        "diamond_ore", "deepslate_diamond_ore", "ancient_debris", "emerald_ore",
        "deepslate_emerald_ore", "gold_ore", "deepslate_gold_ore", "iron_ore",
        "deepslate_iron_ore", "redstone_ore", "deepslate_redstone_ore",
        "lapis_ore", "deepslate_lapis_ore", "coal_ore", "deepslate_coal_ore",
        "copper_ore", "deepslate_copper_ore", "nether_gold_ore", "nether_quartz_ore",
        "chest", "ender_chest", "spawner", "lava", "water"));
}
