package com.minecartrevamp.mixin;

import com.minecartrevamp.FurnacePushTracker;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin implements FurnacePushTracker {
	@Unique
	private static final int MINECARTREVAMP$FURNACE_BOOST_DURATION = 20;
	@Unique
	private int minecartrevamp$furnaceBoostTicks;

	@Inject(method = "tick", at = @At("HEAD"))
	private void minecartrevamp$decayFurnaceBoost(CallbackInfo ci) {
		if (this.minecartrevamp$furnaceBoostTicks > 0) {
			this.minecartrevamp$furnaceBoostTicks--;
		}
	}

	@Inject(method = "pushOtherMinecart", at = @At("HEAD"))
	private void minecartrevamp$recordFurnacePush(AbstractMinecart other, double d, double e, CallbackInfo ci) {
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		if (other.isFurnace() && !self.isFurnace() && this.minecartrevamp$isPoweredFurnace(other)) {
			this.minecartrevamp$markPushedByFurnace();
		} else if (self.isFurnace() && !other.isFurnace() && this.minecartrevamp$isPoweredFurnace(self)) {
			((FurnacePushTracker)other).minecartrevamp$markPushedByFurnace();
		}
	}

	@Unique
	private boolean minecartrevamp$isPoweredFurnace(AbstractMinecart cart) {
		return cart instanceof MinecartFurnace furnace && furnace.push.lengthSqr() > 1.0E-7;
	}

	@Override
	public void minecartrevamp$markPushedByFurnace() {
		this.minecartrevamp$furnaceBoostTicks = MINECARTREVAMP$FURNACE_BOOST_DURATION;
	}

	@Override
	public boolean minecartrevamp$isPushedByFurnace() {
		return this.minecartrevamp$furnaceBoostTicks > 0;
	}
}
