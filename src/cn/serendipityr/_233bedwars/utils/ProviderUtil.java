package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import com.andrei1058.bedwars.api.BedWars;
import org.bukkit.Bukkit;

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
}
