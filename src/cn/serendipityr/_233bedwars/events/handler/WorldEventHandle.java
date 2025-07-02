package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.GlobalEvents;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.addons.shopItems.Grenade;
import cn.serendipityr._233bedwars.config.ConfigManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.List;

public class WorldEventHandle implements Listener {
    @EventHandler
    public void onFireworkExplode(FireworkExplodeEvent event) {
        Firework firework = event.getEntity();
        if (ConfigManager.addon_shopItemAddon && ShopItemAddon.handleFireworkExplode(firework)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (ConfigManager.addon_shopItemAddon) {
            if (Grenade.settings_grenade_enable) {
                Grenade.onProjectileHit(projectile);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        List<Block> blocks = event.blockList();
        if (ConfigManager.addon_globalEvents) {
            GlobalEvents.handleEntityExplode(entity, blocks);
        }
    }
}
