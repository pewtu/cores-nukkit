package net.dragoria.cores.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import net.dragoria.cores.game.GameManager;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class PlayerQuitListener implements Listener {

    private final GameManager gameManager;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
        this.gameManager.handlePlayerQuit(event.getPlayer());
    }
}
