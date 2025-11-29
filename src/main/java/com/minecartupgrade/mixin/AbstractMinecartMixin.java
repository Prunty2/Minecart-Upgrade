package com.minecartupgrade.mixin;

import com.minecartupgrade.FurnacePushTracker;
import com.minecartupgrade.MinecartRerailHelper;
import com.minecartupgrade.MinecartSpeedHelper;
import com.minecartupgrade.RailDirectionTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin implements FurnacePushTracker, RailDirectionTracker {
	@Unique
	private static final int MINECARTUPGRADE$FURNACE_BOOST_DURATION = 20;
	@Unique
	private int minecartupgrade$furnaceBoostTicks;
	@Unique
	private Vec3 minecartupgrade$lastRailDirection = Vec3.ZERO;

	@Inject(method = "tick", at = @At("HEAD"))
	private void minecartupgrade$decayFurnaceBoost(CallbackInfo ci) {
		if (this.minecartupgrade$furnaceBoostTicks > 0) {
			this.minecartupgrade$furnaceBoostTicks--;
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void minecartupgrade$lockDirection(CallbackInfo ci) {
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		Vec3 velocity = self.getDeltaMovement();
		double speed = velocity.length();
		if (self.isOnRails()) {
			if (speed > 1.0E-4) {
				this.minecartupgrade$lastRailDirection = velocity.normalize();
			} else if (this.minecartupgrade$lastRailDirection.lengthSqr() > 0.0 && speed > 0.0) {
				self.setDeltaMovement(this.minecartupgrade$lastRailDirection.scale(speed));
			}
		}

		if (self.isOnRails() && speed > 1.0E-4 && this.minecartupgrade$lastRailDirection.lengthSqr() > 0.0) {
			if (this.minecartupgrade$lastRailDirection.dot(velocity) < -1.0E-4) {
				self.setDeltaMovement(this.minecartupgrade$lastRailDirection.scale(speed));
			}
		}
	}

	@Inject(method = "push", at = @At("HEAD"))
	private void minecartupgrade$recordFurnacePushOnCollision(Entity entity, CallbackInfo ci) {
		if (!(entity instanceof AbstractMinecart other)) {
			return;
		}
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		if (other instanceof MinecartFurnace && !(self instanceof MinecartFurnace) && this.minecartupgrade$isPoweredFurnace(other)) {
			this.minecartupgrade$markPushedByFurnace();
		} else if (self instanceof MinecartFurnace && !(other instanceof MinecartFurnace) && this.minecartupgrade$isPoweredFurnace(self)) {
			if (other instanceof FurnacePushTracker tracker) {
				tracker.minecartupgrade$markPushedByFurnace();
			}
		}
	}

	@Unique
	private boolean minecartupgrade$isPoweredFurnace(AbstractMinecart cart) {
		if (cart instanceof MinecartFurnace furnace) {
			Vec3 delta = furnace.getDeltaMovement();
			double magnitude = delta.x * delta.x + delta.z * delta.z;
			return magnitude > 1.0E-7;
		}
		return false;
	}

	@Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartupgrade$raiseMaxSpeed(CallbackInfoReturnable<Double> cir) {
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		boolean boostedByFurnace = self instanceof FurnacePushTracker tracker && tracker.minecartupgrade$isPushedByFurnace();
		cir.setReturnValue(MinecartSpeedHelper.computeMaxSpeed(self, boostedByFurnace));
	}

	@Override
	public void minecartupgrade$markPushedByFurnace() {
		this.minecartupgrade$furnaceBoostTicks = MINECARTUPGRADE$FURNACE_BOOST_DURATION;
	}

	@Override
	public boolean minecartupgrade$isPushedByFurnace() {
		return this.minecartupgrade$furnaceBoostTicks > 0;
	}

	@Override
	public Vec3 minecartupgrade$getLastRailDirection() {
		return this.minecartupgrade$lastRailDirection;
	}

	@Override
	public void minecartupgrade$setLastRailDirection(Vec3 direction) {
		this.minecartupgrade$lastRailDirection = direction;
	}

	@Inject(method = "comeOffTrack", at = @At("HEAD"), cancellable = true)
	private void minecartupgrade$keepOnRails(CallbackInfo ci) {
		if (MinecartRerailHelper.rerailIfSafe((AbstractMinecart)(Object)this)) {
			ci.cancel();
		}
	}
}
