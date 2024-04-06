package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.CombatDetails;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageEventHandler implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (ProviderUtil.bw.getArenaUtil().getArenaByPlayer(damager) != null && ProviderUtil.bw.getArenaUtil().getArenaByPlayer(damager).getStatus() == GameState.playing) {
                Player victim = (Player) event.getEntity();
                if (ConfigManager.addon_combatDetails) {
                    CombatDetails.sendDamageMsg(damager, victim, event.getDamage(), event.getFinalDamage());
                    CombatDetails.checkStrengthEffect(damager, victim);
                }
            }
        }
    }
}
