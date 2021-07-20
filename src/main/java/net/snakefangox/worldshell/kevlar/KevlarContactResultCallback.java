package net.snakefangox.worldshell.kevlar;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;

import java.util.HashSet;

public class KevlarContactResultCallback extends ContactResultCallback {

    public btCollisionObject obj = null;

    public HashSet<ShapeRecord> done = new HashSet<>();

    public Callback callback;

    @Override
    public boolean needsCollision(btBroadphaseProxy proxy0) {
        return true;
    }

    @Override
    public float addSingleResult(btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0, btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
        if (colObj0Wrap.getCollisionObject() == obj) {
            if (!done.add(new ShapeRecord(colObj1Wrap, index1))) {
                return 1;
            }
        } else {
            if (!done.add(new ShapeRecord(colObj0Wrap, index0))) {
                return 1;
            }
        }

        Vector3 normalFromOther = new Vector3();
        cp.getNormalWorldOnB(normalFromOther);
        float penetration = -cp.getDistance();

        if (penetration <= 0) {
            return 1;
        }

        if (colObj1Wrap.getCollisionObject() == obj) {
            normalFromOther = new Vector3().sub(normalFromOther);
        }

        callback.apply(normalFromOther, penetration);

        return 1;
    }

    private record ShapeRecord(btCollisionObjectWrapper wrapper, Integer index) {
    }

    @FunctionalInterface
    public interface Callback {
        void apply(Vector3 normalFromOther, float penetration);
    }
}
