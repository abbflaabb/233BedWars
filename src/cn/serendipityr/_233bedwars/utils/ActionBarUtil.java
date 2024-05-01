package cn.serendipityr._233bedwars.utils;

import com.andrei1058.bedwars.BedWars;
import org.bukkit.entity.Player;

public class ActionBarUtil {
    public static void send(Player player, String message) {
        BedWars.nms.playAction(player, message);
    }
}