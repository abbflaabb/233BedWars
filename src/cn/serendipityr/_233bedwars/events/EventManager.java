package cn.serendipityr._233bedwars.events;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.events.handler.DamageEventHandler;
import cn.serendipityr._233bedwars.events.handler.GameEventHandler;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import org.bukkit.Bukkit;

public class EventManager {
    public static void regEventHandlers() {
        Bukkit.getPluginManager().registerEvents(new GameEventHandler(), _233BedWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new DamageEventHandler(), _233BedWars.getInstance());
        Bukkit.getPluginManager().registerEvents(new InteractEventHandler(), _233BedWars.getInstance());
    }
}
