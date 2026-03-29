package cn.thegoodboys.duels.type;

import cn.thegoodboys.duels.Duels;
import cn.thegoodboys.duels.arena.Arena;
import cn.thegoodboys.duels.arena.ArenaState;
import cn.thegoodboys.duels.utils.Utils;
import cn.thegoodboys.utils.util.TitleUtil;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.List;

public class DEBUFF implements Listener {
    private final String mapName;

    public DEBUFF(String mapName) {
        this.mapName = mapName;
    }

    public void init() {
        // 初始化
        Bukkit.getPluginManager().registerEvents(this, Duels.getInstance());
    }

    public void onJoin(Player player) {
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFlySpeed(0.1f);
        player.setWalkSpeed(0.2f);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setFireTicks(0);
        player.setFallDistance(0);
        TitleUtil.sendTitle(player, 10, 100, 10, "§e§l决斗游戏", "§6Debuff练习！");
        player.getInventory().setItem(8,new ItemBuilder(Material.SLIME_BALL).name("§c离开游戏 §7(右键打开)").build());
    }

    public void onStart() {
        List<Player> players = Arena.arenaPlayers.get(mapName);
        if (players != null && players.size() == 2) {
            // 传送玩家到出生点
            players.get(0).getInventory().clear();
            players.get(0).teleport(Arena.spawn1.get(mapName));
            players.get(0).getInventory().addItem(
                    new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, 1).enchant(
                            Enchantment.FIRE_ASPECT, 1
                    ).build()
            );
            players.get(0).getInventory().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(0).getInventory().setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(0).getInventory().setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(0).getInventory().setBoots(new ItemBuilder(Material.DIAMOND_BOOTS)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(0).getInventory().setItem(1,new ItemBuilder(Material.ENDER_PEARL).amount(16).build());
            players.get(0).getInventory().setItem(8,new ItemBuilder(Material.COOKED_BEEF).amount(64).build());
            players.get(0).getInventory().setItem(2,new ItemBuilder(Material.POTION).durability(8259).amount(1).build());
            players.get(0).getInventory().setItem(3,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            players.get(0).getInventory().setItem(17,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            players.get(0).getInventory().setItem(26,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            players.get(0).getInventory().setItem(35,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            for (int i = 0; i <= players.get(0).getInventory().getSize(); i++) {
                players.get(0).getInventory().addItem(new ItemBuilder(Material.POTION).durability(16421).build());
            }

            players.get(1).getInventory().clear();
            players.get(1).teleport(Arena.spawn2.get(mapName));
            players.get(1).getInventory().addItem(
                    new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, 1).enchant(
                            Enchantment.FIRE_ASPECT, 1
                    ).build()
            );
            players.get(1).getInventory().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(1).getInventory().setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(1).getInventory().setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(1).getInventory().setBoots(new ItemBuilder(Material.DIAMOND_BOOTS)
                    .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build());
            players.get(1).getInventory().setItem(1,new ItemBuilder(Material.ENDER_PEARL).amount(16).build());
            players.get(1).getInventory().setItem(8,new ItemBuilder(Material.COOKED_BEEF).amount(64).build());
            players.get(1).getInventory().setItem(2,new ItemBuilder(Material.POTION).durability(8259).amount(1).build());
            players.get(1).getInventory().setItem(3,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            players.get(1).getInventory().setItem(17,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            players.get(1).getInventory().setItem(26,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            players.get(1).getInventory().setItem(35,new ItemBuilder(Material.POTION).durability(8226).amount(1).build());
            for (int i = 0; i <= players.get(1).getInventory().getSize(); i++) {
                players.get(1).getInventory().addItem(new ItemBuilder(Material.POTION).durability(16421).build());
            }
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setDeathMessage(null);
        event.getDrops().clear();
        // 检查玩家是否在当前竞技场中
        if (isPlayerInArena(player)) {
            return;
        }
        // 检查游戏状态
        if (Arena.state.get(mapName) != ArenaState.PLAYING) {
            return;
        }
        if (event.getEntity().getKiller() != null) {
            //击杀信息
            Utils.castMessage("§c" + event.getEntity().getName() + "§f 被击杀 §f击杀者: §a" + event.getEntity().getKiller().getName(),mapName);
        }else {
        Utils.castMessage("§c" + event.getEntity().getName() + "§f 被击杀",mapName);
        }
        Bukkit.getScheduler().runTaskLater(Duels.getInstance(),()-> {
            player.spigot().respawn();
        },10);
    }

    @EventHandler
    private void onSpawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // 检查玩家是否在当前竞技场中
        if (isPlayerInArena(player)) {
            return;
        }
        // 检查游戏状态
        if (Arena.state.get(mapName) != ArenaState.PLAYING) {
            return;
        }
        Arena.setSpectator(mapName,player);
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            if (Arena.state.get(Arena.arenaMap.get(p)) != ArenaState.PLAYING) {
                event.setCancelled(true);
            }
        }
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
        HashMap<Location, Block> blocks = Arena.placedBlocks.computeIfAbsent(mapName, k -> new HashMap<>());
        blocks.put(event.getBlock().getLocation(), event.getBlock());
    }

    // 辅助方法：检查玩家是否在当前竞技场中
    private boolean isPlayerInArena(Player player) {
        List<Player> players = Arena.arenaPlayers.get(mapName);
        return players == null || !players.contains(player);
    }
}