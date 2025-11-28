package com.minecartupgrade;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.mixin.gamerule.GameRulesAccessor;
import net.minecraft.world.level.GameRules;

public final class MinecartUpgradeGameRules {
	private static final String RULE_NAME = "minecartMaxSpeed";

	public static volatile GameRules.Key<DoubleRule> MINECART_MAX_SPEED;

	private MinecartUpgradeGameRules() {
	}

	public static void init() {
		register();
	}

	public static synchronized void register() {
		if (MINECART_MAX_SPEED != null && GameRuleRegistry.hasRegistration(RULE_NAME)) {
			return;
		}

		GameRules.Type<DoubleRule> type = GameRuleFactory.createDoubleRule(
			MinecartSpeedHelper.DEFAULT_BLOCKS_PER_SECOND,
			MinecartSpeedHelper.MIN_BLOCKS_PER_SECOND,
			(server, rule) -> MinecartUpgradeConfigManager.updateFromGameRule(server, rule.get())
		);
		MINECART_MAX_SPEED = registerOrReuse(RULE_NAME, type);
		DoubleRule sample = type.createRule();
		MinecartUpgradeMod.LOGGER.debug("Registered {} gamerule with value type {}", RULE_NAME, sample.getClass().getSimpleName());
	}

	static String getRuleName() {
		return RULE_NAME;
	}

	@SuppressWarnings("unchecked")
	public static GameRules.Key<DoubleRule> resolveKey() {
		if (MINECART_MAX_SPEED != null) {
			return MINECART_MAX_SPEED;
		}

		for (GameRules.Key<?> key : GameRulesAccessor.getRuleTypes().keySet()) {
			if (key.toString().equals(RULE_NAME)) {
				MINECART_MAX_SPEED = (GameRules.Key<DoubleRule>) key;
				return MINECART_MAX_SPEED;
			}
		}

		return null;
	}

	private static GameRules.Key<DoubleRule> registerOrReuse(String name, GameRules.Type<DoubleRule> type) {
		if (GameRuleRegistry.hasRegistration(name)) {
			GameRules.Key<DoubleRule> existing = findExisting(name);
			GameRules.Type<?> existingType = GameRulesAccessor.getRuleTypes().get(existing);
			if (!(existingType.createRule() instanceof DoubleRule)) {
				MinecartUpgradeMod.LOGGER.warn(
					"Existing gamerule {} had type {}, replacing with DoubleRule",
					name,
					existingType.createRule().getClass().getSimpleName()
				);
				GameRulesAccessor.getRuleTypes().put(existing, type);
			}
			return existing;
		}
		return GameRuleRegistry.register(name, GameRules.Category.MISC, type);
	}

	@SuppressWarnings("unchecked")
	private static GameRules.Key<DoubleRule> findExisting(String name) {
		for (GameRules.Key<?> key : GameRulesAccessor.getRuleTypes().keySet()) {
			if (key.toString().equals(name)) {
				return (GameRules.Key<DoubleRule>) key;
			}
		}
		throw new IllegalStateException("Game rule '" + name + "' already registered but key not found");
	}
}
