package com.playerinspector.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("playerinspector.json");

    private static ModConfig INSTANCE = new ModConfig();

    public enum HudPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER_LEFT, CENTER_RIGHT
    }

    public enum ArmorDisplay {
        BAR, PERCENT, BOTH
    }

    public HudPosition position = HudPosition.CENTER_LEFT;
    public int offsetX = 12;
    public int offsetY = 0;
    public ArmorDisplay armorDisplay = ArmorDisplay.BAR;
    public int holdTimeMs = 2000;
    public boolean showHpBar = true;
    public boolean showHpNumber = true;

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        try {
            if (CONFIG_PATH.toFile().exists()) {
                try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                    ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                    if (loaded != null) INSTANCE = loaded;
                }
            }
        } catch (Exception e) {
            INSTANCE = new ModConfig();
        }
    }

    public static void save() {
        try {
            CONFIG_PATH.getParent().toFile().mkdirs();
            try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
