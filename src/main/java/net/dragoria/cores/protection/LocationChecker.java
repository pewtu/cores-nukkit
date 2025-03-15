package net.dragoria.cores.protection;

import cn.nukkit.level.Location;
import org.jetbrains.annotations.NotNull;

public class LocationChecker {

    public static boolean isWithinRadius(@NotNull Location loc1, @NotNull Location loc2, int radius) {
        if (!loc1.getLevel().getName().equals(loc2.getLevel().getName())) {
            return false;
        }

        double dx = loc1.getX()  - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        return horizontalDistance <= radius;
    }
}
