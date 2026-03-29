package cn.thegoodboys.duels;

import cn.thegoodboys.duels.arena.Arena;
import cn.thegoodboys.duels.commands.AdminCommands;
import cn.thegoodboys.duels.listener.ScoreBoard;
import cn.thegoodboys.duels.type.DEBUFF;
import cn.thegoodboys.duels.type.MegaWalls;
import cn.thegoodboys.utils.util.config.FileConfig;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.java.JavaPlugin;

public final class Duels extends JavaPlugin {

    @Getter
    public static Duels instance;

    //config
    @Getter
    private final FileConfig config = new FileConfig(this, "config.yml");

    //debug
    private final boolean isEnabled = config.getBoolean("setup");

    @Getter
    private Arena arena;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        if (isEnabled) {
            registerCommand(new AdminCommands());
        }else {
            registerCommand(new AdminCommands());
            arena = new Arena();
            for (String map : config.getStringList("maps")) {
                arena.init(map);
                switch (Arena.arenaMode.get(map)) {
                    case DEBUFF:
                        new DEBUFF(map).init();
                        break;
                    case MEGAWALLS:
                        new MegaWalls(map).init();
                        break;
                }
            }
            Bukkit.getPluginManager().registerEvents(new ScoreBoard(),this);
        }
    }

    private void registerCommand(Command command) {
        MinecraftServer.getServer().server.getCommandMap().register(command.getName(),getName(),command);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
