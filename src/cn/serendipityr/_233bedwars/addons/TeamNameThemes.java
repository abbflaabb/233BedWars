package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars.utils.LogUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamNameThemes {
    static String applyThemeMsg;
    // 游戏组别(主题名称(团队名称, 对应名称))
    public static HashMap<String, HashMap<String, HashMap<String, String>>> themes = new HashMap<>();
    public static Map<IArena, String> arenaTheme = new HashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        applyThemeMsg = cfg.getString("applyThemeMsg").replace("&", "§");
        for (String section : cfg.getConfigurationSection("teamNameThemes").getKeys(false)) {
            HashMap<String, HashMap<String, String>> _theme = new HashMap<>();
            for (String theme : cfg.getStringList("teamNameThemes." + section)) {
                HashMap<String, String> __theme = new HashMap<>();
                for (String team : theme.split(":")[1].split(",")) {
                    __theme.put(team.split("-")[0], team.split("-")[1]);
                }
                _theme.put(theme.split(":")[0], __theme);
            }
            themes.put(section, _theme);
        }
    }

    public static void initGame(IArena arena) {
        String group = arena.getGroup();
        String arenaName = arena.getArenaName();
        List<String> keysList = new ArrayList<>(themes.get(arena.getGroup()).keySet());
        String randomTheme = keysList.get(new Random().nextInt(keysList.size()));
        arenaTheme.put(arena, randomTheme);
        if (themes.containsKey(group) && !themes.get(group).isEmpty()) {
            for (Player player : arena.getPlayers()) {
                player.sendMessage(applyThemeMsg.replace("{tTheme}", randomTheme));
            }
            LogUtil.consoleLog("&9233BedWars &3&l > &c为游戏 &b" + arenaName + "&7(" + group + ") &c应用了队伍名称主题: &a&l" + randomTheme);
        } else {
            LogUtil.consoleLog("&9233BedWars &3&l > &c没有为游戏 &b" + arenaName + "&7(" + group + ") &c配置正确的队伍名称主题。");
        }
    }
}
