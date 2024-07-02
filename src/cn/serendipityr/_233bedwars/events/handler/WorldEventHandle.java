package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;

public class WorldEventHandle implements Listener {
    @EventHandler
    public void onFireworkExplode(FireworkExplodeEvent event) {
        Firework firework = event.getEntity();
        if (ShopItemAddon.handleFireworkExplode(firework)) {
            event.setCancelled(true);
        }
    }
}
