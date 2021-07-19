package net.snakefangox.worldshell_fork.mixinextras;

import net.snakefangox.worldshell_fork.entity.WorldShellEntity;

public interface WorldShellEntityTracker {
	void addWorldShellEntity(WorldShellEntity entity);

	boolean removeWorldShellEntity(WorldShellEntity entity);
}
