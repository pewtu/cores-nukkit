package net.dragoria.cores.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import cn.nukkit.potion.Effect;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.scheduler.TaskHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.dragoria.cores.Cores;
import net.dragoria.cores.config.object.MapConfig;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.game.object.GameState;
import net.dragoria.cores.game.object.player.GamePlayer;
import net.dragoria.cores.game.object.team.Team;
import net.dragoria.cores.util.MessageUtil;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static cn.nukkit.Player.SPECTATOR;

@Singleton
public class CoreBreakListener implements Listener {

    private final GameManager gameManager;

    private final List<UUID> alreadyNotified = new CopyOnWriteArrayList<>();

    private final TaskHandler taskHandler;

    @Inject
    public CoreBreakListener(Cores plugin, GameManager gameManager) {
        this.gameManager = gameManager;

        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new NukkitRunnable() {
            @Override
            public void run() {
                if (!gameManager.getGameState().equals(GameState.IN_GAME)) {
                    return;
                }

                MapConfig currentMap = gameManager.getCurrentMap();
                if (currentMap == null) {
                    return;
                }

                for (Team team : currentMap.getTeams()) {
                    int amount = 0;
                    for (GamePlayer player : gameManager.getPlayers()) {
                        if (player.getTeam().equals(team)) {
                            continue;
                        }

                        Player nukkitPlayer = player.asNukkitPlayer();
                        if (nukkitPlayer == null || (nukkitPlayer.getGamemode() == SPECTATOR)) {
                            continue;
                        }

                        Location coreLocation = team.getCoreLocation().toNukkit();
                        double distance = nukkitPlayer.getLocation().distance(coreLocation);
                        if (distance <= 6.0) {
                            amount++;
                            nukkitPlayer.addEffect(Effect.getEffect(Effect.MINING_FATIGUE)
                                    .setDuration(5 * 20)
                                    .setAmplifier(0)
                                    .setVisible(false));

                            team.setCoreEnemyNotification(true);

                            gameManager.getPlayers()
                                    .stream()
                                    .filter(gamePlayer -> gamePlayer.getTeam().equals(team))
                                    .forEach(otherPlayer -> {
                                        Player nukkitPlayerOther = otherPlayer.asNukkitPlayer();
                                        if (nukkitPlayerOther != null) {
                                            if (!alreadyNotified.contains(otherPlayer.getUniqueId())) {
                                                alreadyNotified.add(otherPlayer.getUniqueId());
                                            }
                                        }
                                    });

                        }
                    }

                    if (amount == 0) {
                        team.setCoreEnemyNotification(false);

                        gameManager.getPlayers()
                                .stream()
                                .filter(gamePlayer -> gamePlayer.getTeam().equals(team))
                                .forEach(gamePlayer -> {
                                    alreadyNotified.remove(gamePlayer.getUniqueId());
                                });
                    }
                }
            }
        }, 20, true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            return;
        }

        Player player = event.getPlayer();

        GamePlayer gamePlayer = this.gameManager.getPlayer(player.getUniqueId());
        if (gamePlayer == null) {
            return;
        }

        Team team = this.gameManager.getTeamByCore(event.getBlock().getLocation());
        if (team == null) {
            return;
        }

        event.setCancelled(true); // always required otherwise the block is dropped also in the following maps

        if (gamePlayer.getTeam().equals(team)) {
            MessageUtil.sendMessageWithPrefix(player, "§7You §ccannot §7destroy your own core.");
            return;
        }

        MessageUtil.broadcastMessageWithPrefix("§7The core of team " + team.getDisplayName() + " §7has been §4destroyed§7.");

        for (Player player1 : Cores.getInstance().getServer().getOnlinePlayers().values()) {
            player1.sendTitle("§7Core of " + team.getDisplayName(), "§7has been §4destroyed");
        }

        //Todo Stats abgebauten Core

        Team differentTeam = this.gameManager.getCurrentMap().getTeams().stream().filter(other -> !other.equals(team)).findFirst().orElse(null);

        if (differentTeam == null) {
            throw new IllegalStateException("No different team found.");
        }

        this.gameManager.stopGame(differentTeam);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getBlock() == null) {
            return;
        }

        if (this.gameManager.getTeamByCore(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
        }
    }

}
