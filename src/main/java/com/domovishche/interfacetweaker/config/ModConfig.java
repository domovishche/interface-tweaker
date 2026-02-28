package com.domovishche.interfacetweaker.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ModConfig {

    public static final String CATEGORY_CLIENT = "client";

    private static Configuration config;

    // ── Tooltip ID hider ──────────────────────────────────────────────────────
    public static boolean creativeOnly = true;

    // ── Enchantment glint ─────────────────────────────────────────────────────
    /**
     * Enchantments in this set will not contribute to glint.
     * If ALL enchantments on an item are suppressed, the item loses its glint.
     * If at least one enchantment is NOT in this set, glint still shows.
     * Format: registry names, e.g. "minecraft:sharpness"
     */
    public static Set<String> noGlintEnchantments = new HashSet<>();

    /**
     * Items in this set always show glint regardless of enchantments.
     * Format: registry names, e.g. "minecraft:stick"
     */
    public static Set<String> forceGlintItems = new HashSet<>();

    public static void init(File configFile) {
        config = new Configuration(configFile);
        load();
    }

    public static void load() {
        config.load();

        creativeOnly = config.get(
                Configuration.CATEGORY_GENERAL,
                "creativeOnly",
                true,
                "If true, item/block IDs in advanced tooltips (F3+H) are only visible in creative mode."
        ).getBoolean(true);

        String[] noGlintDefault = new String[0];
        noGlintEnchantments = new HashSet<>(Arrays.asList(config.get(
                Configuration.CATEGORY_GENERAL,
                "noGlintEnchantments",
                noGlintDefault,
                "Enchantments listed here will not contribute to glint.\n" +
                        "If ALL enchantments on an item are in this list, the item has no glint.\n" +
                        "Use registry names, e.g. minecraft:sharpness"
        ).getStringList()));

        String[] forceGlintDefault = new String[0];
        forceGlintItems = new HashSet<>(Arrays.asList(config.get(
                Configuration.CATEGORY_GENERAL,
                "forceGlintItems",
                forceGlintDefault,
                "Items listed here always show glint regardless of enchantments.\n" +
                        "Use registry names, e.g. minecraft:stick"
        ).getStringList()));

        if (config.hasChanged()) config.save();
    }

    private ModConfig() {}
}
