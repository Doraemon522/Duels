package cn.thegoodboys.duels.commands;

import cn.thegoodboys.duels.Duels;
import cn.thegoodboys.duels.arena.Arena;
import cn.thegoodboys.duels.arena.ArenaMode;
import cn.thegoodboys.utils.util.chat.CC;
import cn.thegoodboys.utils.util.config.FileConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommands extends Command {

    public AdminCommands() {
        super("duels");
    }

    private FileConfig gameConfig;

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player p = ((Player) commandSender).getPlayer();
            if (!p.isOp()) {
                CC.send(p, "&c你没有权限执行这个命令");
                return false;
            }
            if (strings.length == 0) {
                CC.send(p, "&c无效参数");
                return false;
            }
            if (strings[0].equalsIgnoreCase("create")) {
                if (strings.length == 2) {
                    gameConfig = new FileConfig(Duels.getInstance(), strings[1] + ".yml");
                    gameConfig.set("Map",strings[1]);
                    gameConfig.save();
                    CC.send(p, "&a成功创建游戏");
                    return true;
                }
            }
            if (strings[0].equalsIgnoreCase("setlobby")) {
                if (gameConfig == null) {
                    CC.send(p, "§c请先创建竞技场！");
                    return false;
                }
                gameConfig.setLocation(p.getLocation(), "Lobby");
                gameConfig.save();
                CC.send(p, "&a成功设置Lobby");
                return true;
            }
            if (strings[0].equalsIgnoreCase("setspawn1")) {
                if (gameConfig == null) {
                    CC.send(p, "&c请先创建竞技场！");
                    return false;
                }
                gameConfig.setLocation(p.getLocation(), "Spawn1");
                gameConfig.save();
                CC.send(p, "&a成功设置Spawn1");
                return true;
            }
            if (strings[0].equalsIgnoreCase("setspawn2")) {
                if (gameConfig == null) {
                    CC.send(p, "&c请先创建竞技场！");
                    return false;
                }
                gameConfig.setLocation(p.getLocation(), "Spawn2");
                gameConfig.save();
                CC.send(p, "&a成功设置Spawn2");
                return true;
            }
            if (strings[0].equalsIgnoreCase("setmode")) {
                if (strings.length == 2) {
                    ArenaMode arenaMode;
                    try {
                        arenaMode = ArenaMode.valueOf(strings[1].toUpperCase());
                    }catch (IllegalArgumentException e){
                        CC.send(p, "&c无效的模式参数");
                        return false;
                    }
                    gameConfig.set("Mode",arenaMode.name());
                    gameConfig.save();
                    CC.send(p,"&a成功设置竞技场模式！");
                }
            }
            if (strings[0].equalsIgnoreCase("join")) {
                if (strings.length == 2) {
                    String mapName = strings[1];
                    FileConfig gameConfig = new FileConfig(Duels.getInstance(), mapName + ".yml");
                    if (gameConfig.getString("Mode") == null ||gameConfig.getLocation("Lobby") == null || gameConfig.getLocation("Spawn1") == null || gameConfig.getLocation("Spawn2") == null) {
                        CC.send(p, "§c地图配置不完整！");
                        return false;
                    }
                    Duels.getInstance().getArena().onJoin(mapName,p);
                    return true;
                }
            }
            if (strings[0].equalsIgnoreCase("leave")) {
                if (strings.length == 2) {
                    String mapName = strings[1];
                    Duels.getInstance().getArena().onLeave(mapName, p);
                    return true;
                }
            }
        }
        return false;
    }
}
