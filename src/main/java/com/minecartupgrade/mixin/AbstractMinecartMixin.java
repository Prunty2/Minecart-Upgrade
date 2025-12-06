package com.minecartupgrade.mixin;

import com.minecartupgrade.FurnacePushTracker;
import com.minecartupgrade.MinecartRerailHelper;
import com.minecartupgrade.MinecartSpeedHelper;
import com.minecartupgrade.RailDirectionTracker;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.level.block.BaseRailBlock;
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

	@Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartupgrade$raiseMinecartSpeed(CallbackInfoReturnable<Double> cir) {
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		boolean boostedByFurnace = this.minecartupgrade$isPushedByFurnace();
		cir.setReturnValue(MinecartSpeedHelper.computeMaxSpeed(self, boostedByFurnace));
	}

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
		boolean onRails = minecartupgrade$isOnRails(self);
		if (onRails) {
			if (speed > 1.0E-4) {
				this.minecartupgrade$lastRailDirection = velocity.normalize();
			} else if (this.minecartupgrade$lastRailDirection.lengthSqr() > 0.0 && speed > 0.0) {
				self.setDeltaMovement(this.minecartupgrade$lastRailDirection.scale(speed));
			}
		}

		if (onRails && speed > 1.0E-4 && this.minecartupgrade$lastRailDirection.lengthSqr() > 0.0) {
			if (this.minecartupgrade$lastRailDirection.dot(velocity) < -1.0E-4) {
				self.setDeltaMovement(this.minecartupgrade$lastRailDirection.scale(speed));
			}
		}
	}

	@Unique
	private static boolean minecartupgrade$isOnRails(AbstractMinecart minecart) {
		return BaseRailBlock.isRail(minecart.level().getBlockState(minecart.blockPosition()))
			|| BaseRailBlock.isRail(minecart.level().getBlockState(minecart.blockPosition().below()));
	}

	@Inject(method = "push", at = @At("HEAD"))
	private void minecartupgrade$recordFurnacePush(net.minecraft.world.entity.Entity entity, CallbackInfo ci) {
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		if (entity instanceof AbstractMinecart other) {
			if (other instanceof MinecartFurnace furnace && !(self instanceof MinecartFurnace) && this.minecartupgrade$isPoweredFurnace(furnace)) {
				this.minecartupgrade$markPushedByFurnace();
			}
		}
	}

	@Unique
	private boolean minecartupgrade$isPoweredFurnace(AbstractMinecart cart) {
		return cart instanceof MinecartFurnace furnace && furnace.xPush * furnace.xPush + furnace.zPush * furnace.zPush > 1.0E-7;
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

	@Inject(method = "moveAlongTrack", at = @At("RETURN"))
	private void minecartupgrade$rerailIfNeeded(CallbackInfo ci) {
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		if (!self.level().isClientSide() && !minecartupgrade$isOnRails(self)) {
			MinecartRerailHelper.rerailIfSafe(self);
		}
	}
}
