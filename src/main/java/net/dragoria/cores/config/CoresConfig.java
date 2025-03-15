package net.dragoria.cores.config;

import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import net.dragoria.cores.config.object.ConfigLocation;
import net.dragoria.cores.config.object.MapConfig;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Singleton
public class CoresConfig {

    private ConfigLocation lobbyLocation;;
    private List<MapConfig> possibleMaps = new ArrayList<>();

}
