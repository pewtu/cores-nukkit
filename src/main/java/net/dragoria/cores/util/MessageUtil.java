package net.dragoria.cores.util;

import cn.nukkit.Player;
import cn.nukkit.Server;
import org.jetbrains.annotations.NotNull;

public class MessageUtil {

    private static final String PREFIX = "§8[§bCores§8] §7";

    public static void broadcastMessageWithPrefix(@NotNull String message) {
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            player.sendMessage(PREFIX + message);
        }
    }

    public static void broadcastMessageWithoutPrefix(@NotNull String message) {
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            player.sendMessage(message);
        }
    }

    public static String getMessageWithPrefix(@NotNull String message) {
        return PREFIX + message;
    }

    public static void sendMessageWithPrefix(@NotNull Player player, @NotNull String message) {
        player.sendMessage(PREFIX + message);
    }
}
