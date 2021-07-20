package net.snakefangox.worldshell.kevlar;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public class PhysDebugDrawer extends btIDebugDraw {

    public Matrix4f modelMatrix;
    public Matrix3f normalMatrix;
    public VertexConsumer consumer;

    @Override
    public void drawLine(Vector3 from, Vector3 to, Vector3 color) {
        Vector3 normal = to.cpy().sub(from).nor();
        consumer.vertex(modelMatrix, from.x, from.y, from.z).color(color.x, color.y, color.z, 1.0f).normal(normalMatrix, normal.x, normal.y, normal.z).next();
        consumer.vertex(modelMatrix, to.x, to.y, to.z).color(color.x, color.y, color.z, 1.0f).normal(normalMatrix, normal.x, normal.y, normal.z).next();
    }

    @Override
    public int getDebugMode() {
        return DebugDrawModes.DBG_DrawWireframe;
    }
}
