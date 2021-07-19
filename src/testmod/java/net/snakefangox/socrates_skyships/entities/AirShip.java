package net.snakefangox.socrates_skyships.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.snakefangox.socrates_skyships.SRegister;
import net.snakefangox.worldshell_fork.entity.WorldShellEntity;

public class AirShip extends WorldShellEntity {
	public AirShip(EntityType<?> type, World world) {
		super(type, world, SRegister.AIRSHIP_SETTINGS);
	}
}
