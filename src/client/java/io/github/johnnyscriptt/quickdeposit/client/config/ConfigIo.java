package io.github.johnnyscriptt.quickdeposit.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.johnnyscriptt.quickdeposit.QuickDepositMod;
import io.github.johnnyscriptt.quickdeposit.core.config.QuickDepositConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Loads and saves the single {@link QuickDepositConfig} instance at config/quickdeposit.json. */
public final class ConfigIo {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static QuickDepositConfig config;

    private ConfigIo() {
    }

    public static synchronized QuickDepositConfig get() {
        if (config == null) {
            config = load();
        }
        return config;
    }

    public static synchronized void save() {
        if (config == null) {
            return;
        }
        config.clamp();
        try {
            Files.writeString(path(), GSON.toJson(config));
        } catch (IOException e) {
            QuickDepositMod.LOGGER.warn("Could not save quickdeposit.json", e);
        }
    }

    private static QuickDepositConfig load() {
        Path file = path();
        if (Files.exists(file)) {
            try {
                QuickDepositConfig loaded = GSON.fromJson(Files.readString(file), QuickDepositConfig.class);
                if (loaded != null) {
                    loaded.clamp();
                    return loaded;
                }
            } catch (Exception e) {
                QuickDepositMod.LOGGER.warn("Could not read quickdeposit.json, using defaults", e);
            }
        }
        return new QuickDepositConfig();
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("quickdeposit.json");
    }
}
