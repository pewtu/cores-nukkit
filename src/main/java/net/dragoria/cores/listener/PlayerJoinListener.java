package net.dragoria.cores.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.level.Location;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import net.dragoria.cores.Cores;
import net.dragoria.cores.config.CoresConfig;
import net.dragoria.cores.config.object.MapConfig;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.game.object.GameState;
import net.dragoria.cores.game.object.player.GamePlayer;
import net.dragoria.cores.game.object.spectator.SpectatorManager;
import net.dragoria.cores.item.ItemUtil;
import net.dragoria.cores.util.MessageUtil;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class PlayerJoinListener implements Listener {

    private final GameManager gameManager;
    private final SpectatorManager spectatorManager;
    private final ItemUtil itemUtil;
    private final CoresConfig config;

    @EventHandler
    public void onLogin(PlayerPreLoginEvent event) {
        MapConfig currentMap = gameManager.getCurrentMap();
        if (currentMap == null) {
            return;
        }

        if (this.gameManager.getGameState().equals(GameState.ENDED)) {
            event.setKickMessage(MessageUtil.getMessageWithPrefix("The game has ended!"));
            event.setCancelled(true);
        }

        if (this.gameManager.getGameState().equals(GameState.LOBBY)) {
            int maxPlayers = currentMap.getRequiredPlayers();
            if (Cores.getInstance().getServer().getOnlinePlayers().size() >= maxPlayers) {
                event.setKickMessage(MessageUtil.getMessageWithPrefix("The game is full!"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage("");
        this.itemUtil.resetPlayer(player);

        if (this.gameManager.getGameState().equals(GameState.LOBBY)) {
            //Todo default tp location
        }

        MapConfig currentMap = gameManager.getCurrentMap();
        if (currentMap == null) {
            MessageUtil.sendMessageWithPrefix(player, "§cNo map is currently loaded!");
            return;
        }

        GamePlayer gamePlayer = this.gameManager.getPlayer(player.getUniqueId());
        if (gamePlayer == null) {
            if (this.gameManager.getGameState().equals(GameState.IN_GAME)) {
                this.spectatorManager.spectatorPlayer(player);
                return;
            }

            player.teleport(this.config.getLobbyLocation().toNukkit());

            this.gameManager.registerNewGamePlayer(player);
            if (this.gameManager.getGameState().equals(GameState.IN_GAME)) {
                MessageUtil.broadcastMessageWithPrefix("§e" + player.getDisplayName() + "§7has §ajoined§7 the game");
            }
            MessageUtil.broadcastMessageWithPrefix("§4" + player.getName() + " §7has joined the game");
            return;
        }

        // This happens if the player re-joins the server after a crash or disconnect

        this.itemUtil.giveItems(player, gamePlayer.getTeam());

        player.setDisplayName(gamePlayer.getTeam().getColorCode() + player.getName());
        player.teleport(gamePlayer.getSpawnLocation());
        MessageUtil.broadcastMessageWithPrefix("§e" + player.getDisplayName() + "§7has §ajoined§7 the game");
    }
}
