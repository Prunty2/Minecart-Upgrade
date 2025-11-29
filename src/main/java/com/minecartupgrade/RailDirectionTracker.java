package com.minecartupgrade;

import net.minecraft.world.phys.Vec3;

public interface RailDirectionTracker {
	Vec3 minecartupgrade$getLastRailDirection();
	void minecartupgrade$setLastRailDirection(Vec3 direction);
}
