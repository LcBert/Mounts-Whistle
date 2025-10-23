package com.lucab.mounts_whistle;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.annotations.Expose;

public class ConfigSchema {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    @Expose
    public Map<String, Object> ONLY_RIDE_OWNER = new HashMap<String, Object>() {
        {
            put("//", "Define if a player can ride a mount that is not their own");
            put("value", true);
        }
    };

    @Expose
    public Map<String, Object> ENABLE_AUTO_RIDE = new HashMap<String, Object>() {
        {
            put("//", "Define if a player can automatically ride their mount");
            put("value", true);
        }
    };

    @Expose
    public Map<String, Object> MOUNTS_LIST = new HashMap<String, Object>() {
        {
            put("//", "Define the list of mounts consider by the whistle");
            put("value", List.of("minecraft:mule", "minecraft:donkey", "minecraft:horse"));
        }
    };

    public static ConfigSchema load(File configFile) {
        ConfigSchema config = new ConfigSchema();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config = GSON.fromJson(reader, ConfigSchema.class);
            } catch (Exception e) {
            }
        } else {
            configFile.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
        }

        return config;
    }
}
