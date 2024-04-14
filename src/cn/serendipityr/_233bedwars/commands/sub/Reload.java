package cn.serendipityr._233bedwars.commands.sub;

import cn.serendipityr._233bedwars.commands.CommandManager;
import cn.serendipityr._233bedwars.config.ConfigManager;
import org.bukkit.command.CommandSender;

public class Reload {
    public static void processCmd(CommandSender commandSender, String[] strings) {
        if (commandSender.hasPermission("bw233.admin")) {
            CommandManager.sendMsg(commandSender, "&9233BedWars &3&l> &e插件重载中...");
            ConfigManager.loadConfig();
            CommandManager.sendMsg(commandSender, "&9233BedWars &3&l> &a插件重载完成。");
        } else {
            CommandManager.sendMsg(commandSender, "&9233BedWars &3&l> &c你没有权限。");
        }
    }
}
