package cn.thegoodboys.duels.listener;

import cn.thegoodboys.duels.arena.Arena;
import cn.thegoodboys.utils.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (Arena.lobbyPlayers.containsValue(event.getPlayer())) {
            event.getPlayer().getInventory().clear();
            event.getPlayer().getInventory().setItem(0,new ItemBuilder(Material.GOLD_SWORD).name("§e§l1V1").build());
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        if (Arena.lobbyPlayers.containsValue(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
