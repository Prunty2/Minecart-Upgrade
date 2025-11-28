package com.minecartupgrade.mixin;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartBehavior.class)
public interface MinecartBehaviorAccessor {
	@Accessor("minecart")
	AbstractMinecart getMinecart();
}
