package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars.config.ConfigManager;
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
            case starting:
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
        if (!ConfigManager.addon_actionBar) {
            arenas.clear();
            actionBarTicks = 0;
            return;
        }
        actionBarTicks++;
        if (actionBarTicks > refreshTicks) {
            actionBarTicks = 0;
            for (IArena arena : arenas.keySet()) {
                if (!arenas.get(arena)) {
                    if (arena.getStatus() == GameState.playing) {
                        sendActionBarToPlayers(arena, getRandomTip(), false);
                        arenas.put(arena, true);
                        continue;
                    }
                    sendActionBarToPlayers(arena, actionBar_waiting, true);
                } else {
                    sendActionBarToPlayers(arena, actionBar_playing, false);
                }
            }
        }
    }

    public static void sendActionBarToPlayers(IArena arena, String msg, boolean waiting) {
        if (waiting) {
            for (Player player : arena.getPlayers()) {
                if (!arena.isReSpawning(player) && !arena.isSpectator(player)) {
                    ActionBarUtil.send(player, PlaceholderUtil.formatText(player, arena, null, msg));
                }
            }
        } else {
            for (ITeam team : arena.getTeams()) {
                for (Player player : team.getMembers()) {
                    if (!arena.isReSpawning(player) && !arena.isSpectator(player)) {
                        ActionBarUtil.send(player, PlaceholderUtil.formatText(player, arena, team, msg));
                    }
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
