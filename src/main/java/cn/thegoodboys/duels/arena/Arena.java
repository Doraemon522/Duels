package cn.thegoodboys.duels.arena;

import cn.thegoodboys.duels.Duels;
import cn.thegoodboys.duels.team.Team;
import cn.thegoodboys.duels.type.DEBUFF;
import cn.thegoodboys.duels.type.MegaWalls;
import cn.thegoodboys.duels.utils.Utils;
import cn.thegoodboys.utils.util.NameTagUtils;
import cn.thegoodboys.utils.util.TitleUtil;
import cn.thegoodboys.utils.util.config.FileConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Arena implements IArena {

    // 游戏管理器

    //全局大厅在线玩家
    public static final HashMap<String, Player> lobbyPlayers = new HashMap<>();

    //游戏大厅位置
    public static final HashMap<String, Location> lobbyLoc = new HashMap<>();

    //游戏玩家位置
    public static final HashMap<String, Location> spawn1 = new HashMap<>();

    //游戏玩家位置
    public static final HashMap<String, Location> spawn2 = new HashMap<>();

    // 倒计时
    public static final HashMap<String, Integer> countdown = new HashMap<>();

    // 游戏状态
    public static final HashMap<String, ArenaState> state = new HashMap<>();

    // 游戏地图
    public static final HashMap<String, String> mapNames = new HashMap<>();

    // 游戏地图名字
    public static final HashMap<Player, String> arenaMap = new HashMap<>();

    // 游戏玩家 - 修复：每个竞技场应该存储一个玩家列表
    public static final ConcurrentHashMap<String, List<Player>> arenaPlayers = new ConcurrentHashMap<>();

    // 存储每个地图的倒计时任务，用于取消
    public static final HashMap<String, BukkitRunnable> countdownTasks = new HashMap<>();

    // 新增：存储每个地图的游戏时间计时任务
    public static final HashMap<String, BukkitRunnable> gameTimeTasks = new HashMap<>();

    //竞技场模式
    public static final HashMap<String, ArenaMode> arenaMode = new HashMap<>();

    //放置方块
    public static final HashMap<String, HashMap<Location, Block>> placedBlocks = new HashMap<>();

    //游戏时间
    public static final HashMap<String, Integer> gameTimes = new HashMap<>();

    //旁观者
    public static final HashMap<String, List<Player>> spectators = new HashMap<>();

    // ========== 新增队伍系统 ==========
    // 竞技场游戏模式 (SOLO 或 TWO_TWO)
    public static final HashMap<String, Mode> arenaGameMode = new HashMap<>();

    // 每个竞技场的队伍列表
    public static final HashMap<String, List<Team>> arenaTeams = new HashMap<>();

    // 玩家所在的队伍
    public static final HashMap<Player, Team> playerTeam = new HashMap<>();

    // 每个竞技场的最小玩家数
    public static final HashMap<String, Integer> minPlayers = new HashMap<>();

    // 每个竞技场的最大玩家数
    public static final HashMap<String, Integer> maxPlayers = new HashMap<>();

    // 队伍2的第二个玩家出生点
    public static final HashMap<String, Location> spawn3 = new HashMap<>();

    // 队伍1的第二个玩家出生点
    public static final HashMap<String, Location> spawn4 = new HashMap<>();
    // ========== 队伍系统结束 ==========

    // 初始化游戏
    @Override
    public void init(String mapName) {
        FileConfig fileConfig = new FileConfig(Duels.getInstance(), mapName + ".yml");
        lobbyLoc.put(mapName, fileConfig.getLocation("Lobby"));
        spawn1.put(mapName, fileConfig.getLocation("Spawn1"));
        spawn2.put(mapName, fileConfig.getLocation("Spawn2"));

        // ========== 新增：读取游戏模式配置 ==========
        String modeString = fileConfig.getString("GameMode", "SOLO");
        Mode gameMode;
        try {
            gameMode = Mode.valueOf(modeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            gameMode = Mode.SOLO;
            Duels.getInstance().getLogger().warning("Invalid GameMode '" + modeString + "' for map " + mapName + ", using SOLO");
        }
        arenaGameMode.put(mapName, gameMode);

        // 根据游戏模式设置玩家数量
        if (gameMode == Mode.SOLO) {
            minPlayers.put(mapName, 2);
            maxPlayers.put(mapName, 2);
        } else {
            minPlayers.put(mapName, 4);
            maxPlayers.put(mapName, 4);
            // 读取额外的出生点
            if (fileConfig.getLocation("Spawn3") != null) {
                spawn3.put(mapName, fileConfig.getLocation("Spawn3"));
            }
            if (fileConfig.getLocation("Spawn4") != null) {
                spawn4.put(mapName, fileConfig.getLocation("Spawn4"));
            }
        }

        // 初始化队伍
        initTeams(mapName, gameMode);
        // ========== 新增结束 ==========

        countdown.put(mapName, 15);
        state.put(mapName, ArenaState.WAITING);
        arenaMode.put(mapName, getArenaModeOrDefault(fileConfig, mapName));
        mapNames.put(mapName, fileConfig.getString("Map"));
        arenaPlayers.put(mapName, new ArrayList<>());
        gameTimes.put(mapName, 9 * 60);
        spectators.put(mapName, new ArrayList<>());
        placedBlocks.put(mapName, new HashMap<>());
    }

    // ========== 新增：初始化队伍 ==========
    private void initTeams(String mapName, Mode gameMode) {
        List<Team> teams = new ArrayList<>();
        if (gameMode == Mode.SOLO) {
            Team team1 = new Team("§c红队");
            Team team2 = new Team("§9蓝队");
            teams.add(team1);
            teams.add(team2);
        } else {
            Team team1 = new Team("§c红队");
            Team team2 = new Team("§9蓝队");
            teams.add(team1);
            teams.add(team2);
        }
        arenaTeams.put(mapName, teams);
    }

    // ========== 新增：分配玩家到队伍 ==========
    private void assignToTeam(String mapName, Player player) {
        List<Team> teams = arenaTeams.get(mapName);
        Mode gameMode = arenaGameMode.get(mapName);

        if (teams == null) return;

        int playersPerTeam = (gameMode == Mode.SOLO) ? 1 : 2;

        for (Team team : teams) {
            if (team.getPlayers().size() < playersPerTeam) {
                team.addPlayer(player);
                playerTeam.put(player, team);
                break;
            }
        }
    }

    // ========== 新增：传送到对应出生点 ==========
    private void teleportToSpawn(String mapName, Player player) {
        Team team = playerTeam.get(player);
        List<Team> teams = arenaTeams.get(mapName);
        Mode gameMode = arenaGameMode.get(mapName);

        if (team == null || teams == null) {
            player.teleport(lobbyLoc.get(mapName));
            return;
        }

        int teamIndex = teams.indexOf(team);
        int playerIndex = team.getPlayers().indexOf(player);

        if (gameMode == Mode.SOLO) {
            // SOLO模式：队伍1去spawn1，队伍2去spawn2
            if (teamIndex == 0) {
                player.teleport(spawn1.get(mapName));
            } else {
                player.teleport(spawn2.get(mapName));
            }
        } else {
            // TWO_TWO模式：队伍1: 第一个玩家spawn1，第二个玩家spawn4
            //           队伍2: 第一个玩家spawn2，第二个玩家spawn3
            if (teamIndex == 0) {
                if (playerIndex == 0) {
                    player.teleport(spawn1.get(mapName));
                } else {
                    Location loc = spawn4.get(mapName);
                    if (loc != null) {
                        player.teleport(loc);
                    } else {
                        player.teleport(spawn1.get(mapName));
                    }
                }
            } else {
                if (playerIndex == 0) {
                    player.teleport(spawn2.get(mapName));
                } else {
                    Location loc = spawn3.get(mapName);
                    if (loc != null) {
                        player.teleport(loc);
                    } else {
                        player.teleport(spawn2.get(mapName));
                    }
                }
            }
        }
    }
    // ========== 新增结束 ==========

    private ArenaMode getArenaModeOrDefault(FileConfig fileConfig, String mapName) {
        String modeString = fileConfig.getString("Mode");
        if (modeString == null || modeString.isEmpty()) {
            Duels.getInstance().getLogger().warning("Mode not specified for map " + mapName + ", using DEFAULT");
            return ArenaMode.DEBUFF;
        }
        try {
            return ArenaMode.valueOf(modeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            Duels.getInstance().getLogger().warning("Invalid mode '" + modeString + "' for map " + mapName + ", using DEFAULT");
            return ArenaMode.DEBUFF;
        }
    }

    @Override
    public void onJoin(String mapName, Player player) {
        List<Player> players = arenaPlayers.computeIfAbsent(mapName, k -> new ArrayList<>());

        if (players.contains(player)) {
            return;
        }

        // ========== 修改：检查最大玩家数 ==========
        int currentSize = players.size();
        int max = maxPlayers.getOrDefault(mapName, 2);

        if (currentSize >= max) {
            player.sendMessage("§c游戏已满！");
            return;
        }
        // ========== 修改结束 ==========

        players.add(player);
        arenaMap.put(player, mapName);
        lobbyPlayers.remove("Lobby", player);
        player.getInventory().clear();
        NameTagUtils.INSTANCE.clearAll(player);

        // ========== 修改：分配到队伍并传送到对应出生点 ==========
        assignToTeam(mapName, player);
        teleportToSpawn(mapName, player);

        // 只有当人数达到最小玩家数且处于等待状态时才启动倒计时
        int min = minPlayers.getOrDefault(mapName, 2);
        if (players.size() >= min && state.get(mapName) == ArenaState.WAITING) {
            Bukkit.getScheduler().runTaskLater(Duels.getInstance(), () -> setCountdown(mapName), 20);
        }

        switch (arenaMode.get(mapName)) {
            case DEBUFF:
                new DEBUFF(mapName).onJoin(player);
                break;
            case MEGAWALLS:
                new MegaWalls(mapName).onJoin(player);
                break;
        }
        Utils.castMessage("§a" + player.getName() + " 加入了游戏！(" + players.size() + "/" + max + ")", mapName);
    }

    private void setCountdown(String mapName) {
        List<Player> players = arenaPlayers.get(mapName);
        int min = minPlayers.getOrDefault(mapName, 2);

        if (players == null || players.size() < min) {
            return;
        }
        if (state.get(mapName) != ArenaState.WAITING) {
            return;
        }

        state.put(mapName, ArenaState.STARTING);
        if (countdownTasks.containsKey(mapName)) {
            countdownTasks.get(mapName).cancel();
            countdownTasks.remove(mapName);
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> currentPlayers = arenaPlayers.get(mapName);
                int minPlayersCount = minPlayers.getOrDefault(mapName, 2);
                if (currentPlayers == null || currentPlayers.size() < minPlayersCount) {
                    cancel();
                    countdown.put(mapName, 15);
                    state.put(mapName, ArenaState.WAITING);
                    countdownTasks.remove(mapName);
                    Utils.castMessage("§c玩家不足！倒计时取消！", mapName);
                    return;
                }
                int time = countdown.getOrDefault(mapName, 15);
                if (time <= 0) {
                    cancel();
                    countdownTasks.remove(mapName);
                    onStart(mapName);
                    return;
                }
                if (time == 10 || time <= 5) {
                    Utils.castTitle("§c§l" + time, "§e准备战斗吧！", 10, 5, 10, mapName);
                    Utils.castSound(Sound.WOOD_CLICK, 1, 1, mapName);
                }
                countdown.put(mapName, time - 1);
            }
        };

        task.runTaskTimer(Duels.getInstance(), 0, 20);
        countdownTasks.put(mapName, task);
    }

    @Override
    public void onLeave(String mapName, Player player) {
        List<Player> players = arenaPlayers.get(mapName);
        lobbyPlayers.put("Lobby", player);
        if (players != null) {
            players.remove(player);
        }

        Team team = playerTeam.remove(player);
        if (team != null) {
            team.removePlayer(player);
        }
        // 如果玩家是旁观者，也从旁观者列表移除
        List<Player> specList = spectators.get(mapName);
        if (specList != null) {
            specList.remove(player);
        }
        arenaMap.remove(player);
        Utils.castMessage("§e" + player.getName() + " 离开了游戏！", mapName);
    }

    @Override
    public void onStart(String mapName) {
        state.put(mapName, ArenaState.PLAYING);
        cn.thegoodboys.utils.util.Utils.setColoredNamesForAll();
        switch (arenaMode.get(mapName)) {
            case DEBUFF:
                new DEBUFF(mapName).onStart();
                break;
            case MEGAWALLS:
                new MegaWalls(mapName).onStart();
                break;
        }

        // 取消之前的游戏时间任务
        if (gameTimeTasks.containsKey(mapName)) {
            BukkitRunnable oldTask = gameTimeTasks.get(mapName);
            if (oldTask != null) {
                oldTask.cancel();
            }
            gameTimeTasks.remove(mapName);
        }

        // 启动新的游戏时间计时任务
        BukkitRunnable gameTimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (state.get(mapName) != ArenaState.PLAYING) {
                    return;
                }
                if (gameTimes.get(mapName) <= 0) {
                    cancel();
                    gameTimeTasks.remove(mapName);
                } else {
                    gameTimes.put(mapName, gameTimes.get(mapName) - 1);
                }
                getWinner(mapName);
            }
        };
        gameTimeTask.runTaskTimer(Duels.getInstance(), 0, 20);
        gameTimeTasks.put(mapName, gameTimeTask);
    }

    // ========== 修改：getWinner方法，支持队伍系统 ==========
    public void getWinner(String mapName) {
        // 获取非旁观者的存活玩家
        List<Player> players = arenaPlayers.get(mapName);
        List<Player> alivePlayers = new ArrayList<>();

        // 检查并清理离线的玩家
        if (players != null) {
            Iterator<Player> iterator = players.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                // 如果玩家离线或不在线，从列表中移除
                if (player == null || !player.isOnline()) {
                    iterator.remove();
                    Team team = playerTeam.remove(player);
                    if (team != null) {
                        team.removePlayer(player);
                    }
                    arenaMap.remove(player);
                    Utils.castMessage("§c" + (player != null ? player.getName() : "未知玩家") + " 因断线被移除！", mapName);
                    continue;
                }

                // 检查是否是旁观者
                List<Player> specList = spectators.get(mapName);
                if (specList == null || !specList.contains(player)) {
                    alivePlayers.add(player);
                }
            }
        }

        // 检查旁观者列表中是否有离线玩家
        List<Player> specList = spectators.get(mapName);
        if (specList != null) {
            Iterator<Player> iterator = specList.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (player == null || !player.isOnline()) {
                    iterator.remove();
                    arenaMap.remove(player);
                }
            }
        }

        // 检查游戏状态，如果玩家不足最小玩家数且游戏正在进行中或倒计时中
        int playerCount = players != null ? players.size() : 0;
        int min = minPlayers.getOrDefault(mapName, 2);
        ArenaState currentState = state.get(mapName);

        // ========== 修改：使用队伍系统判断胜负 ==========
        if (currentState == ArenaState.PLAYING) {
            // 检查队伍存活情况
            List<Team> teams = arenaTeams.get(mapName);
            if (teams != null) {
                List<Team> aliveTeams = new ArrayList<>();
                for (Team team : teams) {
                    if (team.isAlive()) {
                        aliveTeams.add(team);
                    }
                }

                // 只有一个队伍存活时，该队伍获胜
                if (aliveTeams.size() == 1) {
                    Team winnerTeam = aliveTeams.get(0);
                    endGame(mapName, winnerTeam);
                    return;
                }
            }
        }

        // 玩家不足时结束游戏
        if (playerCount < min && (currentState == ArenaState.PLAYING || currentState == ArenaState.STARTING)) {
            if (playerCount == 1 && alivePlayers.size() == 1) {
                // 只有一名玩家，该玩家获胜
                Player winner = alivePlayers.get(0);
                Utils.castTitle("§6§l胜利！", "§e" + winner.getName() + " 赢得了比赛", 10, 40, 10, mapName);
                Utils.castSound(Sound.LEVEL_UP, 1, 1, mapName);
            } else if (playerCount == 0) {
                Utils.castMessage("§c所有玩家都离开了游戏！", mapName);
            }
            state.put(mapName, ArenaState.ENDING);
            Bukkit.getScheduler().runTaskLater(Duels.getInstance(), () -> resetArena(mapName), 60);
            return;
        }
    }
    // ========== 修改结束 ==========

    // ========== 新增：结束游戏方法 ==========
    private void endGame(String mapName, Team winnerTeam) {
        Utils.castTitle("§6§l胜利！", "", 10, 40, 10, mapName);
        Utils.castSound(Sound.LEVEL_UP, 1, 1, mapName);

        for (Player player : winnerTeam.getPlayers()) {
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
            }
        }

        state.put(mapName, ArenaState.ENDING);
        Bukkit.getScheduler().runTaskLater(Duels.getInstance(), () -> resetArena(mapName), 100);
    }

    // 设置玩家为旁观者
    public static void setSpectator(String mapName, Player player) {
        // 从存活玩家列表中移除
        List<Player> players = arenaPlayers.get(mapName);
        if (players != null) {
            players.remove(player);
        }
        // 添加到旁观者列表
        List<Player> specList = spectators.computeIfAbsent(mapName, k -> new ArrayList<>());
        if (!specList.contains(player)) {
            specList.add(player);
        }
        // 设置旁观者模式
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.1f);
        // 传送到旁观者位置
        Location center = lobbyLoc.get(mapName);
        if (center != null) {
            Location spectatorLoc = center.clone();
            spectatorLoc.setY(center.getY() + 10);
            player.teleport(spectatorLoc);
        }
        TitleUtil.sendTitle(player, 10, 5, 10, "", "§c§l你凉了！");
    }

    // 重置竞技场 - 完全清理所有数据
    private void resetArena(String mapName) {
        // 1. 取消所有相关任务
        cancelAllTasks(mapName);

        // 2. 收集并处理所有玩家
        handleAllPlayers(mapName);

        // 3. 清理所有数据结构
        clearAllData(mapName);

        // 4. 重置基本状态
        resetBasicState(mapName);

        // 5. 清理放置的方块
        clearPlacedBlocks(mapName);

        // ========== 新增：重置队伍 ==========
        Mode gameMode = arenaGameMode.get(mapName);
        if (gameMode != null) {
            initTeams(mapName, gameMode);
        }
        // ========== 新增结束 ==========

        Duels.getInstance().getLogger().info("竞技场 " + mapName + " 已完全重置");
    }

    // 取消所有任务
    private void cancelAllTasks(String mapName) {
        // 取消倒计时任务
        if (countdownTasks.containsKey(mapName)) {
            BukkitRunnable task = countdownTasks.get(mapName);
            if (task != null) {
                task.cancel();
            }
            countdownTasks.remove(mapName);
        }

        // 取消游戏时间任务
        if (gameTimeTasks.containsKey(mapName)) {
            BukkitRunnable task = gameTimeTasks.get(mapName);
            if (task != null) {
                task.cancel();
            }
            gameTimeTasks.remove(mapName);
        }
    }

    // 处理所有玩家
    private void handleAllPlayers(String mapName) {
        List<Player> allPlayers = new ArrayList<>();

        // 收集存活玩家
        List<Player> players = arenaPlayers.get(mapName);
        if (players != null) {
            allPlayers.addAll(players);
        }

        // 收集旁观者
        List<Player> specList = spectators.get(mapName);
        if (specList != null) {
            allPlayers.addAll(specList);
        }

        // 获取全局大厅位置
        Location lobbyLocation = lobbyLoc.get("Lobby");
        if (lobbyLocation == null) {
            lobbyLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        }

        // 处理每个玩家
        for (Player player : allPlayers) {
            if (player != null && player.isOnline()) {
                // 传送回大厅
                player.teleport(lobbyLocation);
                // 清理玩家状态
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
                lobbyPlayers.put("Lobby", player);
                NameTagUtils.INSTANCE.clearAll(player);
                // 移除竞技场映射
                arenaMap.remove(player);
                // ========== 新增：清除队伍映射 ==========
                playerTeam.remove(player);
                // ========== 新增结束 ==========
            }
        }
    }

    // 清理所有数据结构
    private void clearAllData(String mapName) {
        // 清空存活玩家列表
        List<Player> players = arenaPlayers.get(mapName);
        if (players != null) {
            players.clear();
        }

        // 清空旁观者列表
        List<Player> specList = spectators.get(mapName);
        if (specList != null) {
            specList.clear();
        }

        // 清空放置的方块记录
        HashMap<Location, Block> blocks = placedBlocks.get(mapName);
        if (blocks != null) {
            blocks.clear();
        }

        // ========== 新增：清空队伍玩家 ==========
        List<Team> teams = arenaTeams.get(mapName);
        if (teams != null) {
            for (Team team : teams) {
                team.clear();
            }
        }
        // ========== 新增结束 ==========

        // 移除玩家映射中属于这个地图的玩家
        arenaMap.entrySet().removeIf(entry -> mapName.equals(entry.getValue()));
    }

    // 重置基本状态
    private void resetBasicState(String mapName) {
        countdown.put(mapName, 15);
        state.put(mapName, ArenaState.WAITING);
        gameTimes.put(mapName, 9 * 60);

        // 确保数据结构存在且为空
        arenaPlayers.put(mapName, new ArrayList<>());
        spectators.put(mapName, new ArrayList<>());
        placedBlocks.put(mapName, new HashMap<>());
    }

    // 清理放置的方块
    private void clearPlacedBlocks(String mapName) {
        HashMap<Location, Block> blocks = placedBlocks.get(mapName);
        if (blocks != null && !blocks.isEmpty()) {
            for (Location loc : blocks.keySet()) {
                if (loc != null && loc.getBlock() != null) {
                    loc.getBlock().setType(Material.AIR);
                }
            }
            blocks.clear();
        }
    }
}