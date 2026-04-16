package com.lucab.mounts_whistle.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lucab.mounts_whistle.MountsWhistle;
import net.neoforged.fml.loading.FMLPaths;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public static class Config {
        public static class ProtectionHandler {
            public boolean enableAutoRide = true;
            public boolean whistleShare = true;
            public boolean onlyRideOwner = true;
            public boolean mountInvulnerable = false;
        }

        public static class DespawnHandler {
            public boolean despawnWhenDrop = true;
            public int despawnDistance = 20;
            public int despawnTime = 60;
        }

        public static class InventoryHandler {
            public boolean equipSaddle = true;
            public boolean dropSaddle = false;
            public boolean dropArmor = false;
        }

        public static class AttributeModifier {
            public static class EnchantModifier {
                public float speedModifier = 0.05f;
                public float jumpModifier = 0.2f;
            }

            public float baseSpeedAttribute = 0.5f;
            public float baseJumpAttribute = 0.5f;
            public EnchantModifier enchantModifier = new EnchantModifier();
        }

        public List<String> mountsList = new ArrayList<>();
        public ProtectionHandler protection = new ProtectionHandler();
        public DespawnHandler despawn = new DespawnHandler();
        public InventoryHandler inventory = new InventoryHandler();
        public AttributeModifier attributeModifier = new AttributeModifier();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("mounts_whistle_server.json");
    public static Config INSTANCE = new Config();

    public static void load() {
        if (!CONFIG_PATH.toFile().exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
            INSTANCE = GSON.fromJson(reader, Config.class);
        } catch (IOException e) {
            MountsWhistle.LOGGER.error("[MountsWhistle] Failed to load config file!");
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            MountsWhistle.LOGGER.error("[MountsWhistle] Failed to save config file!");
        }
    }
}
