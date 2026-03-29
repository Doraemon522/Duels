package cn.thegoodboys.duels.team;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String teamName;
    private final List<Player> players;
    private int kills;

    public Team(String teamName) {
        this.teamName = teamName;
        this.players = new ArrayList<>();
        this.kills = 0;
    }

    public String getTeamName() {
        return teamName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public boolean isTeamMate(Player player, Player other) {
        return players.contains(player) && players.contains(other);
    }

    public boolean isAlive() {
        for (Player player : players) {
            if (player != null && player.isOnline() && player.getHealth() > 0) {
                return true;
            }
        }
        return false;
    }

    public List<Player> getAlivePlayers() {
        List<Player> alive = new ArrayList<>();
        for (Player player : players) {
            if (player != null && player.isOnline() && player.getHealth() > 0) {
                alive.add(player);
            }
        }
        return alive;
    }

    public void clear() {
        players.clear();
        kills = 0;
    }
}