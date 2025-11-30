package com.minecartupgrade;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public final class MinecartSpeedHelper {
	public static final double DEFAULT_BLOCKS_PER_SECOND = 12.0;
	public static final double MIN_BLOCKS_PER_SECOND = 0.1;
	private static final double SAFE_CURVE_LIMIT_BLOCKS_PER_SECOND = 10.0; // don't clamp curves/slopes until 10 bps
	private static final double WATER_MULTIPLIER = 0.5;
	private static volatile double configuredBlocksPerSecond = DEFAULT_BLOCKS_PER_SECOND;

	private MinecartSpeedHelper() {
	}

	public static double computeMaxSpeed(AbstractMinecart minecart, boolean furnaceBoosted) {
		double baseSpeed = getConfiguredMaxSpeedPerTick();
		baseSpeed = maybeCapForTrackShape(minecart, furnaceBoosted, baseSpeed);
		if (minecart.isInWater()) {
			baseSpeed *= WATER_MULTIPLIER;
		}

		return baseSpeed;
	}

	public static double computeFurnaceSelfSpeed(AbstractMinecart minecart) {
		double baseSpeed = maybeCapForTrackShape(minecart, true, getConfiguredMaxSpeedPerTick());
		if (minecart.isInWater()) {
			baseSpeed *= WATER_MULTIPLIER;
		}

		return baseSpeed;
	}

	public static void updateConfiguredBlocksPerSecond(double blocksPerSecond) {
		configuredBlocksPerSecond = Math.max(MIN_BLOCKS_PER_SECOND, blocksPerSecond);
	}

	public static double getConfiguredBlocksPerSecond() {
		return configuredBlocksPerSecond;
	}

	private static double maybeCapForTrackShape(AbstractMinecart minecart, boolean furnaceBoosted, double speed) {
		if (!furnaceBoosted) {
			return speed;
		}

		double safeCurveLimit = SAFE_CURVE_LIMIT_BLOCKS_PER_SECOND / 20.0;
		if (speed <= safeCurveLimit) {
			return speed;
		}

		BlockPos railPos = minecart.getCurrentBlockPosOrRailBelow();
		BlockState state = minecart.level().getBlockState(railPos);
		if (!(state.getBlock() instanceof BaseRailBlock baseRailBlock)) {
			return speed;
		}

		RailShape shape = state.getValue(baseRailBlock.getShapeProperty());
		if (shape.isSlope()) {
			return Math.min(speed, safeCurveLimit);
		}

		boolean curve = shape == RailShape.NORTH_EAST
			|| shape == RailShape.NORTH_WEST
			|| shape == RailShape.SOUTH_EAST
			|| shape == RailShape.SOUTH_WEST;
		return curve ? Math.min(speed, safeCurveLimit) : speed;
	}

	private static double getConfiguredMaxSpeedPerTick() {
		return configuredBlocksPerSecond / 20.0;
	}
}
