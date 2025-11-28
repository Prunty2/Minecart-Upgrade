package com.minecartupgrade;

public final class MinecartUpgradeConfig {
	private double maxBlocksPerSecond = MinecartSpeedHelper.DEFAULT_BLOCKS_PER_SECOND;

	public double getMaxBlocksPerSecond() {
		return this.maxBlocksPerSecond;
	}

	public void setMaxBlocksPerSecond(double maxBlocksPerSecond) {
		this.maxBlocksPerSecond = maxBlocksPerSecond;
	}

	public static MinecartUpgradeConfig createDefault() {
		return new MinecartUpgradeConfig();
	}
}
