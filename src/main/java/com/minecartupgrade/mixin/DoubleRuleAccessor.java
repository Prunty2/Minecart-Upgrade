package com.minecartupgrade.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin to invoke the protected changed() method on GameRules.Value
 * for compatibility with older Fabric API versions that don't have direct set methods.
 */
@Mixin(GameRules.Value.class)
public interface DoubleRuleAccessor {
	@Invoker("onChanged")
	void minecartupgrade$invokeOnChanged(MinecraftServer server);
}
