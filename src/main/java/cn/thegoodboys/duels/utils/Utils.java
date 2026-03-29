package cn.thegoodboys.duels.utils;

import cn.thegoodboys.duels.arena.Arena;
import cn.thegoodboys.utils.util.TitleUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Utils {

    public static void castTitle(String title,String subtitle,int duration,int fadeIn,int fadeOut,String mapName) {
        for (Player player : Arena.arenaPlayers.get(mapName)) {
            TitleUtil.sendTitle(player,duration,fadeIn,fadeOut,title,subtitle);
        }
    }

    public static void castSound(Sound sound, int pitch, int volume,String mapName) {
        for (Player player : Arena.arenaPlayers.get(mapName)) {
            player.playSound(player.getLocation(),sound,pitch,volume);
        }
    }

    public static void castMessage(String message,String mapName) {
    for (Player player : Arena.arenaPlayers.get(mapName)) {
            player.sendMessage(message);
        }
    }
}
