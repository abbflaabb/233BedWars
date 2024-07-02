package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.CombatDetails;
import cn.serendipityr._233bedwars.addons.shopItems.FlightFirework;
import cn.serendipityr._233bedwars.addons.shopItems.SuicideBomber;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageEventHandler implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Player victim = null;
        if (event.getEntity() instanceof Player) {
            victim = (Player) event.getEntity();
        }
        if (victim != null) {
            Player damager = null;
            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            }
            if (damager != null) {
                IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(damager);
                if (arena != null && arena.getStatus() == GameState.playing) {
                    if (arena.getTeam(damager) != arena.getTeam(victim)) {
                        if (ConfigManager.addon_combatDetails) {
                            CombatDetails.sendDamageMsg(damager, victim, event.getDamage(), event.getFinalDamage());
                            CombatDetails.checkStrengthEffect(damager, victim);
                        }

                        if (ConfigManager.addon_shopItemAddon) {
                            if (SuicideBomber.settings_suicide_bomber_enable) {
                                SuicideBomber.handlePlayerDamage(victim);
                            }
                        }
                    }
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
