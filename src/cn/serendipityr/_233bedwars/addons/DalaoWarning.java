package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.utils.MathUtil;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendMsg(arena, eliteTeam);
                }
            }.runTaskLaterAsynchronously(_233BedWars.getInstance(), 20L);
        }
    }

    private static ITeam getEliteTeam(IArena arena) {
        switch (flag) {
            case "average":
                Map<Double, ITeam> avgLevelMap = new HashMap<>();
                for (ITeam team : arena.getTeams()) {
                    avgLevelMap.put(getAverageLevel(team), team);
                }
                if (avgLevelMap.size() >= 2) {
                    List<Double> sortedKeys = avgLevelMap.keySet().stream()
                            .sorted(Comparator.reverseOrder())
                            .collect(Collectors.toList());
                    double avg_first = sortedKeys.get(0);
                    double avg_all = sortedKeys.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double diff = avg_first - avg_all;
                    if (diff > averageLevelDifference) {
                        return avgLevelMap.get(avg_first);
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
                Map<Double, ITeam> avgMap = new HashMap<>();
                for (ITeam team : arena.getTeams()) {
                    avgMap.put(getAverageLevel(team), team);
                }
                List<Double> sortedAvgKeys = avgMap.keySet().stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());
                if (sortedAvgKeys.size() <= 1) {
                    return null;
                }
                Double avg_first = sortedAvgKeys.get(0);
                Double avg_all = sortedAvgKeys.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double diff = avg_first - avg_all;
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

    private static Player getHighestLevelPlayer(ITeam team) {
        if (team.getMembers().isEmpty()) {
            return null;
        }
        Player p = null;
        int highest = 0;
        for (Player player : team.getMembers()) {
            int level = ProviderUtil.bw.getLevelsUtil().getPlayerLevel(player);
            if (level > highest) {
                highest = level;
                p = player;
            }
        }
        return p;
    }

    private static Double getAverageLevel(ITeam team) {
        if (team.getMembers().isEmpty()) {
            return 0D;
        }
        double totalLevel = 0;
        for (Player player : team.getMembers()) {
            totalLevel += ProviderUtil.bw.getLevelsUtil().getPlayerLevel(player);
        }
        return MathUtil.roundDouble(totalLevel / team.getMembers().size(), 2);
    }

    private static void sendMsg(IArena arena, ITeam eliteTeam) {
        Player highest_player = getHighestLevelPlayer(eliteTeam);
        for (Player player : arena.getPlayers()) {
            for (String s : warningMsg) {
                player.sendMessage(s
                        .replace("&", "§")
                        .replace("{tColor}", PlaceholderUtil.getTeamColor(eliteTeam))
                        .replace("{tName}", PlaceholderUtil.getTeamName(eliteTeam, player))
                        .replace("{highestLevel}", String.valueOf(getHighestLevel(eliteTeam)))
                        .replace("{highestPlayer}", highest_player == null ? "" : highest_player.getDisplayName())
                        .replace("{avgLevel}", String.valueOf(getAverageLevel(eliteTeam)))
                );
            }
        }
    }
}
