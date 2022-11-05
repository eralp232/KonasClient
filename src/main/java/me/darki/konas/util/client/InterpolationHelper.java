package me.darki.konas.util.client;

import net.minecraft.util.math.MathHelper;

public class InterpolationHelper {

    public static float finterpTo(float oldValue, float newValue, float ticks, float speed) {

        if (speed <= 0f) {
            return newValue;
        }

        float dist = newValue - oldValue;

        if (Math.pow(dist, 2) < 20) {
            return newValue;
        }

        float deltaMove = dist * MathHelper.clamp(ticks * speed, 0f, 1f);

        return oldValue + deltaMove;

    }

}
