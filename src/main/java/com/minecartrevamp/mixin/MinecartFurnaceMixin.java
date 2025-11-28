package com.minecartrevamp.mixin;

import com.minecartrevamp.MinecartSpeedHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
public abstract class MinecartFurnaceMixin {
	@Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartrevamp$raiseFurnaceMaxSpeed(ServerLevel serverLevel, CallbackInfoReturnable<Double> cir) {
		cir.setReturnValue(MinecartSpeedHelper.computeFurnaceSelfSpeed((MinecartFurnace)(Object)this));
	}
}
