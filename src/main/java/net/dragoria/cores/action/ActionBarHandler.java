package net.dragoria.cores.action;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.Task;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.game.object.GameState;
import net.dragoria.cores.game.object.player.GamePlayer;
import net.dragoria.cores.game.object.team.Team;
import org.jetbrains.annotations.NotNull;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class ActionBarHandler {

    private final PluginBase plugin;
    private final GameManager gameManager;

    public void start() {
        Server.getInstance().getScheduler().scheduleRepeatingTask(this.plugin, new Task() {
            @Override
            public void onRun(int currentTick) {
                if (gameManager.getGameState().equals(GameState.ENDED)) {
                    return;
                }

                if (gameManager.getGameState().equals(GameState.LOBBY)) {
                    if (gameManager.getStartCountdown() == null) {
                        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                            sendActionText(player, "§aWaiting for more players...");
                        }
                    }
                    return;
                }

                for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                    GamePlayer gamePlayer = gameManager.getPlayer(player.getUniqueId());

                    if (gamePlayer == null) {
                        sendActionText(player, "§7Spectator");
                        continue;
                    }

                    Team team = gamePlayer.getTeam();

                    sendActionText(player, team.getDisplayName());
                }
            }
        }, 20);
    }

    public void sendActionText(@NotNull Player player, String message) {
        player.sendActionBar(message);
    }
}
