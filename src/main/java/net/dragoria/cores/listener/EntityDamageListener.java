package net.dragoria.cores.listener;

import cn.nukkit.Player;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.level.Sound;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dragoria.cores.Cores;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.game.object.GameState;
import net.dragoria.cores.game.object.player.GamePlayer;
import net.dragoria.cores.item.ItemUtil;
import net.dragoria.cores.util.Countdown;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class EntityDamageListener implements Listener {

    private final Map<Player, Player> lastDamager = new HashMap<>();
    private final List<UUID> spawnProtection = new CopyOnWriteArrayList<>();

    @Getter
    private final List<Countdown> pendingRespawns = new CopyOnWriteArrayList<>();

    private final Cores plugin;
    private final GameManager gameManager;
    //Scoreboard
    private final ItemUtil itemUtil;

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getEntity();
        if (player.getGamemode() == Player.SPECTATOR) {
            event.setCancelled(true);
            return;
        }

        GamePlayer gamePlayer = this.gameManager.getPlayer(player.getUniqueId());
        if (gamePlayer == null) {
            event.setCancelled(true);
            return;
        }

        if (this.spawnProtection.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!this.gameManager.getGameState().equals(GameState.IN_GAME)) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getEntity();
        GamePlayer targetGamePlayer = this.gameManager.getPlayer(target.getUniqueId());

        if (targetGamePlayer == null) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            GamePlayer damagerGamePlayer = this.gameManager.getPlayer(damager.getUniqueId());

            if (damagerGamePlayer == null || targetGamePlayer.getTeam().equals(damagerGamePlayer.getTeam())) {
                event.setCancelled(true);
                return;
            }

            this.lastDamager.put(target, damager);
        } else if (event.getDamager() instanceof EntityProjectile) {
            EntityProjectile projectile = (EntityProjectile) event.getDamager();

            if (projectile.shootingEntity instanceof Player) {
                Player shooter = (Player) projectile.shootingEntity;
                GamePlayer shooterGamePlayer = this.gameManager.getPlayer(shooter.getUniqueId());

                if (shooterGamePlayer == null || targetGamePlayer.getTeam().equals(shooterGamePlayer.getTeam())) {
                    event.setCancelled(true);
                    return;
                }

                this.lastDamager.put(target, shooter);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gamePlayer = this.gameManager.getPlayer(player.getUniqueId());

        if (gamePlayer == null) {
            return;
        }

        Player lastDamager = this.lastDamager.remove(player);

        event.getDrops();

        if (lastDamager != null) {
            GamePlayer lastDamagerGamePlayer = this.gameManager.getPlayer(lastDamager.getUniqueId());
            if (lastDamagerGamePlayer != null) {
                lastDamagerGamePlayer.setKills(lastDamagerGamePlayer.getKills() + 1);
                //scoreboard
                lastDamager.getLevel().addSound(lastDamager.getLocation(), Sound.RANDOM_LEVELUP, 40, 1);
            }

            event.setDeathMessage("§7[§bCores§7] §7" + "§e" + player.getDisplayName() + " §7was kiled by §e" + lastDamager.getDisplayName());

            //Todo Stats tod und kill
        } else {
            event.setDeathMessage("§7[§bCores§7] §7" + "§e" + player.getDisplayName() + " §7has died");
            //Todo Stats wenn ein spieler alleine stirbt
        }

        player.setGamemode(Player.SPECTATOR);

        //todo spieler respawn
        player.teleport(gamePlayer.getSpawnLocation());

        Map<Integer, Runnable> runnableSeconds = new HashMap<>();
        runnableSeconds.put(5, () -> player.sendTitle("§6Respawn in", "§e5 seconds"));
        runnableSeconds.put(4, () -> player.sendTitle("§6Respawn in", "§e4 seconds"));
        runnableSeconds.put(3, () -> player.sendTitle("§6Respawn in", "§e3 seconds"));
        runnableSeconds.put(2, () -> player.sendTitle("§6Respawn in", "§e2 seconds"));
        runnableSeconds.put(1, () -> player.sendTitle("§6Respawn in", "§e1 second"));

        Countdown countdown = Countdown.fire(5, runnableSeconds, () -> {
            //todo spieler respawn
            player.teleport(gamePlayer.getSpawnLocation());
            this.itemUtil.resetPlayer(player);
            this.itemUtil.giveItems(player, gamePlayer.getTeam());
            this.handleSpawnProtection(player.getUniqueId());
        }, false, plugin);

        this.pendingRespawns.add(countdown);
    }

    private void handleSpawnProtection(@NotNull UUID uniqueId) {
        this.spawnProtection.add(uniqueId);
        this.plugin.getServer().getScheduler().scheduleDelayedTask(this.plugin, () -> this.spawnProtection.remove(uniqueId), 20 * 5);
    }
}
