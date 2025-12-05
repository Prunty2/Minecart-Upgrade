package com.minecartupgrade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.minecartupgrade.mixin.DoubleRuleAccessor;
import com.minecartupgrade.mixin.GameRulesInstanceAccessor;
import net.fabricmc.fabric.mixin.gamerule.GameRulesAccessor;

public final class MinecartUpgradeConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
		.getConfigDir()
		.resolve(MinecartUpgradeMod.MOD_ID + ".json");
	private static final double EPSILON = 0.0001;

	private static MinecartUpgradeConfig config = MinecartUpgradeConfig.createDefault();

	private MinecartUpgradeConfigManager() {
	}

	public static synchronized MinecartUpgradeConfig load() {
		boolean dirty = false;
		config = MinecartUpgradeConfig.createDefault();

		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				MinecartUpgradeConfig read = GSON.fromJson(reader, MinecartUpgradeConfig.class);
				if (read != null) {
					config = read;
				}
			} catch (IOException | JsonParseException exception) {
				MinecartUpgradeMod.LOGGER.warn("Failed to read {}, using defaults instead", CONFIG_PATH, exception);
				dirty = true;
			}
		} else {
			dirty = true;
		}

		dirty |= sanitizeAndApply(config);

		if (dirty) {
			save();
		}

		return config;
	}

	public static synchronized void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			MinecartUpgradeMod.LOGGER.error("Failed to write config to {}", CONFIG_PATH, exception);
		}
	}

	public static synchronized void syncRuleFromConfig(MinecraftServer server) {
		sanitizeAndApply(config);
		double configured = config.getMaxBlocksPerSecond();
		save();

		GameRules gameRules = server.getGameRules();
		DoubleRule rule = getRuleOrNull(gameRules);
		if (rule == null) {
			MinecartUpgradeMod.LOGGER.warn("Minecart max speed gamerule missing on server start; skipping sync.");
			return;
		}
		if (rule != null && Math.abs(rule.get() - configured) > EPSILON) {
			// Use validate() for compatibility with older Fabric API versions
			// that don't have DoubleRule.set(double, MinecraftServer)
			if (rule.validate(Double.toString(configured))) {
				((DoubleRuleAccessor) (GameRules.Value<?>) rule).minecartupgrade$invokeOnChanged(server);
			}
		}
	}

	public static synchronized void updateFromGameRule(MinecraftServer server, double newBlocksPerSecond) {
		if (Double.isNaN(newBlocksPerSecond)) {
			return;
		}

		double clamped = clamp(newBlocksPerSecond);
		MinecartSpeedHelper.updateConfiguredBlocksPerSecond(clamped);
		if (Math.abs(config.getMaxBlocksPerSecond() - clamped) > EPSILON) {
			config.setMaxBlocksPerSecond(clamped);
			save();
		}
	}

	private static boolean sanitizeAndApply(MinecartUpgradeConfig configToApply) {
		double clamped = clamp(configToApply.getMaxBlocksPerSecond());
		boolean changed = Math.abs(clamped - configToApply.getMaxBlocksPerSecond()) > EPSILON;
		configToApply.setMaxBlocksPerSecond(clamped);
		MinecartSpeedHelper.updateConfiguredBlocksPerSecond(clamped);
		return changed;
	}

	private static double clamp(double value) {
		return Math.max(MinecartSpeedHelper.MIN_BLOCKS_PER_SECOND, value);
	}

	private static DoubleRule getRuleOrNull(GameRules gameRules) {
		GameRules.Key<DoubleRule> key = MinecartUpgradeGameRules.resolveKey();
		if (key == null) {
			return null;
		}

		try {
			GameRules.Value<?> rawRule = gameRules.getRule(key);
			if (rawRule instanceof DoubleRule doubleRule) {
				return doubleRule;
			}
			// Rule exists but is wrong type - this can happen if another mod registered a conflicting rule
			MinecartUpgradeMod.LOGGER.warn("GameRule {} has unexpected type {}, expected DoubleRule", 
				key, rawRule != null ? rawRule.getClass().getSimpleName() : "null");
			return null;
		} catch (IllegalArgumentException ignored) {
			// Rule not found in this GameRules instance, try to create it
			GameRules.Type<?> type = GameRulesAccessor.getRuleTypes().get(key);
			if (type == null || !(gameRules instanceof GameRulesInstanceAccessor accessor)) {
				return null;
			}

			GameRules.Value<?> sample = type.createRule();
			if (!(sample instanceof DoubleRule created)) {
				MinecartUpgradeMod.LOGGER.error("Expected DoubleRule for {}, got {}", key, sample.getClass().getSimpleName());
				return null;
			}

			accessor.minecartupgrade$getRules().put(key, created);
			return created;
		} catch (ClassCastException e) {
			// This happens when the gamerule was registered by vanilla/another mod with a different type
			MinecartUpgradeMod.LOGGER.warn("GameRule {} type mismatch - cannot cast to DoubleRule. Another mod may have registered a conflicting rule.", key);
			return null;
		}
	}
}
