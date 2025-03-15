package net.dragoria.cores.game;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.Task;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import net.dragoria.cores.Cores;
import net.dragoria.cores.config.CoresConfig;
import net.dragoria.cores.config.object.ConfigLocation;
import net.dragoria.cores.config.object.MapConfig;
import net.dragoria.cores.game.object.GameState;
import net.dragoria.cores.game.object.player.GamePlayer;
import net.dragoria.cores.game.object.spectator.SpectatorManager;
import net.dragoria.cores.game.object.team.Team;
import net.dragoria.cores.item.ItemUtil;
import net.dragoria.cores.listener.EntityDamageListener;
import net.dragoria.cores.util.Countdown;
import net.dragoria.cores.util.MessageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@Singleton
public class GameManager {

    private final Cores plugin;
    private final CoresConfig config;
    private final ItemUtil itemUtil;
    //Scoreboard
    private final Provider<EntityDamageListener> entityDamageListenerProvider;

    private final List<GamePlayer> players = new ArrayList<>();

    private GameState gameState = GameState.LOBBY;

    private MapConfig currentMap;
    private Countdown startCountdown;

    private final SpectatorManager spectatorManager;

    private Team winnerTeam;

    public GameManager(Cores plugin, CoresConfig config, ItemUtil itemUtil, MapManager mapManager, Provider<EntityDamageListener> entityDamageListenerProvider, SpectatorManager spectatorManager) {
        this.plugin = plugin;
        this.config = config;
        this.itemUtil = itemUtil;
        this.entityDamageListenerProvider = entityDamageListenerProvider;
        this.spectatorManager = spectatorManager;

        if (this.config.getPossibleMaps().isEmpty()) {
            plugin.getLogger().warning("No maps have been configured. Setup a new map with /cores setup");
            return;
        }

        Random random = new Random();
        this.currentMap = this.config.getPossibleMaps().get(random.nextInt(this.config.getPossibleMaps().size()));

        mapManager.prepareWorldsForSelectedMap(this.config, this.currentMap);
    }

    public void registerNewGamePlayer(@NotNull Player player) {
        this.players.add(new GamePlayer(player.getUniqueId()));

        int onlinePlayers = this.plugin.getServer().getOnlinePlayers().size();
        int requiredPlayers = this.currentMap.getRequiredPlayers();

        if (onlinePlayers == requiredPlayers) {
            Map<Integer, Runnable> executions = new HashMap<>();
            executions.put(15, () -> this.broadcastMessageWithPling("The game starts in 15 seconds!"));
            executions.put(10, () -> this.broadcastMessageWithPling("The game starts in 10 seconds!"));
            executions.put(5, () -> this.broadcastMessageWithPling("The game starts in 5 seconds!"));
            executions.put(4, () -> this.broadcastMessageWithPling("The game starts in 4 seconds!"));
            executions.put(3, () -> this.broadcastMessageWithPling("The game starts in 3 seconds!"));
            executions.put(2, () -> this.broadcastMessageWithPling("The game starts in 2 seconds!"));
            executions.put(1, () -> this.broadcastMessageWithPling("The game starts in one second!"));

            this.startCountdown = Countdown.fire(
                    15,
                    executions,
                    () -> {
                        this.fillTeams();

                        this.gameState = GameState.IN_GAME;

                        for (GamePlayer gamePlayer : this.players) {
                            Player gamePlayerPlayer = this.plugin.getServer().getPlayer(gamePlayer.getUniqueId()).orElse(null);

                            gamePlayerPlayer.setDisplayName(gamePlayer.getTeam().getColorCode() + gamePlayerPlayer.getName());

                            gamePlayerPlayer.teleport(gamePlayer.getSpawnLocation());

                            gamePlayerPlayer.getLevel().addSound(gamePlayerPlayer.getLocation(), Sound.RANDOM_LEVELUP, 1, 100);

                            this.itemUtil.giveItems(gamePlayerPlayer, gamePlayer.getTeam());

                            //Todo Stats gespieltes Spiel
                        }

                        MessageUtil.broadcastMessageWithPrefix("The game has started!");
                    }, false, plugin
            );

        }
    }

