package cn.serendipityr._233bedwars.utils;

import org.bukkit.Bukkit;

public class LogUtil {
    public static void consoleLog(String text) {
        Bukkit.getConsoleSender().sendMessage(text.replace("&", "§"));
    }
}
