package com.minecartupgrade;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartUpgradeMod implements ModInitializer {
	public static final String MOD_ID = "minecartupgrade";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MinecartUpgradeGameRules.init();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> MinecartUpgradeConfigManager.load());
		ServerLifecycleEvents.SERVER_STARTED.register(server -> MinecartUpgradeConfigManager.syncRuleFromConfig(server));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> MinecartUpgradeConfigManager.save());
		LOGGER.info("Minecart Upgrade initialized");
	}
}
