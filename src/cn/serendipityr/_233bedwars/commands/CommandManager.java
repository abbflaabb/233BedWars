package cn.serendipityr._233bedwars.commands;

import cn.serendipityr._233bedwars.commands.sub.FastCommands;
import cn.serendipityr._233bedwars.commands.sub.Help;
import cn.serendipityr._233bedwars.commands.sub.Reload;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {
    public void regCommands() {
        Bukkit.getPluginCommand("233bw").setExecutor(this);
        Bukkit.getPluginCommand("233bedwars").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0 || strings[0].equals("help")) {
            Help.processCmd(commandSender, strings);
            return true;
        } else {
            if ("reload".equals(strings[0])) {
                Reload.processCmd(commandSender, strings);
                return true;
            }
            if ("fastcommands".equals(strings[0])) {
                FastCommands.processCmd(commandSender, strings);
                return true;
            }
        }
        return true;
    }

    public static void sendMsg(CommandSender target, String text) {
        target.sendMessage(text.replace("&", "§"));
    }
}
