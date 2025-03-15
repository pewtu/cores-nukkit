package net.dragoria.cores.game.object.team;

import cn.nukkit.utils.TextFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.dragoria.cores.config.object.ConfigLocation;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@RequiredArgsConstructor
public class Team {

    private final String name;
    private final String colorCode;
    private final int armorColor;

    private ConfigLocation spawnLocation;
    private ConfigLocation coreLocation;

    private transient boolean coreEnemyNotification = false;
    private transient int wins = 0;

    @NotNull
    public String getDisplayName() {
        return this.colorCode + this.name;
    }

    public TextFormat getChatColor() {
        return TextFormat.getByChar(this.colorCode.charAt(1));
    }
}
