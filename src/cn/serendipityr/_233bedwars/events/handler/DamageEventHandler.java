package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.CombatDetails;
import cn.serendipityr._233bedwars.addons.shopItems.SuicideBomber;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageEventHandler implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Player damager = null;
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            damager = (Player) event.getDamager();
        }
        if (damager != null) {
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(damager);
            if (arena != null && arena.getStatus() == GameState.playing) {
                Player victim = (Player) event.getEntity();
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
