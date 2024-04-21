package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars.utils.ActionBarUtil;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBar {
    static int refreshTicks;
    static String actionBar_waiting;
    static String actionBar_playing;
    static List<String> tips = new ArrayList<>();
    static ConcurrentHashMap<IArena, Boolean> arenas = new ConcurrentHashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        refreshTicks = cfg.getInt("refreshTicks");
        actionBar_waiting = cfg.getString("actionBar.waiting");
        actionBar_playing = cfg.getString("actionBar.playing");
        tips = cfg.getStringList("tips");
    }

    public static void initGame(IArena arena, boolean check) {
        if (check && arenas.containsKey(arena)) {
            return;
        }
        switch (arena.getStatus()) {
            case waiting:
            case playing:
                arenas.put(arena, false);
                break;
            default:
                arenas.remove(arena);
        }
    }

    static int actionBarTicks = 0;

    public static void sendActionBar() {
        actionBarTicks++;
        if (actionBarTicks > refreshTicks) {
            actionBarTicks = 0;
            for (IArena arena : arenas.keySet()) {
                if (!arenas.get(arena)) {
                    sendActionBarToPlayers(arena, getRandomTip());
                    if (arena.getStatus() == GameState.playing) {
                        arenas.put(arena, true);
                    }
                } else {
                    sendActionBarToPlayers(arena, arena.getStatus() == GameState.waiting ? actionBar_waiting : actionBar_playing);
                }
            }
        }
    }

    public static void sendActionBarToPlayers(IArena arena, String msg) {
        for (ITeam team : arena.getTeams()) {
            for (Player player : team.getMembers()) {
                if (!arena.isReSpawning(player) && !arena.isSpectator(player)) {
                    ActionBarUtil.send(player, PlaceholderUtil.formatText(player, arena, team, msg));
                }
            }
        }
    }

    private static String getRandomTip() {
        if (tips.isEmpty()) {
            return "";
        }
        return tips.get(new Random().nextInt(tips.size()));
    }
}
