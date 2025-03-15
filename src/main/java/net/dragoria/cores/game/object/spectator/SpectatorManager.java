package net.dragoria.cores.game.object.spectator;

import cn.nukkit.Player;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class SpectatorManager {
    @Getter
    private final List<Player> spectators = new ArrayList<>();
    private final GameManager gameManager;

    public void spectatorPlayer(Player player) {

        //Todo spectator effects

        if (!spectators.contains(player)) {
            spectators.add(player);
            player.setGamemode(Player.SPECTATOR);
            MessageUtil.sendMessageWithPrefix(player, "You are now a spectator");
        }


    }

}
