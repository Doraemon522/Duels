package cn.thegoodboys.duels.listener;

import cn.thegoodboys.duels.Duels;
import cn.thegoodboys.duels.arena.Arena;
import cn.thegoodboys.utils.util.Utils;
import cn.thegoodboys.utils.util.chat.CC;
import cn.thegoodboys.utils.util.chat.scoreboard.FastBoard;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScoreBoard implements Listener {
    @Getter
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd");

    public void set(Player player) {
        FastBoard fastBoard = new FastBoard(player);
        Bukkit.getScheduler().runTaskTimer(Duels.getInstance(), () -> updateBoard(player, fastBoard), 0, 5);
    }

    private void updateBoard(Player player, FastBoard fastBoard) {
        fastBoard.updateTitle("§e§l竞技场");
        List<String> list = new ArrayList<>();
        list.add(getDateFormat().format(new Date()));

        if (Arena.lobbyPlayers.containsValue(player)) {
            list.add("");
            list.add("§e/duel <玩家名>");
            list.add("§e对一个玩家发起单挑");
            list.add("");
            list.add("§f总击杀: §aN/A");
            list.add("§f总死亡: §cN/A");
            list.add("§fK/D: §d1.00");
            list.add("");
            list.add("§f硬币: §6N/A");
            list.add("");
            list.add("§f赛季段位: §7NULL");
        } else {
            String playerArena = Arena.arenaMap.get(player);
            if (playerArena != null && Arena.arenaPlayers.containsKey(playerArena)) {
                List<Player> players = Arena.arenaPlayers.get(playerArena);
                if (players != null && players.contains(player)) {
                    String opponentName = "无！";
                    for (Player p : players) {
                        if (!p.equals(player)) {
                            opponentName = p.getName();
                            break;
                        }
                    }
                    list.add("");
                    list.add("§f游戏结束:");
                    list.add("§a§l" + Utils.getFormattedTime(Arena.gameTimes.get(playerArena)));
                    list.add("");
                    list.add("§f对手: §a" + opponentName);
                    list.add("");
                    list.add("§f游戏模式: §a" + Arena.arenaMode.get(playerArena));
                    list.add("");
                    list.add("§f当前地图: §a" + playerArena);
                }
            }
        }

        list.add("");
        list.add("§eIP: Mc.Mc233.Com.cn");
        list = CC.translate(list);
        fastBoard.updateLines(list);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Arena.lobbyPlayers.put("Lobby", e.getPlayer());
        set(e.getPlayer());
    }
}