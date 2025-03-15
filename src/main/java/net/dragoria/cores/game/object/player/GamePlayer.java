package net.dragoria.cores.game.object.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dragoria.cores.game.object.team.Team;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class GamePlayer {

    private final UUID uniqueId;

    private Team team;
    private int kills = 0;

    @Nullable
    public Location getSpawnLocation() {
        return (this.team != null) ? this.team.getSpawnLocation().toNukkit() : null;
    }

    @Nullable
    public Player asNukkitPlayer() {
        return Server.getInstance().getPlayer(this.uniqueId).orElse(null);
    }
}
