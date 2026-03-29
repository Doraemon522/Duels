package cn.thegoodboys.duels.arena;

import org.bukkit.entity.Player;

public interface IArena {

    public void onJoin(String mapName, Player player);

    public void onLeave(String mapName, Player player);

    public void onStart(String mapName);
}
