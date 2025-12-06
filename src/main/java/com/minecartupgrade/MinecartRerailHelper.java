package com.minecartupgrade;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class MinecartRerailHelper {
	private static final double EPSILON = 1.0E-4;
	private static final int[][] SEARCH_OFFSETS = buildSearchOffsets();

	private MinecartRerailHelper() {
	}

	public static boolean rerailIfSafe(AbstractMinecart minecart) {
		Level level = minecart.level();
		if (level.isClientSide()) {
			return false;
		}

		Vec3 direction = pickSearchDirection(minecart);
		Vec3 incoming = minecart.getDeltaMovement();
		if (incoming.lengthSqr() < EPSILON && minecart instanceof RailDirectionTracker tracker) {
			Vec3 remembered = tracker.minecartupgrade$getLastRailDirection();
			if (remembered.lengthSqr() > EPSILON) {
				incoming = remembered;
			}
		}
		double speed = incoming.length();
		double searchDistance = Math.min(Math.max(3.0, speed * 6.0 + 2.0), 48.0);
		double step = 0.25;

		for (double travelled = 0.0; travelled <= searchDistance; travelled += step) {
			Vec3 sample = minecart.position().add(direction.scale(travelled));
			BlockPos railPos = findDirectionalRail(level, BlockPos.containing(sample), direction);
			if (railPos != null && snapToRail(minecart, railPos, incoming)) {
				return true;
			}
		}

		Vec3 weakDirection = direction.lengthSqr() > EPSILON ? direction : incoming;
		BlockPos fallback = findDirectionalRail(level, minecart.blockPosition(), weakDirection);
		return fallback != null && snapToRail(minecart, fallback, incoming);
	}

	private static Vec3 pickSearchDirection(AbstractMinecart minecart) {
		Vec3 movement = minecart.getDeltaMovement();
		if (movement.lengthSqr() > 1.0E-6) {
			return movement.normalize();
		}

		Direction facing = minecart.getMotionDirection();
		Vec3 fallback = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
		return fallback.lengthSqr() > 0.0 ? fallback.normalize() : new Vec3(0.0, 0.0, 1.0);
	}

	@Nullable
	private static BlockPos findDirectionalRail(Level level, BlockPos basePos, Vec3 forward) {
		BlockPos bestPos = null;
		double bestScore = -Double.MAX_VALUE;

		for (int[] offset : SEARCH_OFFSETS) {
			BlockPos candidate = new BlockPos(basePos.getX() + offset[0], basePos.getY() + offset[1], basePos.getZ() + offset[2]);
			BlockPos railPos = getRailPos(level, candidate);
			if (railPos == null) {
				continue;
			}

			Vec3 delta = new Vec3(railPos.getX() + 0.5 - basePos.getX(), railPos.getY() + 0.5 - basePos.getY(), railPos.getZ() + 0.5 - basePos.getZ());
			double dot = forward.lengthSqr() > EPSILON ? forward.normalize().dot(delta.normalize()) : 0.0;
			if (forward.lengthSqr() > EPSILON && dot < -0.2) {
				continue;
			}

			double score = dot - delta.lengthSqr() * 0.01;
			if (score > bestScore) {
				bestScore = score;
				bestPos = railPos;
			}
		}

		return bestPos;
	}

	@Nullable
	private static BlockPos getRailPos(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (BaseRailBlock.isRail(state)) {
			return pos;
		}

		BlockPos below = pos.below();
		if (below != pos) {
			BlockState belowState = level.getBlockState(below);
			if (BaseRailBlock.isRail(belowState)) {
				return below;
			}
		}

		return null;
	}

	private static boolean snapToRail(AbstractMinecart minecart, BlockPos railPos, Vec3 incomingMotion) {
		Level level = minecart.level();
		BlockState state = level.getBlockState(railPos);
		if (!(state.getBlock() instanceof BaseRailBlock railBlock)) {
			return false;
		}

		RailShape shape = state.getValue(railBlock.getShapeProperty());
		Pair<Vec3i, Vec3i> exits = getExits(shape);
		Vec3 exitA = new Vec3(exits.getFirst().getX(), exits.getFirst().getY(), exits.getFirst().getZ()).scale(0.5);
		Vec3 exitB = new Vec3(exits.getSecond().getX(), exits.getSecond().getY(), exits.getSecond().getZ()).scale(0.5);
		Vec3 start = railPos.getBottomCenter().add(exitA);
		Vec3 end = railPos.getBottomCenter().add(exitB);

		Vec3 trackVec = end.subtract(start);
		Vec3 projected = projectOntoSegment(minecart.position(), start, end);
		minecart.setPos(projected.add(0.0, 0.1, 0.0));
		minecart.resetFallDistance();

		Vec3 aligned = alignDirection(incomingMotion, exitA, exitB, trackVec);
		double speed = Math.max(minecart.getDeltaMovement().length(), incomingMotion.length());
		if (aligned.lengthSqr() > 1.0E-6 && speed > EPSILON) {
			minecart.setDeltaMovement(aligned.normalize().scale(speed));
		}
		if (minecart instanceof RailDirectionTracker tracker && aligned.lengthSqr() > 1.0E-6) {
			tracker.minecartupgrade$setLastRailDirection(aligned.normalize());
		}

		return true;
	}

	private static Pair<Vec3i, Vec3i> getExits(RailShape shape) {
		return switch (shape) {
			case NORTH_SOUTH -> Pair.of(new Vec3i(0, 0, -1), new Vec3i(0, 0, 1));
			case EAST_WEST -> Pair.of(new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0));
			case ASCENDING_EAST -> Pair.of(new Vec3i(-1, 0, 0), new Vec3i(1, 1, 0));
			case ASCENDING_WEST -> Pair.of(new Vec3i(-1, 1, 0), new Vec3i(1, 0, 0));
			case ASCENDING_NORTH -> Pair.of(new Vec3i(0, 1, -1), new Vec3i(0, 0, 1));
			case ASCENDING_SOUTH -> Pair.of(new Vec3i(0, 0, -1), new Vec3i(0, 1, 1));
			case SOUTH_EAST -> Pair.of(new Vec3i(1, 0, 0), new Vec3i(0, 0, 1));
			case SOUTH_WEST -> Pair.of(new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1));
			case NORTH_WEST -> Pair.of(new Vec3i(-1, 0, 0), new Vec3i(0, 0, -1));
			case NORTH_EAST -> Pair.of(new Vec3i(1, 0, 0), new Vec3i(0, 0, -1));
		};
	}

	private static Vec3 projectOntoSegment(Vec3 point, Vec3 start, Vec3 end) {
		Vec3 segment = end.subtract(start);
		double lenSq = segment.lengthSqr();
		if (lenSq < EPSILON) {
			return start;
		}

		double t = point.subtract(start).dot(segment) / lenSq;
		t = Math.min(1.0, Math.max(0.0, t));
		return start.add(segment.scale(t));
	}

	private static Vec3 alignDirection(Vec3 incomingMotion, Vec3 exitA, Vec3 exitB, Vec3 trackVec) {
		Vec3 incoming = incomingMotion.lengthSqr() < EPSILON ? null : incomingMotion.normalize();
		Vec3 dirA = normalizeOrZero(exitA);
		Vec3 dirB = normalizeOrZero(exitB);

		if (incoming == null) {
			if (dirA.lengthSqr() > EPSILON) {
				return dirA;
			}
			if (dirB.lengthSqr() > EPSILON) {
				return dirB;
			}
			return trackVec;
		}

		double dotA = dirA.lengthSqr() > EPSILON ? incoming.dot(dirA) : Double.NEGATIVE_INFINITY;
		double dotB = dirB.lengthSqr() > EPSILON ? incoming.dot(dirB) : Double.NEGATIVE_INFINITY;
		Vec3 best = Math.abs(dotA) >= Math.abs(dotB) ? dirA : dirB;
		double bestDot = Math.abs(dotA) >= Math.abs(dotB) ? dotA : dotB;

		if (best.lengthSqr() < EPSILON) {
			best = trackVec;
			bestDot = incoming.dot(trackVec);
		}

		if (bestDot < 0.0) {
			best = best.scale(-1.0);
		}

		return best;
	}

	private static Vec3 normalizeOrZero(Vec3 vec3) {
		return vec3.lengthSqr() < EPSILON ? Vec3.ZERO : vec3.normalize();
	}

	private static int[][] buildSearchOffsets() {
		int[] range = {-1, 0, 1};
		int[][] offsets = new int[27][3];
		int idx = 0;
		for (int dx : range) {
			for (int dy : range) {
				for (int dz : range) {
					offsets[idx][0] = dx;
					offsets[idx][1] = dy;
					offsets[idx][2] = dz;
					idx++;
				}
			}
		}
		return offsets;
	}
}
