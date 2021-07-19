package net.snakefangox.worldshell_fork;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell_fork.collision.EntityBounds;
import net.snakefangox.worldshell_fork.entity.WorldShellEntity;
import net.snakefangox.worldshell_fork.math.Quaternion;
import net.snakefangox.worldshell_fork.storage.Bay;
import net.snakefangox.worldshell_fork.storage.Microcosm;
import net.snakefangox.worldshell_fork.storage.ShellAwareBlock;
import net.snakefangox.worldshell_fork.util.WorldShellPacketHelper;

import java.util.*;

// TODO This is a mess and is next on my kill list
public class WSNetworking {

	public static final Identifier SHELL_DATA = new Identifier(WorldShellMain.MODID, "data");
	public static final Identifier SHELL_UPDATE = new Identifier(WorldShellMain.MODID, "update");
	public static final Identifier SHELL_INTERACT = new Identifier(WorldShellMain.MODID, "interact");
	public static final Identifier SHELL_BLOCK_EVENT = new Identifier(WorldShellMain.MODID, "block_event");
	public static final TrackedDataHandler<EntityBounds> BOUNDS = new TrackedDataHandler<EntityBounds>() {

		@Override
		public void write(PacketByteBuf data, EntityBounds object) {
			data.writeFloat(object.length);
			data.writeFloat(object.width);
			data.writeFloat(object.height);
			data.writeBoolean(object.fixed);
		}

		@Override
		public EntityBounds read(PacketByteBuf buf) {
			float l = buf.readFloat();
			float w = buf.readFloat();
			float h = buf.readFloat();
			boolean f = buf.readBoolean();
			return new EntityBounds(l, w, h, f);
		}

		@Override
		public EntityBounds copy(EntityBounds object) {
			return object;
		}
	};
	public static final TrackedDataHandler<Vec3d> VEC3D = new TrackedDataHandler<Vec3d>() {

		@Override
		public void write(PacketByteBuf data, Vec3d object) {
			data.writeDouble(object.x);
			data.writeDouble(object.y);
			data.writeDouble(object.z);
		}

		@Override
		public Vec3d read(PacketByteBuf buf) {
			double x = buf.readDouble();
			double y = buf.readDouble();
			double z = buf.readDouble();
			return new Vec3d(x, y, z);
		}

		@Override
		public Vec3d copy(Vec3d object) {
			return object;
		}
	};

	public static final TrackedDataHandler<Quaternion> QUATERNION = new TrackedDataHandler<>() {

		@Override
		public void write(PacketByteBuf data, Quaternion object) {
			data.writeDouble(object.getX());
			data.writeDouble(object.getY());
			data.writeDouble(object.getZ());
			data.writeDouble(object.getW());
		}

		@Override
		public Quaternion read(PacketByteBuf buf) {
			double x = buf.readDouble();
			double y = buf.readDouble();
			double z = buf.readDouble();
			double w = buf.readDouble();
			return new Quaternion(x, y, z, w);
		}

		@Override
		public Quaternion copy(Quaternion object) {
			return object.clone();
		}
	};

	static {
		TrackedDataHandlerRegistry.register(BOUNDS);
		TrackedDataHandlerRegistry.register(VEC3D);
		TrackedDataHandlerRegistry.register(QUATERNION);
	}

	public static void registerClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(SHELL_DATA, WSNetworking::handleShellData);
		ClientPlayNetworking.registerGlobalReceiver(SHELL_UPDATE, WSNetworking::handleShellUpdate);
		ClientPlayNetworking.registerGlobalReceiver(SHELL_BLOCK_EVENT, WSNetworking::handleShellBlockEvent);
	}

	private static void handleShellData(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
		int entityID = buf.readInt();
		Map<BlockPos, BlockState> stateMap = new HashMap<>();
		Map<BlockPos, BlockEntity> entityMap = new HashMap<>();
		List<Microcosm.ShellTickInvoker> tickers = new ArrayList<>();
		WorldShellPacketHelper.readBlocks(buf, stateMap, entityMap, tickers);

		client.execute(() -> {
			Entity entity = client.world.getEntityById(entityID);
			if (entity instanceof WorldShellEntity) {
				((WorldShellEntity) entity).initializeWorldShell(stateMap, entityMap, tickers);
			}
		});
	}

	private static void handleShellUpdate(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
		int entityID = buf.readInt();
		BlockPos pos = BlockPos.fromLong(buf.readLong());
		BlockState state = Block.getStateFromRawId(buf.readInt());
		NbtCompound tag = buf.readNbt();

		client.execute(() -> {
			Entity entity = client.world.getEntityById(entityID);
			if (entity instanceof WorldShellEntity) {
				((WorldShellEntity) entity).updateWorldShell(pos, state, tag);
			}
		});
	}

	private static void handleShellBlockEvent(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
		int entityID = buf.readInt();
		BlockPos pos = buf.readBlockPos();
		int type = buf.readInt();
		int data = buf.readInt();
		client.execute(() -> {
			Entity entity = client.world.getEntityById(entityID);
			if (entity instanceof WorldShellEntity) {
				((WorldShellEntity) entity).getMicrocosm().addBlockEvent(pos, type, data);
			}
		});
	}

	public static void registerServerPackets() {
		ServerPlayNetworking.registerGlobalReceiver(SHELL_INTERACT, WSNetworking::handleShellInteract);
	}

	private static void handleShellInteract(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler networkHandler, PacketByteBuf buf, PacketSender sender) {
		int entityID = buf.readInt();
		BlockHitResult hit = buf.readBlockHitResult();
		Hand hand = buf.readEnumConstant(Hand.class);
		boolean interact = buf.readBoolean();

		server.execute(() -> {
			Entity entity = player.world.getEntityById(entityID);
			if (entity instanceof WorldShellEntity wsEntity) {
				EntityBounds dimensions = wsEntity.getDimensions();
				if (player.distanceTo(entity) < dimensions.getRoughMaxDist() + 4.5) {
					Optional<Bay> bay = wsEntity.getBay();
					if (bay.isPresent()) {
						World world = WorldShellMain.getStorageDim(server);
						BlockPos bp = bay.get().toGlobal(hit.getBlockPos());
						if (!world.isChunkLoaded(bp)) return;
						BlockHitResult gHit = new BlockHitResult(bay.get().toGlobal(hit.getPos()),
								hit.getSide(), bp, hit.isInsideBlock());
						if (interact) {
//							world.getBlockState(gHit.getBlockPos()).onUse(world, player, hand, gHit);

							BlockState blockState = world.getBlockState(gHit.getBlockPos());
							ActionResult result;
							if (blockState.getBlock() instanceof ShellAwareBlock awareBlock) {
								result = awareBlock.onUseWorldshell(wsEntity, blockState, world, gHit.getBlockPos(), player, hand, gHit);
							} else {
								result = blockState.onUse(world, player, hand, gHit);
							}
							if (result.isAccepted() || !player.isCreative()) {
								return;
							}
							World playerWorld = player.world;
							player.world = world;
							player.getStackInHand(hand).useOnBlock(new ItemUsageContext(player, hand, gHit));
							player.world = playerWorld;
						} else {
							if (player.isCreative()) {
//								world.getBlockState(gHit.getBlockPos()).onBlockBreakStart(world, gHit.getBlockPos(), player);
								world.setBlockState(gHit.getBlockPos(), Blocks.AIR.getDefaultState());
							}
						}
					}
				}
			}
		});
	}

}
