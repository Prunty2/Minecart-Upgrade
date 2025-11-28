package com.minecartupgrade.mixin;

import com.minecartupgrade.FurnacePushTracker;
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
	private static final int MINECARTUPGRADE$FURNACE_BOOST_DURATION = 20;
	@Unique
	private int minecartupgrade$furnaceBoostTicks;

	@Inject(method = "tick", at = @At("HEAD"))
	private void minecartupgrade$decayFurnaceBoost(CallbackInfo ci) {
		if (this.minecartupgrade$furnaceBoostTicks > 0) {
			this.minecartupgrade$furnaceBoostTicks--;
		}
	}

	@Inject(method = "pushOtherMinecart", at = @At("HEAD"))
	private void minecartupgrade$recordFurnacePush(AbstractMinecart other, double d, double e, CallbackInfo ci) {
		AbstractMinecart self = (AbstractMinecart)(Object)this;
		if (other.isFurnace() && !self.isFurnace() && this.minecartupgrade$isPoweredFurnace(other)) {
			this.minecartupgrade$markPushedByFurnace();
		} else if (self.isFurnace() && !other.isFurnace() && this.minecartupgrade$isPoweredFurnace(self)) {
			((FurnacePushTracker)other).minecartupgrade$markPushedByFurnace();
		}
	}

	@Unique
	private boolean minecartupgrade$isPoweredFurnace(AbstractMinecart cart) {
		return cart instanceof MinecartFurnace furnace && furnace.push.lengthSqr() > 1.0E-7;
	}

	@Override
	public void minecartupgrade$markPushedByFurnace() {
		this.minecartupgrade$furnaceBoostTicks = MINECARTUPGRADE$FURNACE_BOOST_DURATION;
	}

	@Override
	public boolean minecartupgrade$isPushedByFurnace() {
		return this.minecartupgrade$furnaceBoostTicks > 0;
	}
}
