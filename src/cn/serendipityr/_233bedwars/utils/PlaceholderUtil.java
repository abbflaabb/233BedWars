package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ScoreboardEditor;
import cn.serendipityr._233bedwars.addons.TeamNameThemes;
import cn.serendipityr._233bedwars.config.ConfigManager;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.libs.sidebar.PlaceholderProvider;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlaceholderUtil {
    static class placeholderAPISupport extends PlaceholderExpansion {
        @Override
        @Nonnull
        public String getIdentifier() {
            return "233bw";
        }

        @Override
        @Nonnull
        public String getAuthor() {
            return String.join(", ", _233BedWars.getInstance().getDescription().getAuthors());
        }

        @Override
        @Nonnull
        public String getVersion() {
            return _233BedWars.getInstance().getDescription().getVersion();
        }

        @Override
        public String onRequest(OfflinePlayer player, String params) {
            Player p = player.getPlayer();
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(p);
            ITeam team = arena.getTeam(p);
            try {
                if (params.equalsIgnoreCase("mode")) {
                    return arena.getGroup();
                }

                if (params.equalsIgnoreCase("map")) {
                    return getMapName(arena);
                }

                if (params.equalsIgnoreCase("modeName")) {
                    return getModeName(arena.getGroup());
                }

                if (params.equalsIgnoreCase("teamDesc")) {
                    return getTeamDesc(arena.getGroup());
                }

                if (params.equalsIgnoreCase("author")) {
                    return getMapAuthor(arena.getArenaName());
                }

                if (params.equalsIgnoreCase("resMode")) {
                    return getResMode(p);
                }

                if (params.equalsIgnoreCase("sTime")) {
                    return getCurrentFormattedTime();
                }

                if (params.equalsIgnoreCase("sId")) {
                    return ConfigManager.serverID;
                }

                if (params.equalsIgnoreCase("globalEvent")) {
                    return getGlobalEvent(arena);
                }

                if (params.equalsIgnoreCase("tHeart")) {
                    return getTeamHeart(team).replace("&", "§");
                }

                if (params.equalsIgnoreCase("tColor")) {
                    return getTeamColor(team).replace("&", "§");
                }

                if (params.equalsIgnoreCase("tName")) {
                    return getTeamName(team).replace("&", "§");
                }

                if (params.equalsIgnoreCase("tAlive")) {
                    return String.valueOf(getTeamAlive(team));
                }

                if (params.contains("tInfo_")) {
                    String teamName = params.split("_")[1];
                    ITeam _team = arena.getTeam(teamName);
                    return getFormattedTeamInfo(_team).replace("&", "§");
                }
            } catch (Exception e) {
                LogUtil.consoleLog("&9233BedWars &3&l > &c调用PlaceholderAPI时发生错误，可能存在异常的占位符引用。");
                e.printStackTrace();
                return "发生错误";
            }
            return null;
        }
    }

    public static void hookPlaceHolderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new placeholderAPISupport().register();
            LogUtil.consoleLog("&3 > &aHooked PlaceholderAPI.");
        } else {
            ScoreboardEditor.useNativePlaceHolder = true;
            LogUtil.consoleLog("&3 > &c未找到PlaceholderAPI。");
        }
    }

    public static void addNativeScoreBoardPlaceHolders(Player player) {
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        ITeam team = arena.getTeam(player);
        ScoreBoardUtil.addPlaceHolder(player, "{modeName}", () -> getModeName(arena.getGroup()));
        ScoreBoardUtil.addPlaceHolder(player, "{teamDesc}", () -> getTeamDesc(arena.getGroup()));
        ScoreBoardUtil.addPlaceHolder(player, "{map}", () -> getMapName(arena));
        ScoreBoardUtil.addPlaceHolder(player, "{author}", () -> getMapAuthor(arena.getArenaName()));
        ScoreBoardUtil.addPlaceHolder(player, "{resMode}", () -> getResMode(player));
        ScoreBoardUtil.addPlaceHolder(player, "{globalEvent}", () -> getGlobalEvent(arena));
        ScoreBoardUtil.addPlaceHolder(player, "{mode}", () -> ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getGroup());
        ScoreBoardUtil.addPlaceHolder(player, "{sTime}", PlaceholderUtil::getCurrentFormattedTime);
        ScoreBoardUtil.addPlaceHolder(player, "{sId}", () -> ConfigManager.serverID);
        ScoreBoardUtil.addPlaceHolder(player, "{tHeart}", () -> getTeamHeart(team));
        ScoreBoardUtil.addPlaceHolder(player, "{tColor}", () -> getTeamColor(team));
        ScoreBoardUtil.addPlaceHolder(player, "{tName}", () -> getTeamName(team));
        ScoreBoardUtil.addPlaceHolder(player, "{tAlive}", () -> String.valueOf(getTeamAlive(team)));
        for (ITeam _team : arena.getTeams()) {
            ScoreBoardUtil.addPlaceHolder(player, "{tInfo_" + _team.getName() + "}", () -> getFormattedTeamInfo(_team));
        }
    }

    public static void addScoreBoardPlaceHolders(Player player, List<String> lines) {
        Collection<PlaceholderProvider> nativePlaceHolders = ScoreBoardUtil.getNativePlaceHolders(player);
        // 转换为Set提高查找效率
        Set<String> nativePlaceHolderNames = nativePlaceHolders.stream()
                .map(PlaceholderProvider::getPlaceholder)
                .collect(Collectors.toSet());

        // 直接在原始列表上操作，替换占位符
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            List<String> placeholders = extractFields(line);

            for (String p : placeholders) {
                // 如果占位符不在nativePlaceHolders中，则替换
                if (!nativePlaceHolderNames.contains("{" + p + "}")) {
                    line = line.replace("{" + p + "}", "%233bw_" + p + "%");
                }
            }
            lines.set(i, line); // 更新行
        }
    }

    public static List<String> extractFields(String input) {
        List<String> fields = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^}]*)}"); // 匹配 {xxx} 格式的正则表达式
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            fields.add(matcher.group(1)); // 添加匹配到的字段（不包含花括号）
        }

        return fields;
    }

    public static String getModeName(String mode) {
        return ScoreboardEditor.modeName.getOrDefault(mode, mode);
    }

    public static String getTeamDesc(String mode) {
        return ScoreboardEditor.teamDesc.getOrDefault(mode, mode);
    }

    public static String getMapName(IArena arena) {
        return arena.getDisplayName();
    }

    public static String getMapAuthor(String map) {
        return ScoreboardEditor.mapAuthor.getOrDefault(map, ScoreboardEditor.defaultMapAuthor);
    }

    public static String getResMode(Player player) {
        return "开发中";
    }

    public static String getGlobalEvent(IArena arena) {
        return "开发中";
    }

    public static String getTeamName(ITeam team) {
        IArena arena = team.getArena();
        return TeamNameThemes.themes.get(arena.getGroup()).get(TeamNameThemes.arenaTheme.get(arena)).getOrDefault(team.getName(), team.getName());
    }

    public static String getTeamHeart(ITeam team) {
        return team.isBedDestroyed() ? "&7❤" : "&c❤";
    }

    public static Integer getTeamAlive(ITeam team) {
        return team.getMembers().size();
    }

    public static String getTeamColor(ITeam team) {
        return team.getColor().chat().toString();
    }

    public static String getFormattedTeamInfo(ITeam team) {
        return ScoreboardEditor.teamNameFormat.replace("{tColor}", getTeamColor(team)).replace("{tName}", getTeamName(team)).replace("{tHeart}", getTeamHeart(team)).replace("{tAlive}", String.valueOf(getTeamAlive(team)));
    }

    public static String getCurrentFormattedTime() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        // 格式化当前时间
        return now.format(formatter);
    }
}
