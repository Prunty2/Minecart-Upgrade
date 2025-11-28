package com.minecartrevamp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

public final class MinecartSpeedHelper {
	public static final double DEFAULT_MAX_SPEED = 12.0 / 20.0; // 12 blocks per second
	public static final double FURNACE_PUSH_MAX_SPEED = 12.0 / 20.0; // 12 blocks per second when pushed by furnace
	public static final double FURNACE_SELF_MAX_SPEED = 12.0 / 20.0; // 12 blocks per second for furnace minecarts themselves
	private static final double SAFE_CURVE_LIMIT = 25.0 / 20.0; // don't clamp curves/slopes until 25 bps
	private static final double WATER_MULTIPLIER = 0.5;

	private MinecartSpeedHelper() {
	}

	public static double computeMaxSpeed(AbstractMinecart minecart, boolean furnaceBoosted) {
		double baseSpeed = furnaceBoosted ? FURNACE_PUSH_MAX_SPEED : DEFAULT_MAX_SPEED;
		baseSpeed = maybeCapForTrackShape(minecart, furnaceBoosted, baseSpeed);
		if (minecart.isInWater()) {
			baseSpeed *= WATER_MULTIPLIER;
		}

		return baseSpeed;
	}

	public static double computeFurnaceSelfSpeed(AbstractMinecart minecart) {
		double baseSpeed = maybeCapForTrackShape(minecart, true, FURNACE_SELF_MAX_SPEED);
		if (minecart.isInWater()) {
			baseSpeed *= WATER_MULTIPLIER;
		}

		return baseSpeed;
	}

	private static double maybeCapForTrackShape(AbstractMinecart minecart, boolean furnaceBoosted, double speed) {
		if (!furnaceBoosted) {
			return speed;
		}

		if (speed <= SAFE_CURVE_LIMIT) {
			return speed;
		}

		BlockPos railPos = minecart.getCurrentBlockPosOrRailBelow();
		BlockState state = minecart.level().getBlockState(railPos);
		if (!(state.getBlock() instanceof BaseRailBlock baseRailBlock)) {
			return speed;
		}

		RailShape shape = state.getValue(baseRailBlock.getShapeProperty());
		if (shape.isSlope()) {
			return Math.min(speed, SAFE_CURVE_LIMIT);
		}

		boolean curve = shape == RailShape.NORTH_EAST
			|| shape == RailShape.NORTH_WEST
			|| shape == RailShape.SOUTH_EAST
			|| shape == RailShape.SOUTH_WEST;
		return curve ? Math.min(speed, SAFE_CURVE_LIMIT) : speed;
	}
}
