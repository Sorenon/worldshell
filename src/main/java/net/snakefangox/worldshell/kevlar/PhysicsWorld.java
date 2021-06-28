package net.snakefangox.worldshell.kevlar;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Disposable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Box;

import java.util.HashMap;

public class PhysicsWorld implements Disposable {

    public btCollisionConfiguration collisionConfiguration;
    public btCollisionDispatcher collisionDispatcher;
    public btBroadphaseInterface broadphase;
    public btConstraintSolver constraintSolver;
    public btDiscreteDynamicsWorld dynamicsWorld;

    public PhysDebugDrawer debugDrawer;

    public btCollisionShape DEBUG_SPHERE_SHAPE;
    public btRigidBody DEBUG_SPHERE;
    public btRigidBody DEBUG_BLOCK;

    public HashMap<BlockState, btCollisionShape> blockShapes = new HashMap<>();
    public HashMap<Vector3, btBoxShape> boxShapes = new HashMap<>();

    public PhysicsWorld() {
        collisionConfiguration = new btDefaultCollisionConfiguration();
        collisionDispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        constraintSolver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(collisionDispatcher, broadphase, constraintSolver, collisionConfiguration);

        debugDrawer = new PhysDebugDrawer();
        dynamicsWorld.setDebugDrawer(debugDrawer);

        {
            DEBUG_SPHERE_SHAPE = new btSphereShape(0.5f);
            float mass = 0;
            Vector3 intertia = new Vector3();
            DEBUG_SPHERE_SHAPE.calculateLocalInertia(mass, intertia);
            DEBUG_SPHERE = new btRigidBody(mass, new btDefaultMotionState(), DEBUG_SPHERE_SHAPE, intertia);
            DEBUG_SPHERE.translate(new Vector3(-25, 85, -176));
            dynamicsWorld.addRigidBody(DEBUG_SPHERE);
        }

        {
            var shape = getOrMakeBlockShape(Blocks.OAK_STAIRS.getDefaultState());
            float mass = 0;
            Vector3 intertia = new Vector3();
            shape.calculateLocalInertia(mass, intertia);
            DEBUG_BLOCK = new btRigidBody(mass, new btDefaultMotionState(), shape, intertia);

            Matrix4 transform = new Matrix4();
            transform.setToTranslation(new Vector3(-22, 83, -176));
            transform.rotate(new Vector3(0, 1, 0), 40);
            DEBUG_BLOCK.setWorldTransform(transform);
            dynamicsWorld.addRigidBody(DEBUG_BLOCK);
        }
    }

    @Override
    public void dispose() {
        dynamicsWorld.dispose();
        constraintSolver.dispose();
        broadphase.dispose();
        collisionDispatcher.dispose();
        collisionConfiguration.dispose();

        for (var shape : blockShapes.values()) {
            if (!(shape instanceof btBoxShape)) {
                shape.dispose();
            }
        }
        for (var shape : boxShapes.values()) {
            shape.dispose();
        }
    }

    public btBoxShape getOrMakeBoxShape(Box box) {
        return getOrMakeBoxShape(
                new Vector3(
                        (float) (box.getXLength() / 2),
                        (float) (box.getYLength() / 2),
                        (float) (box.getZLength() / 2)
                )
        );
    }

    public btBoxShape getOrMakeBoxShape(Vector3 boxHalfExtents) {
        return boxShapes.computeIfAbsent(boxHalfExtents, btBoxShape::new);
    }

    public btCollisionShape getOrMakeBlockShape(BlockState blockState) {
        return blockShapes.computeIfAbsent(blockState, _bs -> {
            var voxelShape = blockState.getCollisionShape(null, null);
            var boxes = voxelShape.getBoundingBoxes();
            if (boxes.size() == 1) {
                return getOrMakeBoxShape(boxes.get(0));
            } else {
                var shape = new btCompoundShape();
                var matrix4 = new Matrix4();
                for (var box : boxes) {
                    var boxShape = getOrMakeBoxShape(box);
                    var center = box.getCenter();
                    matrix4.set(
                            (float) center.x, (float) center.y, (float) center.z,
                            0f, 0f, 0f, 1f
                    );
                    shape.addChildShape(matrix4, boxShape);
                }
                return shape;
            }
        });
    }
}
