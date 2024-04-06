package cn.serendipityr._233bedwars.commands.sub;

import cn.serendipityr._233bedwars.commands.CommandManager;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FastCommands {
    public static void processCmd(CommandSender commandSender, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player) != null) {
                cn.serendipityr._233bedwars.addons.FastCommands.openGUI(player);
            } else {
                CommandManager.sendMsg(commandSender, "&9233BedWars &3&l > &c你只能在游戏中使用此命令。");
            }
        }
    }
}
