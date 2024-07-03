package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.CombatDetails;
import cn.serendipityr._233bedwars.addons.shopItems.FlightFirework;
import cn.serendipityr._233bedwars.addons.shopItems.SuicideBomber;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageEventHandler implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (victim instanceof Player) {
            victim = event.getEntity();
            Entity damager = event.getDamager();
            if (damager != null) {
                if (damager instanceof Player) {
                    damager = event.getDamager();
                    IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer((Player) damager);
                    if (arena != null && arena.getStatus() == GameState.playing) {
                        if (arena.getTeam((Player) damager) != arena.getTeam((Player) victim)) {
                            if (ConfigManager.addon_combatDetails) {
                                CombatDetails.sendDamageMsg((Player) damager, (Player) victim, event.getDamage(), event.getFinalDamage());
                                CombatDetails.checkStrengthEffect((Player) damager, (Player) victim);
                            }
                        }
                    }
                }
            }
            if (ConfigManager.addon_shopItemAddon) {
                if (SuicideBomber.settings_suicide_bomber_enable) {
                    SuicideBomber.handlePlayerDamage((Player) victim);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ConfigManager.addon_shopItemAddon) {
                if (FlightFirework.settings_flight_firework_enable) {
                    FlightFirework.onPlayerDamage(player);
                }
            }
        }
    }
}
