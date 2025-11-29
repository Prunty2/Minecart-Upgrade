package com.minecartupgrade.mixin;

import com.minecartupgrade.FurnacePushTracker;
import com.minecartupgrade.MinecartSpeedHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin {

	@Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartupgrade$raiseMinecartSpeed(ServerLevel serverLevel, CallbackInfoReturnable<Double> cir) {
		AbstractMinecart minecart = ((MinecartBehaviorAccessor) (Object) this).getMinecart();
		boolean boostedByFurnace = minecart instanceof FurnacePushTracker tracker && tracker.minecartupgrade$isPushedByFurnace();
		cir.setReturnValue(MinecartSpeedHelper.computeMaxSpeed(minecart, boostedByFurnace));
	}

	@Inject(method = "calculateSlopeSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartupgrade$preventSlopeBounce(Vec3 vec3, RailShape railShape, CallbackInfoReturnable<Vec3> cir) {
		Vec3 adjusted = this.minecartupgrade$clampSlope(vec3, railShape);
		cir.setReturnValue(adjusted);
	}

	@org.spongepowered.asm.mixin.Unique
	private Vec3 minecartupgrade$clampSlope(Vec3 vec3, RailShape railShape) {
		double d = Math.max(0.0078125, vec3.horizontalDistance() * 0.02);
		if (((MinecartBehaviorAccessor)(Object)this).getMinecart().isInWater()) {
			d *= 0.2;
		}

		double deltaX = 0.0;
		double deltaZ = 0.0;
		switch (railShape) {
			case ASCENDING_EAST -> deltaX = -d;
			case ASCENDING_WEST -> deltaX = d;
			case ASCENDING_NORTH -> deltaZ = d;
			case ASCENDING_SOUTH -> deltaZ = -d;
			default -> {
			}
		}

		double newX = this.minecartupgrade$preserveSign(vec3.x, vec3.x + deltaX);
		double newZ = this.minecartupgrade$preserveSign(vec3.z, vec3.z + deltaZ);
		return new Vec3(newX, vec3.y, newZ);
	}

	@org.spongepowered.asm.mixin.Unique
	private double minecartupgrade$preserveSign(double original, double candidate) {
		if (Math.abs(original) < 1.0E-5) {
			return candidate;
		}

		if (Math.signum(original) != 0.0 && Math.signum(original) != Math.signum(candidate) && Math.abs(candidate) > 1.0E-5) {
			return Math.copySign(Math.abs(candidate), original);
		}

		return candidate;
	}
}
