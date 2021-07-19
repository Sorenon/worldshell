package net.snakefangox.socrates_skyships;

import net.minecraft.entity.EntityType;
import net.snakefangox.rapidregister.annotations.BlockMeta;
import net.snakefangox.rapidregister.annotations.Exclude;
import net.snakefangox.rapidregister.annotations.RegisterContents;
import net.snakefangox.socrates_skyships.blocks.ShipsHelm;
import net.snakefangox.socrates_skyships.entities.AirShip;
import net.snakefangox.socrates_skyships.entities.GhastAirShip;
import net.snakefangox.worldshell_fork.entity.WorldShellEntityType;
import net.snakefangox.worldshell_fork.entity.WorldShellSettings;
import net.snakefangox.worldshell_fork.transfer.ConflictSolver;

@RegisterContents(defaultBlockMeta = @BlockMeta(blockItemGroup = "transportation"))
public class SRegister {
	public static final ShipsHelm SHIPS_HELM = new ShipsHelm();
	@Exclude
	public static final WorldShellSettings AIRSHIP_SETTINGS = new WorldShellSettings.Builder(true, true).setConflictSolver(ConflictSolver.HARDNESS).build();
	public static final EntityType<AirShip> AIRSHIP_TYPE = new WorldShellEntityType<>(AirShip::new);


	public static final EntityType<GhastAirShip> AIRSHIP_TYPE2 = new WorldShellEntityType<>(GhastAirShip::new);
}
