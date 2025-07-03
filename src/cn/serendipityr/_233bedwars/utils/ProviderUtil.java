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
        for (Player teamPlayer : team.getMembers()) {
            teamPlayer.sendMessage(msg);
        }
    }

    public static void sendTeamTitle(ITeam team, String title, String subTitle, int stay) {
        for (Player teamPlayer : team.getMembers()) {
            TitleUtil.send(teamPlayer, title, subTitle, 0, stay, 0);
        }
    }

    public static void playTeamSound(ITeam team, Sound sound, float volume, float pitch) {
        for (Player teamPlayer : team.getMembers()) {
            teamPlayer.playSound(teamPlayer.getLocation(), sound, volume, pitch);
        }
    }
}
