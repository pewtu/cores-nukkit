package net.dragoria.cores.config.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dragoria.cores.game.object.team.Team;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Setter
@Getter
public class MapConfig {

    private final String name;
    private final String item;

    private ConfigLocation spectatorLocation;

    private final Set<Team> teams = new HashSet<>();

    private final int playersPerTeam = 4;

    public int getRequiredPlayers() {
        return this.playersPerTeam * this.teams.size();
    }

}
