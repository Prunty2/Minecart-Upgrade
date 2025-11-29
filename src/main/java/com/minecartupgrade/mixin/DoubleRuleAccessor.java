package com.minecartupgrade.mixin;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoubleRule.class)
public interface DoubleRuleAccessor {
	@Accessor("value")
	void minecartupgrade$setValue(double value);

	@org.spongepowered.asm.mixin.gen.Invoker("copy")
	DoubleRule minecartupgrade$copy();
}
