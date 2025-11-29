package com.minecartupgrade.mixin;

import com.minecartupgrade.FurnacePushTracker;
import com.minecartupgrade.MinecartSpeedHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin {
	@org.spongepowered.asm.mixin.Unique
	private Vec3 minecartupgrade$preSlopeMotion = Vec3.ZERO;

	@Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartupgrade$raiseMinecartSpeed(ServerLevel serverLevel, CallbackInfoReturnable<Double> cir) {
		AbstractMinecart minecart = ((MinecartBehaviorAccessor) (Object) this).getMinecart();
		boolean boostedByFurnace = minecart instanceof FurnacePushTracker tracker && tracker.minecartupgrade$isPushedByFurnace();
		cir.setReturnValue(MinecartSpeedHelper.computeMaxSpeed(minecart, boostedByFurnace));
	}

	@Inject(method = "moveAlongTrack", at = @At("HEAD"))
	private void minecartupgrade$rememberMotion(ServerLevel serverLevel, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		this.minecartupgrade$preSlopeMotion = ((MinecartBehaviorAccessor)(Object)this).getMinecart().getDeltaMovement();
	}

	@Inject(
		method = "moveAlongTrack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/vehicle/OldMinecartBehavior;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;",
			ordinal = 1
		)
	)
	private void minecartupgrade$preventSlopeReversal(ServerLevel serverLevel, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		AbstractMinecart minecart = ((MinecartBehaviorAccessor)(Object)this).getMinecart();
		Vec3 current = minecart.getDeltaMovement();
		double newX = this.minecartupgrade$preserveSign(this.minecartupgrade$preSlopeMotion.x, current.x);
		double newZ = this.minecartupgrade$preserveSign(this.minecartupgrade$preSlopeMotion.z, current.z);
		minecart.setDeltaMovement(newX, current.y, newZ);
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
