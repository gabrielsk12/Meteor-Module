package com.gabrielsk.ai.legit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Simple line-of-sight and FOV helpers for legit checks. */
public final class Visibility {
    private Visibility() {}

    /** Returns true if there are no blocks between player eye pos and target pos. */
    public static boolean hasLineOfSight(MinecraftClient mc, Vec3d targetPos) {
        if (mc.player == null || mc.world == null) return false;
        Vec3d eye = mc.player.getCameraPosVec(1.0f);
        HitResult hit = mc.world.raycast(new RaycastContext(
            eye,
            targetPos,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            mc.player
        ));
        return hit == null || hit.getType() == HitResult.Type.MISS;
    }

    /** Returns true if the target entity is within a given FOV in degrees. */
    public static boolean withinFov(MinecraftClient mc, Entity target, double fovDeg) {
        if (mc.player == null) return false;
        Vec3d look = mc.player.getRotationVec(1.0f).normalize();
        Vec3d dir = target.getPos().subtract(mc.player.getCameraPosVec(1.0f)).normalize();
        double dot = look.dotProduct(dir);
        dot = Math.max(-1.0, Math.min(1.0, dot));
        double angle = Math.toDegrees(Math.acos(dot));
        return angle <= Math.max(1.0, fovDeg);
    }
}
