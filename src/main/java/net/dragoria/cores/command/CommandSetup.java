package net.dragoria.cores.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import com.google.inject.Singleton;
import net.dragoria.cores.Cores;
import net.dragoria.cores.config.ConfigService;
import net.dragoria.cores.config.CoresConfig;
import net.dragoria.cores.config.object.ConfigLocation;
import net.dragoria.cores.config.object.MapConfig;
import net.dragoria.cores.game.GameManager;
import net.dragoria.cores.game.object.team.Team;
import net.dragoria.cores.util.MessageUtil;

@Singleton
public class CommandSetup extends Command {

    private ConfigService configService;
    private GameManager gameManager;
    private CoresConfig config;
    private Cores plugin;

    public CommandSetup() {
        super("cores");
        this.setDescription("Cores Setup Command");
        this.setPermission("cores.setup");
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("cores.setup")) {
            MessageUtil.sendMessageWithPrefix(player, "§cYou do not have permission to use this command");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("setLobby")) {
            this.config.setLobbyLocation(this.fromLocation(player.getLocation()));
            this.configService.storeConfig();

            MessageUtil.sendMessageWithPrefix(player, "§7Die Lobby-Location wurde §aerfolgreich §7gesetzt");
            return true;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("createMap")) {
                String name = args[1];
                int itemId;
                try {
                    itemId = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessageWithPrefix(player, "§7Ungültige Item-ID: §e" + args[2]);
                    return true;
                }

                MapConfig mapConfig = new MapConfig(name, String.valueOf(itemId));
                this.configService.getConfig().getPossibleMaps().add(mapConfig);
                this.configService.storeConfig();

                MessageUtil.sendMessageWithPrefix(player, "§7Die Map §a" + name + " §7wurde §aerfolgreich §7erstellt");
                return true;
            }

            if (args[0].equalsIgnoreCase("createTeam")) {
                String mapName = args[1];
                String teamName = args[2];

                MapConfig mapConfig = this.configService.getConfig().getPossibleMaps().stream()
                        .filter(m -> m.getName().equalsIgnoreCase(mapName))
                        .findFirst().orElse(null);

                if (mapConfig == null) {
                    MessageUtil.sendMessageWithPrefix(player, "§7Die Map §e" + mapName + " §7wurde nicht gefunden");
                    return true;
                }

                Team team = new Team(teamName, "f", 0);
                mapConfig.getTeams().add(team);
                this.configService.storeConfig();

                MessageUtil.sendMessageWithPrefix(player, "§7Das Team §a" + teamName + " §7wurde §aerfolgreich §7erstellt");
                return true;
            }

            if (args[0].equalsIgnoreCase("setCore")) {
                String mapName = args[1];
                String teamName = args[2];

                MapConfig mapConfig = this.configService.getConfig().getPossibleMaps().stream()
                        .filter(m -> m.getName().equalsIgnoreCase(mapName))
                        .findFirst().orElse(null);

                if (mapConfig == null) {
                    MessageUtil.sendMessageWithPrefix(player, "§7Die Map §e" + mapName + " §7wurde nicht gefunden");
                    return true;
                }

                Team team = mapConfig.getTeams().stream()
                        .filter(t -> t.getName().equalsIgnoreCase(teamName))
                        .findFirst().orElse(null);

                if (team == null) {
                    MessageUtil.sendMessageWithPrefix(player, "§7Das Team §e" + teamName + " §7wurde nicht gefunden");
                    return true;
                }

                Block lookingBlock = getTargetBlock(player, 5);

                if (lookingBlock == null) {
                    MessageUtil.sendMessageWithPrefix(player, "§7Du schaust auf keinen Block");
                    return true;
                }

                int beforeMaterialId = lookingBlock.getId();
                lookingBlock.getLevel().setBlock(lookingBlock.getLocation(), Block.get(Block.DIAMOND_BLOCK));

                Server.getInstance().getScheduler().scheduleDelayedTask(this.plugin, () ->
                        lookingBlock.getLevel().setBlock(lookingBlock.getLocation(), Block.get(beforeMaterialId)), 100);

                team.setCoreLocation(this.fromLocation(lookingBlock.getLocation()));
                this.configService.storeConfig();

                MessageUtil.sendMessageWithPrefix(player, "§7Der Core für das Team §a" + teamName + " §7wurde §aerfolgreich §7gesetzt");
                return true;
            }
        }

        MessageUtil.sendMessageWithPrefix(player, "§7Verfügbare Commands:");
        MessageUtil.sendMessageWithPrefix(player, "§c/cores setLobby");
        MessageUtil.sendMessageWithPrefix(player, "§c/cores createMap <name> <item-id>");
        MessageUtil.sendMessageWithPrefix(player, "§c/cores createTeam <map-name> <team-name>");
        MessageUtil.sendMessageWithPrefix(player, "§c/cores setCore <map-name> <team-name>");

        return false;
    }

    private ConfigLocation fromLocation(Location location) {
        return new ConfigLocation(location.getLevel().getName(), location.getX(), location.getY(), location.getZ(), (float) location.getYaw(), (float) location.getPitch());
    }

    private Block getTargetBlock(Player player, int maxDistance) {
        Location eyeLocation = player.getLocation().add(0, player.getEyeHeight(), 0);
        Vector3 direction = eyeLocation.getDirectionVector();

        for (int i = 0; i < maxDistance; i++) {
            Location checkLocation = eyeLocation.add(direction.x * i, direction.y * i, direction.z * i);
            Block block = checkLocation.getLevel().getBlock(checkLocation);

            if (block != null && block.getId() != Block.AIR) {
                return block;
            }
        }

        return null;
    }
}
