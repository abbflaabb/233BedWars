package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import com.andrei1058.bedwars.api.BedWars;
import me.devtec.theapi.bukkit.BukkitLoader;
import me.devtec.theapi.bukkit.nms.NmsProvider;
import me.devtec.theapi.bukkit.packetlistener.PacketHandler;
import org.bukkit.Bukkit;

public class ProviderUtil {
    public static BedWars bw;
    public static NmsProvider nms;
    public static PacketHandler<?> packetHandler;

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

    public static boolean getAPIProvider() {
        try {
            nms = BukkitLoader.getNmsProvider();
            packetHandler = BukkitLoader.getPacketHandler();
        } catch (Throwable e) {
            LogUtil.consoleLog("&3 > &c发生致命错误！");
            e.printStackTrace();
            return false;
        }
        return nms != null;
    }
}
