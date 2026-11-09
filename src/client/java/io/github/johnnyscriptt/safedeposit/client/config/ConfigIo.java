package io.github.johnnyscriptt.safedeposit.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.johnnyscriptt.safedeposit.SafeDepositMod;
import io.github.johnnyscriptt.safedeposit.core.config.SafeDepositConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Loads and saves the single {@link SafeDepositConfig} instance at config/safedeposit.json. */
public final class ConfigIo {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SafeDepositConfig config;

    private ConfigIo() {
    }

    public static synchronized SafeDepositConfig get() {
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
            SafeDepositMod.LOGGER.warn("Could not save safedeposit.json", e);
        }
    }

    private static SafeDepositConfig load() {
        Path file = path();
        if (Files.exists(file)) {
            try {
                SafeDepositConfig loaded = GSON.fromJson(Files.readString(file), SafeDepositConfig.class);
                if (loaded != null) {
                    loaded.clamp();
                    return loaded;
                }
            } catch (Exception e) {
                SafeDepositMod.LOGGER.warn("Could not read safedeposit.json, using defaults", e);
            }
        }
        return new SafeDepositConfig();
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("safedeposit.json");
    }
}
