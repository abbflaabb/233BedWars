package cn.serendipityr._233bedwars.utils;

import me.devtec.shared.components.Component;
import me.devtec.theapi.bukkit.nms.NmsProvider;
import org.bukkit.entity.Player;

public class ActionBarUtil {
    public static void send(Player player, String message) {
        Object packet = ProviderUtil.nms.packetTitle(NmsProvider.TitleAction.ACTIONBAR, new Component(message));
        ProviderUtil.packetHandler.send(player, packet);
    }
}