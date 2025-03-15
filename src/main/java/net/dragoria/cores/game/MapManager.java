package net.dragoria.cores.game;

import cn.nukkit.Server;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import net.dragoria.cores.Cores;
import net.dragoria.cores.config.CoresConfig;
import net.dragoria.cores.config.object.MapConfig;
import net.dragoria.cores.game.object.team.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class MapManager {

    private final Cores plugin;

    public void prepareWorldsForSelectedMap(@NotNull CoresConfig config, @NotNull MapConfig currentMap) {
        this.plugin.getLogger().info("Preparing worlds for map " + currentMap.getName());

        Set<String> worldsToLoad = new HashSet<>();
        worldsToLoad.add(config.getLobbyLocation().getWorldName());
        worldsToLoad.add(currentMap.getSpectatorLocation().getWorldName());

        for (Team team : currentMap.getTeams()) {
            worldsToLoad.add(team.getCoreLocation().getWorldName());
            worldsToLoad.add(team.getSpawnLocation().getWorldName());
        }

        this.plugin.getLogger().info("Worlds to prepare: " + String.join(", ", worldsToLoad));
        for (String worldName : worldsToLoad) {
            if (!Server.getInstance().isLevelLoaded(worldName)) {
                this.plugin.getLogger().info("World " + worldName + " not found. Loading or generating...");

                if (!Server.getInstance().loadLevel(worldName)) {
                    this.plugin.getLogger().info("World " + worldName + " does not exist. Generating...");
                    Server.getInstance().generateLevel(worldName);
                }
            }
        }
    }
}
