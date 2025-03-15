package net.dragoria.cores.config.object;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ConfigLocation {

    private final String worldName;

    private final double x;
    private final double y;
    private final double z;

    private final float yaw;
    private final float pitch;

    @NotNull
    public Location toNukkit() {
        Level level = Server.getInstance().getLevelByName(this.worldName);
        if (level == null) {
            throw new IllegalStateException("World " + this.worldName + " not loaded1");
        }
        return new Location(this.x, this.y, this.z, this.yaw, this.pitch, level);
    }
}
