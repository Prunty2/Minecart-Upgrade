package com.minecartupgrade.mixin;

import java.util.Map;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRules.class)
public interface GameRulesInstanceAccessor {
	@Accessor("rules")
	Map<GameRules.Key<?>, GameRules.Value<?>> minecartupgrade$getRules();
}