    private void fillTeams() {
        Random random = new Random();
        List<Team> allTeams = new ArrayList<>(currentMap.getTeams());

        for (GamePlayer player : this.players) {
            if (player.getTeam() != null) {
                List<Team> availableTeams = allTeams.stream()
                        .filter(team -> {
                            long countPlayersInTeam = this.players.stream()
                                    .filter(p -> team.equals(p.getTeam()))
                                    .count();
                            return countPlayersInTeam < this.currentMap.getPlayersPerTeam();
                        })
                        .collect(Collectors.toList());

                if (availableTeams.isEmpty()) {
                    throw new IllegalStateException("Unable to assign team to player " + player + ". All teams are full");
                }
            }
        }
    }

    private void checkForEmptyTeams() {
        Map<Team, List<GamePlayer>> teamPlayers = this.players.stream()
                .collect(Collectors.groupingBy(GamePlayer::getTeam));

        List<Team> emptyTeams = teamPlayers.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!emptyTeams.isEmpty()) {
            for (Team emptyTeam : emptyTeams) {
                MessageUtil.broadcastMessageWithPrefix("§cTeam " + emptyTeam.getDisplayName() + " §7has no more players!");
            }

            Optional<Team> winningTeam = teamPlayers.keySet().stream()
                    .filter(team -> !emptyTeams.contains(team))
                    .findFirst();

            winningTeam.ifPresent(this::endGame);
        }
    }


    @Nullable
    public GamePlayer getPlayer(@NotNull UUID uniqueId) {
        return this.players.stream()
                .filter(player -> player.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public Team getTeamByCore(@NotNull Location blockLocation) {
        return this.currentMap.getTeams().stream()
                .filter(team -> {
                    ConfigLocation coreLocation = team.getCoreLocation();
                    return coreLocation.getWorldName().equalsIgnoreCase(blockLocation.getLevel().getName()) &&
                            coreLocation.getX() == blockLocation.getFloorX() &&
                            coreLocation.getY() == blockLocation.getFloorY() &&
                            coreLocation.getZ() == blockLocation.getFloorZ();
                })
                .findFirst()
                .orElse(null);
    }

    public void handlePlayerQuit(@NotNull Player player) {
        if (!this.spectatorManager.getSpectators().contains(player)) {
            if (this.gameState.equals(GameState.IN_GAME)) {
                MessageUtil.broadcastMessageWithPrefix("§e" + player.getDisplayName() + " §7has left the game");
            }

            if (this.gameState.equals(GameState.LOBBY) || this.gameState.equals(GameState.ENDED)) {
                MessageUtil.broadcastMessageWithPrefix( "§4" + player.getName() + " §7has left the game");
            }

            GamePlayer removedPlayer = this.getPlayer(player.getUniqueId());
            this.players.removeIf(gamePlayer -> gamePlayer.getUniqueId().equals(player.getUniqueId()));

            this.spectatorManager.getSpectators().remove(player);

            if (removedPlayer != null && removedPlayer.getTeam() != null) {
                checkForEmptyTeams();
            }

            if (this.gameState.equals(GameState.LOBBY)) {
                if (this.players.size() != this.currentMap.getRequiredPlayers() && this.startCountdown != null) {
                    MessageUtil.broadcastMessageWithPrefix("The countdown was canceled because a player left the game.");
                    this.startCountdown.cancel();
                    this.startCountdown = null;
                }
            }
        }
    }

    public void stopGame(@NotNull Team winnerTeam) {
        this.entityDamageListenerProvider.get().getPendingRespawns().stream()
                .filter(countdown -> !countdown.isFinished())
                .forEach(Countdown::cancel);

        winnerTeam.setWins(winnerTeam.getWins() + 1);

        if (winnerTeam.getWins() >= 2) {
            this.endGame(winnerTeam);
            return;
        }

        for (GamePlayer gamePlayer : this.players) {
            Player gamePlayerPlayer = this.plugin.getServer().getPlayer(gamePlayer.getUniqueId()).orElse(null);;

            if (gamePlayerPlayer == null) {
                continue;
            }

            gamePlayerPlayer.setDisplayName(gamePlayer.getTeam().getColorCode() + gamePlayerPlayer.getName());
            gamePlayerPlayer.teleport(gamePlayer.getSpawnLocation());
            this.itemUtil.resetPlayer(gamePlayerPlayer);
            this.itemUtil.giveItems(gamePlayerPlayer, gamePlayer.getTeam());
            gamePlayerPlayer.getInventory().sendContents(gamePlayerPlayer);

            makePlayerImmobile(gamePlayerPlayer, gamePlayer.getSpawnLocation(), 3);

            Map<Integer, Runnable> runnableSeconds = new HashMap<>();
            runnableSeconds.put(3, () -> {
                MessageUtil.sendMessageWithPrefix(gamePlayerPlayer, "§7Next round in §e3 §7seconds");
                gamePlayerPlayer.getLevel().addSound(gamePlayerPlayer.getLocation(), Sound.NOTE_BASS, 1, 100);
            });
            runnableSeconds.put(2, () -> {
                MessageUtil.sendMessageWithPrefix(gamePlayerPlayer, "§7Next round in §e2 §7seconds");
                gamePlayerPlayer.getLevel().addSound(gamePlayerPlayer.getLocation(), Sound.NOTE_BASS, 1, 100);
            });
            runnableSeconds.put(1, () -> {
                MessageUtil.sendMessageWithPrefix(gamePlayerPlayer, "§7Next round in §eone §7second");
                gamePlayerPlayer.getLevel().addSound(gamePlayerPlayer.getLocation(), Sound.NOTE_BASS, 1, 100);
            });

            Countdown.fire(3, runnableSeconds, () -> {
                MessageUtil.sendMessageWithPrefix(gamePlayerPlayer, "§aThe round has started");
                gamePlayerPlayer.getLevel().addSound(gamePlayerPlayer.getLocation(), Sound.RANDOM_LEVELUP, 1, 100);
            }, false, plugin);
        }

        for (Player player : this.spectatorManager.getSpectators()) {
            player.teleport(currentMap.getSpectatorLocation().toNukkit());
        }
    }

    private void endGame(@NotNull Team winnerTeam) {
        this.gameState = GameState.ENDED;
        this.winnerTeam = winnerTeam;

        for (GamePlayer gamePlayer : this.players) {
            Player player = this.plugin.getServer().getPlayer(gamePlayer.getUniqueId()).orElse(null);
            if (player == null) continue;

            player.teleport(this.config.getLobbyLocation().toNukkit());
            this.itemUtil.resetPlayer(player);

            player.sendTitle("§8» " + winnerTeam.getDisplayName() + " §8«", "§7has won the game!");

            if (gamePlayer.getTeam() != null && gamePlayer.getTeam().equals(winnerTeam)) {
                //Todo Stats gewonnes Spiel
            }
        }

        for (Player player : this.spectatorManager.getSpectators()) {
            if (player == null) continue;

            player.teleport(this.config.getLobbyLocation().toNukkit());
            this.itemUtil.resetPlayer(player);

            player.sendTitle("§8» " + winnerTeam.getDisplayName() + " §8«", "§7has won the game!");
        }


        Map<Integer, Runnable> messages = new HashMap<>();
        messages.put(15, () -> this.broadcastMessageWithPling("The server will restart in 15 seconds."));
        messages.put(10, () -> this.broadcastMessageWithPling("The server will restart in 10 seconds."));
        messages.put(5, () -> this.broadcastMessageWithPling("The server will restart in 5 seconds."));
        messages.put(4, () -> this.broadcastMessageWithPling("The server will restart in 4 seconds."));
        messages.put(3, () -> this.broadcastMessageWithPling("The server will restart in 3 seconds."));
        messages.put(2, () -> this.broadcastMessageWithPling("The server will restart in 2 seconds."));
        messages.put(1, () -> this.broadcastMessageWithPling("The server will restart in one second."));

        Countdown.fire(15, messages, () -> {
            for (Player player : this.plugin.getServer().getOnlinePlayers().values()) {
                player.sendMessage(MessageUtil.getMessageWithPrefix("The server is restarting."));
            }

            this.plugin.getServer().shutdown();
        }, false, plugin);
    }

    private void broadcastMessageWithPling(@NotNull String message) {
        for (Player player : this.plugin.getServer().getOnlinePlayers().values()) {
            MessageUtil.sendMessageWithPrefix(player, message);
            player.getLevel().addSound(player.getLocation(), Sound.NOTE_PLING, 1, 100);
        }
    }

    public static void makePlayerImmobile(Player player, Location teleportLocation, int durationInSeconds) {
        long startTime = System.currentTimeMillis();

        int taskId = Server.getInstance().getScheduler().scheduleRepeatingTask(new Task() {
            @Override
            public void onRun(int currentTick) {
                long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

                if (elapsedTime >= durationInSeconds) {
                    this.cancel();
                    return;
                }

                if (player.getLocation().distanceSquared(teleportLocation) > 0.1) {
                    player.teleport(teleportLocation);
                }
            }
        }, 1).getTaskId();
    }
}
