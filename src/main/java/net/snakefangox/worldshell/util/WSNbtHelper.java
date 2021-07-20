package net.snakefangox.worldshell.util;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Vec3d;
import net.snakefangox.worldshell.math.Quaternion;

/** A common place for implementations of NBT serialization */
public class WSNbtHelper {

	public static void putVec3d(NbtCompound tag, Vec3d vec3d, String name) {
		NbtList list = new NbtList();
		list.add(NbtDouble.of(vec3d.x));
		list.add(NbtDouble.of(vec3d.y));
		list.add(NbtDouble.of(vec3d.z));
		tag.put(name, list);
	}

	public static Vec3d getVec3d(NbtCompound tag, String name) {
		NbtList list = tag.getList(name, NbtDouble.ZERO.getType());
		return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	public static void putQuaternion(NbtCompound tag, String name, Quaternion rotation) {
		NbtList list = new NbtList();
		list.add(NbtDouble.of(rotation.getX()));
		list.add(NbtDouble.of(rotation.getY()));
		list.add(NbtDouble.of(rotation.getZ()));
		list.add(NbtDouble.of(rotation.getW()));
		tag.put(name, list);
	}

	public static Quaternion getQuaternion(NbtCompound tag, String name) {
		NbtList list = tag.getList(name, NbtElement.DOUBLE_TYPE);
		return new Quaternion(list.getDouble(0), list.getDouble(1), list.getDouble(2), list.getDouble(3));
	}

	public static NbtIntArray blockBoxToNbt(BlockBox bounds) {
		int[] array = new int[6];
		array[0] = bounds.getMinX();
		array[1] = bounds.getMinY();
		array[2] = bounds.getMinZ();
		array[3] = bounds.getMaxX();
		array[4] = bounds.getMaxY();
		array[5] = bounds.getMaxZ();

		return new NbtIntArray(array);
	}

	public static BlockBox blockBoxFromNbt(int[] dims) {
		return new BlockBox(dims[0], dims[1], dims[2], dims[3], dims[4], dims[5]);
	}
}
