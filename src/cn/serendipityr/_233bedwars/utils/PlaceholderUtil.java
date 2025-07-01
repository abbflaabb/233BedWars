package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.GlobalEvents;
import cn.serendipityr._233bedwars.addons.TeamNameThemes;
import cn.serendipityr._233bedwars.addons.XpResMode;
import cn.serendipityr._233bedwars.config.ConfigManager;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlaceholderUtil {
    static int team_heart_animation_ticks;
    static List<String> team_heart_danger_animation = new ArrayList<>();
    static double team_heart_danger_search_radius;
    static String team_heart_normal;
    static String team_heart_destroyed;
    static String team_in_danger;
    static String team_in_danger_full;
    static String teamNameFormat;
    static String resMode_normal;
    static String resMode_exp;
    static Map<String, String> modeName = new HashMap<>();
    static Map<String, String> teamDesc = new HashMap<>();
    static Map<String, String> mapAuthor = new HashMap<>();
    static String defaultMapAuthor;

    public static void loadConfig(YamlConfiguration cfg) {
        team_heart_animation_ticks = cfg.getInt("team_heart_animation_ticks");
        team_heart_danger_search_radius = cfg.getDouble("team_heart_danger_search_radius");
        team_heart_danger_animation = cfg.getStringList("team_heart_danger_animation");
        team_heart_danger_animation.replaceAll(PlaceholderUtil::formatTextUnicode);
        team_heart_destroyed = formatTextUnicode(cfg.getString("team_heart_destroyed"));
        team_heart_normal = formatTextUnicode(cfg.getString("team_heart_normal"));
        teamNameFormat = formatTextUnicode(cfg.getString("teamNameFormat"));
        team_in_danger = formatTextUnicode(cfg.getString("team_in_danger"));
        team_in_danger_full = formatTextUnicode(cfg.getString("team_in_danger_full"));
        for (String mN : cfg.getConfigurationSection("modeName").getKeys(false)) {
            modeName.put(mN, cfg.getString("modeName." + mN));
        }
        for (String tD : cfg.getConfigurationSection("teamDesc").getKeys(false)) {
            teamDesc.put(tD, cfg.getString("teamDesc." + tD));
        }
        for (String mA : cfg.getConfigurationSection("mapAuthor").getKeys(false)) {
            mapAuthor.put(mA, cfg.getString("mapAuthor." + mA));
        }
        defaultMapAuthor = cfg.getString("defaultMapAuthor");
        resMode_normal = cfg.getString("resMode_normal");
        resMode_exp = cfg.getString("resMode_exp");
    }

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
        public String onRequest(OfflinePlayer player, @Nonnull String params) {
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
                    return getTeamName(team, p).replace("&", "§");
                }

                if (params.equalsIgnoreCase("tDanger")) {
                    return getTeamDanger(team).replace("&", "§");
                }

                if (params.equalsIgnoreCase("tDangerF")) {
                    return getTeamDangerFull(team).replace("&", "§");
                }

                if (params.equalsIgnoreCase("tDistance")) {
                    return String.valueOf(getTeamDistance(team, p));
                }

                if (params.equalsIgnoreCase("tIndicator")) {
                    return getTeamIndicator(team, p);
                }

                if (params.equalsIgnoreCase("tAlive")) {
                    return String.valueOf(getTeamAlive(team));
                }

                if (params.contains("tInfo_")) {
                    String teamName = params.split("_")[1];
                    ITeam _team = arena.getTeam(teamName);
                    return getFormattedTeamInfo(_team, p).replace("&", "§");
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
        ScoreBoardUtil.addPlaceHolder(player, "{tDanger}", () -> getTeamDanger(team));
        ScoreBoardUtil.addPlaceHolder(player, "{tDangerF}", () -> getTeamDangerFull(team));
        ScoreBoardUtil.addPlaceHolder(player, "{tColor}", () -> getTeamColor(team));
        ScoreBoardUtil.addPlaceHolder(player, "{tName}", () -> getTeamName(team, player));
        ScoreBoardUtil.addPlaceHolder(player, "{tAlive}", () -> String.valueOf(getTeamAlive(team)));
        ScoreBoardUtil.addPlaceHolder(player, "{tDistance}", () -> String.valueOf(getTeamDistance(team, player)));
        ScoreBoardUtil.addPlaceHolder(player, "{tIndicator}", () -> getTeamIndicator(team, player));
        ScoreBoardUtil.addPlaceHolder(player, "{tAlive}", () -> String.valueOf(getTeamAlive(team)));
        for (ITeam _team : arena.getTeams()) {
            ScoreBoardUtil.addPlaceHolder(player, "{tInfo_" + _team.getName() + "}", () -> getFormattedTeamInfo(_team, player));
        }
    }

    public static String formatText(Player player, IArena arena, ITeam team, String text) {
        text = text.replace("{modeName}", getModeName(arena.getGroup()))
                .replace("{teamDesc}", arena.getGroup())
                .replace("{map}", getMapName(arena))
                .replace("{author}", getMapAuthor(arena.getArenaName()))
                .replace("{resMode}", getResMode(player))
                .replace("{globalEvent}", getGlobalEvent(arena))
                .replace("{mode}", ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getGroup())
                .replace("{sTime}", PlaceholderUtil.getCurrentFormattedTime())
                .replace("{sId}", ConfigManager.serverID);
        text = formatTextUnicode(text);
        if (team != null) {
            text = text.replace("{tHeart}", getTeamHeart(team))
                    .replace("{tDanger}", getTeamDanger(team))
                    .replace("{tDangerF}", getTeamDangerFull(team))
                    .replace("{tColor}", getTeamColor(team))
                    .replace("{tName}", getTeamName(team, player))
                    .replace("{tDistance}", String.valueOf(getTeamDistance(team, player)))
                    .replace("{tIndicator}", getTeamIndicator(team, player))
                    .replace("{tAlive}", String.valueOf(getTeamAlive(team)));
        }
        return text.replace("&", "§");
    }

    public static String formatTextUnicode(String text) {
        return text
                .replace("{unicode_square}", "▊")
                .replace("{unicode_heart}", "❤")
                .replace("{unicode_block}", "☲")
                .replace("{unicode_cross}", "✚")
                .replace("{unicode_arrow}", "➤");
    }

    public static String getModeName(String mode) {
        return modeName.getOrDefault(mode, mode);
    }

    public static String getTeamDesc(String mode) {
        return teamDesc.getOrDefault(mode, mode);
    }

    public static String getMapName(IArena arena) {
        return arena.getDisplayName();
    }

    public static String getMapAuthor(String map) {
        return mapAuthor.getOrDefault(map, defaultMapAuthor);
    }

    public static String getResMode(Player player) {
        return XpResMode.isExpMode(player) ? resMode_exp : resMode_normal;
    }

    public static String getGlobalEvent(IArena arena) {
        return GlobalEvents.getEventInfo(GlobalEvents.getApplyEvent(arena))[0];
    }

    public static String getTeamName(ITeam team, Player player) {
        IArena arena = team.getArena();
        String name = team.getName();
        String displayName = team.getDisplayName(ProviderUtil.bw.getPlayerLanguage(player));
        if (!ConfigManager.addon_teamNameThemes) {
            return displayName;
        }
        if (!TeamNameThemes.themes.containsKey(arena.getGroup()) || !TeamNameThemes.arenaTheme.containsKey(arena)) {
            return displayName;
        }
        if (!TeamNameThemes.themes.get(arena.getGroup()).containsKey(TeamNameThemes.arenaTheme.get(arena))) {
            return displayName;
        }
        return TeamNameThemes.themes.get(arena.getGroup()).get(TeamNameThemes.arenaTheme.get(arena)).getOrDefault(name, displayName);
    }

    static List<ITeam> riskyTeams = new CopyOnWriteArrayList<>();

    public static String getTeamHeart(ITeam team) {
        if (riskyTeams.contains(team)) {
            return riskyTeamHeart;
        }
        return team.isBedDestroyed() ? team_heart_destroyed : team_heart_normal;
    }

    public static Integer getTeamAlive(ITeam team) {
        return team.getMembers().size();
    }

    public static String getTeamColor(ITeam team) {
        return team.getColor().chat().toString();
    }

    public static Double getTeamDistance(ITeam team, Player player) {
        return roundDouble(team.getSpawn().distance(player.getLocation()), 1);
    }

    public static String getTeamIndicator(ITeam team, Player player) {
        // 获取玩家位置和朝向
        Location playerLocation = player.getLocation();
        org.bukkit.util.Vector toTarget = team.getSpawn().toVector().subtract(playerLocation.toVector()).normalize();
        org.bukkit.util.Vector playerDir = playerLocation.getDirection().normalize();

        // 计算方向差
        double dot = playerDir.dot(toTarget);
        Vector crossProduct = playerDir.crossProduct(toTarget);
        double angle = Math.toDegrees(Math.acos(dot));

        // 根据角度和叉积决定方向符号
        String directionSymbol = "↑"; // 默认向前
        if (angle > 157.5 || angle < 22.5) {
            directionSymbol = angle > 157.5 ? "↓" : "↑";
        } else if (angle > 67.5 && angle < 112.5) {
            directionSymbol = crossProduct.getY() > 0 ? "←" : "→";
        } else if (angle >= 22.5 && angle <= 67.5) {
            directionSymbol = crossProduct.getY() > 0 ? "↖" : "↗";
        } else if (angle >= 112.5) {
            directionSymbol = crossProduct.getY() > 0 ? "↙" : "↘";
        }
        return directionSymbol;
    }

    public static String getTeamDanger(ITeam team) {
        return riskyTeams.contains(team) ? team_in_danger : "";
    }

    public static String getTeamDangerFull(ITeam team) {
        return riskyTeams.contains(team) ? team_in_danger_full : "";
    }

    public static String getFormattedTeamInfo(ITeam team, Player player) {
        return teamNameFormat.replace("{tColor}", getTeamColor(team)).replace("{tName}", getTeamName(team, player)).replace("{tHeart}", getTeamHeart(team)).replace("{tAlive}", String.valueOf(getTeamAlive(team)));
    }

    public static String getCurrentFormattedTime() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        // 格式化当前时间
        return now.format(formatter);
    }

    static String riskyTeamHeart = "";
    static int teamHeartTicks = 0;
    static int currentIndex = 0;

    public static void updateTeamHeart() {
        teamHeartTicks++;
        if (teamHeartTicks > team_heart_animation_ticks) {
            teamHeartTicks = 0;
            currentIndex++;
            if (currentIndex >= team_heart_danger_animation.size()) {
                currentIndex = 0;
            }
            checkRiskyTeams();
            riskyTeamHeart = team_heart_danger_animation.get(currentIndex);
        }
    }

    private static void checkRiskyTeams() {
        for (IArena arena : ProviderUtil.bw.getArenaUtil().getArenas()) {
            if (arena.getStatus() != GameState.playing) {
                continue;
            }
            for (ITeam team : arena.getTeams()) {
                if (riskyTeams.contains(team)) {
                    if (team.isBedDestroyed()) {
                        riskyTeams.remove(team);
                        return;
                    }
                    if (!checkRiskyTeam(arena, team)) {
                        riskyTeams.remove(team);
                    }
                } else if (checkRiskyTeam(arena, team)) {
                    riskyTeams.add(team);
                }
            }
        }
    }

    private static boolean checkRiskyTeam(IArena arena, ITeam team) {
        Location bedLoc = team.getBed();
        if (team.isBedDestroyed()) {
            return false;
        }
        boolean risky = false;
        for (Entity entity : arena.getWorld().getNearbyEntities(bedLoc, team_heart_danger_search_radius, team_heart_danger_search_radius, team_heart_danger_search_radius)) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (!arena.isSpectator(player) && !team.isMember(player) && !arena.isReSpawning(player)) {
                    risky = true;
                    break;
                }
            }
        }
        return risky;
    }

    public static void resetArenaRiskyTeams(IArena arena) {
        for (ITeam team : arena.getTeams()) {
            riskyTeams.remove(team);
        }
    }

    private static Double roundDouble(double num, int scale) {
        double rounded = new BigDecimal(num).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        if (num > 0 && rounded == 0) {
            return 0.1D;
        } else {
            return rounded;
        }
    }
}
