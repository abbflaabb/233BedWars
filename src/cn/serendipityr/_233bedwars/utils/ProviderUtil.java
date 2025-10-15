package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ProviderUtil {
    public static BedWars bw;

    public static Object hookPlugin(String plugin, Class<?> plgClass) {
        if (Bukkit.getPluginManager().getPlugin(plugin) == null) {
            LogUtil.consoleLog("&3 > &c未找到" + plugin + "，插件将被卸载。");
            new _233BedWars().disablePlugin();
            return null;
        } else {
            Object provider;
            try {
                provider = Bukkit.getServicesManager().getRegistration(plgClass).getProvider();
            } catch (Exception var2) {
                LogUtil.consoleLog("&3 > &c发生致命错误！");
                var2.printStackTrace();
                new _233BedWars().disablePlugin();
                return null;
            }

            LogUtil.consoleLog("&3 > &aHooked " + plugin + " in " + plgClass);
            return provider;
        }
    }

    public static void hookPlaceHolderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderUtil.placeholderAPISupport().register();
            LogUtil.consoleLog("&3 > &aHooked PlaceholderAPI.");
        } else {
            LogUtil.consoleLog("&3 > &c未找到PlaceholderAPI。");
        }
    }

    public static String getServerVersion() {
        String packageName = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static void sendGlobalMessage(IArena arena, String msg) {
        for (Player arenaPlayer : arena.getPlayers()) {
            arenaPlayer.sendMessage(msg);
        }
    }

    public static void sendTeamMessage(ITeam team, String msg) {
        if (team == null) {
            return;
        }
        for (Player teamPlayer : team.getMembers()) {
            if (!compareTeam(teamPlayer, team)) {
                continue;
            }
            teamPlayer.sendMessage(msg);
        }
    }

    public static void sendTeamTitle(ITeam team, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (team == null) {
            return;
        }
        for (Player teamPlayer : team.getMembers()) {
            if (!compareTeam(teamPlayer, team)) {
                continue;
            }
            TitleUtil.send(teamPlayer, title, subTitle, fadeIn, stay, fadeOut);
        }
    }

    public static void playTeamSound(ITeam team, Sound sound, float volume, float pitch) {
        if (team == null) {
            return;
        }
        for (Player teamPlayer : team.getMembers()) {
            if (!compareTeam(teamPlayer, team)) {
                continue;
            }
            teamPlayer.playSound(teamPlayer.getLocation(), sound, volume, pitch);
        }
    }

    public static boolean compareTeam(Player player, ITeam team) {
        if (bw.getArenaUtil().isPlaying(player)) {
            return false;
        }
        IArena arena = bw.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) {
            return false;
        }
        return arena.getTeam(player) == team;
    }
}
