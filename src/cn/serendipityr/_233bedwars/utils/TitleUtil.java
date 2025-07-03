package cn.serendipityr._233bedwars.utils;

import org.bukkit.entity.Player;

public class TitleUtil {
    public static void send(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title.trim().isEmpty()) {
            if (subtitle.trim().isEmpty()) {
                return;
            }
            title = "§r";
        }
        ProviderUtil.bw.getVersionSupport().sendTitle(p, title, subtitle, fadeIn, stay, fadeOut);
    }
}
