package com.minecartrevamp.mixin;

import com.minecartrevamp.FurnacePushTracker;
import com.minecartrevamp.MinecartSpeedHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin {

	@Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartrevamp$raiseMinecartSpeed(ServerLevel serverLevel, CallbackInfoReturnable<Double> cir) {
		AbstractMinecart minecart = ((MinecartBehaviorAccessor) (Object) this).getMinecart();
		boolean boostedByFurnace = minecart instanceof FurnacePushTracker tracker && tracker.minecartrevamp$isPushedByFurnace();
		cir.setReturnValue(MinecartSpeedHelper.computeMaxSpeed(minecart, boostedByFurnace));
	}
}
