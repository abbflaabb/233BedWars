package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DalaoWarning {
    static String flag;
    static Integer averageLevelDifference;
    static Integer highLevelRange;
    static List<String> warningMsg;

    public static void loadConfig(YamlConfiguration cfg) {
        flag = cfg.getString("flag");
        averageLevelDifference = cfg.getInt("averageLevelDifference");
        highLevelRange = cfg.getInt("highLevelRange");
        warningMsg = cfg.getStringList("warningMsg");
    }

    public static void initGame(IArena arena) {
        ITeam eliteTeam = getEliteTeam(arena);
        if (eliteTeam != null) {
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> sendMsg(arena, eliteTeam), 20L);
        }
    }

    private static ITeam getEliteTeam(IArena arena) {
        switch (flag) {
            case "average":
                Map<Integer, ITeam> avgLevelMap = new HashMap<>();
                for (ITeam team : arena.getTeams()) {
                    avgLevelMap.put(getAverageLevel(team), team);
                }
                if (avgLevelMap.size() >= 2) {
                    List<Integer> sortedKeys = avgLevelMap.keySet().stream()
                            .sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList());
                    Integer firstKey = sortedKeys.get(0);
                    Integer secondKey = sortedKeys.get(1);
                    int diff = firstKey - secondKey;
                    if (diff > averageLevelDifference) {
                        return avgLevelMap.get(firstKey);
                    }
                }
                break;
            case "highLevel":
                Map<Integer, ITeam> levelMap = new HashMap<>();
                for (ITeam team : arena.getTeams()) {
                    levelMap.put(getHighestLevel(team), team);
                }
                List<Integer> sortedKeys = levelMap.keySet().stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
                Integer firstKey = sortedKeys.get(0);
                if (firstKey > highLevelRange) {
                    return levelMap.get(firstKey);
                }
                break;
            case "both":
            default:
                Map<Integer, ITeam> avgMap = new HashMap<>();
                for (ITeam team : arena.getTeams()) {
                    avgMap.put(getAverageLevel(team), team);
                }
                List<Integer> sortedAvgKeys = avgMap.keySet().stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
                if (sortedAvgKeys.size() <= 1) {
                    return null;
                }
                Integer avg_first = sortedAvgKeys.get(0);
                Integer avg_second = sortedAvgKeys.get(1);
                int diff = avg_first - avg_second;
                if (diff > averageLevelDifference) {
                    Map<Integer, ITeam> highLevelMap = new HashMap<>();
                    for (ITeam team : arena.getTeams()) {
                        highLevelMap.put(getHighestLevel(team), team);
                    }
                    List<Integer> sortedHighKeys = highLevelMap.keySet().stream()
                            .sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList());
                    Integer level_first = sortedHighKeys.get(0);
                    if (level_first > highLevelRange) {
                        ITeam avgHighest = avgMap.get(avg_first);
                        ITeam levelHighest = highLevelMap.get(level_first);
                        if (avgHighest.equals(levelHighest)) {
                            return avgHighest;
                        }
                    }
                }
                break;
        }
        return null;
    }

    private static Integer getHighestLevel(ITeam team) {
        if (team.getMembers().isEmpty()) {
            return 0;
        }
        int highest = 0;
        for (Player player : team.getMembers()) {
            int level = ProviderUtil.bw.getLevelsUtil().getPlayerLevel(player);
            if (level > highest) {
                highest = level;
            }
        }
        return highest;
    }

    private static Integer getAverageLevel(ITeam team) {
        if (team.getMembers().isEmpty()) {
            return 0;
        }
        int totalLevel = 0;
        for (Player player : team.getMembers()) {
            totalLevel += ProviderUtil.bw.getLevelsUtil().getPlayerLevel(player);
        }
        return totalLevel / team.getMembers().size();
    }

    private static void sendMsg(IArena arena, ITeam dalao) {
        for (Player player : arena.getPlayers()) {
            for (String s : warningMsg) {
                player.sendMessage(s
                        .replace("&", "§")
                        .replace("{tColor}", PlaceholderUtil.getTeamColor(dalao))
                        .replace("{tName}", PlaceholderUtil.getTeamName(dalao, player)));
            }
        }
    }
}
