package net.dragoria.cores.protection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import cn.nukkit.level.Location;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class SpawnProtectionManager {

    @Getter
    private final Set<Location> spawnLocations = new HashSet<>();

    @Getter
    private final Set<Location> coreLocations = new HashSet<>();

    private final int spawnProtectionRadius;
    private final int coreProtectionRadius;

    public void addSpawnLocation(@NotNull Location location) {
        this.spawnLocations.add(location);
    }

    public void addCoreLocation(@NotNull Location location) {
        this.coreLocations.add(location);
    }

    public boolean isWithinSpawnProtection(@NotNull Location location) {
        return isWithinProtection(location, spawnLocations, spawnProtectionRadius);
    }

    public boolean isWithinCoreProtection(@NotNull Location location) {
        for (Location coreLocation : coreLocations) {
            if (LocationChecker.isWithinRadius(location, coreLocation, coreProtectionRadius)) {
                double yDifference = location.getY() - coreLocation.getY();
                if (yDifference == 0 || yDifference == 1) {
                    return true;
                }

                if (location.getY() <= coreLocation.getY()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWithinProtection(@NotNull Location location, Set<Location> locations, int radius) {
        for (Location protectedLocation : locations) {
            if (LocationChecker.isWithinRadius(location, protectedLocation, radius)) {
                return true;
            }
        }
        return false;
    }
}
