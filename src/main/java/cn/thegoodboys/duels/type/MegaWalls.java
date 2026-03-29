package cn.thegoodboys.duels.type;

import cn.thegoodboys.duels.Duels;
import cn.thegoodboys.duels.arena.Arena;
import cn.thegoodboys.duels.arena.ArenaState;
import cn.thegoodboys.utils.util.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.List;

public class MegaWalls implements Listener {

    private final String mapName;

    public MegaWalls(String mapName) {
        this.mapName = mapName;
    }

    public void init() {
        // 初始化
        Bukkit.getPluginManager().registerEvents(this, Duels.getInstance());
    }

    public void onJoin(Player player) {

    }

    public void onStart() {

    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // 检查玩家是否在当前竞技场中
        if (isPlayerInArena(player)) {
            return;
        }
        // 检查游戏状态
        if (Arena.state.get(mapName) != ArenaState.PLAYING) {
            event.setCancelled(true);
            return;
        }
        // 检查是否是玩家放置的方块
        HashMap<Location, Block> placedBlocks = Arena.placedBlocks.get(mapName);
        if (placedBlocks == null || !placedBlocks.containsKey(event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§c你只能破坏玩家放置的方块！");
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否在当前竞技场中
        if (isPlayerInArena(player)) {
            return;
        }
        // 检查游戏状态
        if (Arena.state.get(mapName) != ArenaState.PLAYING) {
            event.setCancelled(true);
            return;
        }
        // 修复：将放置的方块添加到现有的记录中
        HashMap<Location, Block> blocks = Arena.placedBlocks.computeIfAbsent(mapName, k -> new HashMap<>());
        blocks.put(event.getBlock().getLocation(), event.getBlock());
    }

    // 辅助方法：检查玩家是否在当前竞技场中
    private boolean isPlayerInArena(Player player) {
        List<Player> players = Arena.arenaPlayers.get(mapName);
        return players == null || !players.contains(player);
    }
}
