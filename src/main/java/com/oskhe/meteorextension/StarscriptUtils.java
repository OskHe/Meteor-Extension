package com.oskhe.meteorextension;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.MissHitResult;
import meteordevelopment.starscript.value.Value;
import net.minecraft.util.hit.HitResult;

public class StarscriptUtils {

    public static Value getDistance() {
        if (MeteorClient.mc.player == null || MeteorClient.mc.cameraEntity == null)
            return Value.number(0.0d);

        HitResult target = MeteorClient.mc.player.raycast(257, 0, true);

        double dist = target.getPos().distanceTo(MeteorClient.mc.cameraEntity.getPos());

        if (target instanceof MissHitResult || dist >= (256))
            return Value.number(0.0d);

        return Value.number(dist);
    }
}
