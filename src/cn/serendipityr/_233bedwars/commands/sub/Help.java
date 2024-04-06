package cn.serendipityr._233bedwars.commands.sub;

import cn.serendipityr._233bedwars.commands.CommandManager;
import org.bukkit.command.CommandSender;

public class Help {
    public static void processCmd(CommandSender commandSender, String[] strings) {
        CommandManager.sendMsg(commandSender, "&3&l&m--------&9 233BedWars &fby &6SerendipityR &3&l&m--------");
        CommandManager.sendMsg(commandSender, "&3&l > &e/233bw help &3➽ &a帮助与提示");
        CommandManager.sendMsg(commandSender, "&3&l > &e/233bw reload &3➽ &a重载插件");
        if (!commandSender.hasPermission("bw233.admin")) {
            CommandManager.sendMsg(commandSender, "&3&l > ");
            CommandManager.sendMsg(commandSender, "&3&l > &c你没有权限。");
        }
        CommandManager.sendMsg(commandSender, "&3&l&m---------------------------------------");
    }
}
