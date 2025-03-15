package net.dragoria.cores.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.level.WeatherChangeEvent;
import cn.nukkit.event.player.PlayerDropItemEvent;
import cn.nukkit.event.player.PlayerFoodLevelChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.game.object.GameState;
import net.dragoria.cores.game.object.spectator.SpectatorManager;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class ProtectionListener implements Listener {

    private final GameManager gameManager;
    private final SpectatorManager spectatorManager;

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            event.setCancelled(true);
        }
        if (this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            if (this.spectatorManager.getSpectators().contains(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            event.setCancelled(true);
        }
        if (this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            if (this.spectatorManager.getSpectators().contains(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            event.setCancelled(true);
        }
        if (this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            if (this.spectatorManager.getSpectators().contains(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevel(PlayerFoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            if (this.spectatorManager.getSpectators().contains(player)) {
                event.setCancelled(true);
            }
        }
    }
}
