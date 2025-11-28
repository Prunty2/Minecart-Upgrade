package com.minecartupgrade.mixin;

import com.minecartupgrade.FurnacePushTracker;
import com.minecartupgrade.MinecartSpeedHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
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
}
