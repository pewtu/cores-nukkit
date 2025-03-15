package net.dragoria.cores.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.level.Sound;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.game.object.GameState;
import net.dragoria.cores.game.object.player.GamePlayer;
import net.dragoria.cores.game.object.spectator.SpectatorManager;
import net.dragoria.cores.game.object.team.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class ChatListener implements Listener {

    private final GameManager gameManager;
    private final SpectatorManager spectatorManager;

    private final Set<String> rewardedPlayers = new HashSet<>();

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event ) {
        Player player = event.getPlayer();
        if( this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            event.setCancelled(true);
            if(this.spectatorManager.getSpectators().contains(player)) {
                spectatorChat(player, event.getMessage());
                return;
            }
            if(event.getMessage().toLowerCase().startsWith( "@all ")) {
                sendGlobalMessage(player, event.getMessage().split("@all ")[1]);
            } else if(event.getMessage().toLowerCase().startsWith("@all" ) ) {
                sendGlobalMessage(player, event.getMessage().split("@all")[1]);
            } else if(event.getMessage().toLowerCase().startsWith( "@a " ) ) {
                sendGlobalMessage(player, event.getMessage().split( "@a ")[1]);
            } else if(event.getMessage().toLowerCase().startsWith( "@a" ) ) {
                sendGlobalMessage(player, event.getMessage().split( "@a")[1]);
            } else {
                sendTeamMessage(player, event.getMessage());
            }
        }
        if (this.gameManager.getGameState() == GameState.ENDED) {
            String message = event.getMessage().trim().toLowerCase();

            if (message.equals("gg")) {
                if (!rewardedPlayers.contains(player.getName())) {
                    //Todo Spieler müssen die Coins auch erhalten
                    player.getLevel().addSound(player.getLocation(), Sound.RANDOM_LEVELUP, 40, 1);
                    rewardedPlayers.add(player.getName());
                }
            }
        }
    }

    public void spectatorChat(Player player, String message) {
        for(Player spectator : this.spectatorManager.getSpectators()) {
            spectator.sendMessage("§7[§4X§7] " + player.getName() + "§8: " + message);
        }
    }

    public void sendGlobalMessage(@NotNull Player sender, @NotNull String message) {
        GamePlayer gamePlayer = gameManager.getPlayers().stream()
                .filter(player -> player.getUniqueId().equals(sender.getUniqueId()))
                .findFirst()
                .orElse(null);

        if (gamePlayer == null || gamePlayer.getTeam() == null) {
            return;
        }

        Team senderTeam = gamePlayer.getTeam();

        for (Player onlinePlayer : Server.getInstance().getOnlinePlayers().values()) {
            onlinePlayer.sendMessage("§8[§7@all§8] " + senderTeam.getColorCode() + sender.getName() + "§8: §f" + message);
        }
    }
    public void sendTeamMessage(@NotNull Player sender, @NotNull String message) {
        GamePlayer gamePlayer = gameManager.getPlayers().stream()
                .filter(player -> player.getUniqueId().equals(sender.getUniqueId()))
                .findFirst()
                .orElse(null);

        if (gamePlayer == null || gamePlayer.getTeam() == null) {
            return;
        }

        Team senderTeam = gamePlayer.getTeam();

        for (GamePlayer teamPlayer : gameManager.getPlayers().stream()
                .filter(player -> player.getTeam() != null && player.getTeam().equals(senderTeam))
                .collect(Collectors.toList())) {
            Player onlinePlayer = Server.getInstance().getPlayer(teamPlayer.getUniqueId()).orElse(null);
            if (onlinePlayer != null) {
                onlinePlayer.sendMessage(senderTeam.getColorCode() + sender.getName() + "§8: §f" + message);
            }
        }
    }
}
