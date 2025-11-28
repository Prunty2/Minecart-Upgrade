package com.minecartupgrade.mixin;

import com.minecartupgrade.MinecartUpgradeGameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.GameRules;

	@Mixin(GameRules.class)
	public abstract class GameRulesMixin {
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void minecartupgrade$registerGamerule(CallbackInfo ci) {
		MinecartUpgradeGameRules.register();
	}
}
