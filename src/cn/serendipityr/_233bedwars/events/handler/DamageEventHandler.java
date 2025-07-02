package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.CombatDetails;
import cn.serendipityr._233bedwars.addons.GlobalEvents;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.addons.XpResMode;
import cn.serendipityr._233bedwars.addons.shopItems.FlightFirework;
import cn.serendipityr._233bedwars.addons.shopItems.SuicideBomber;
import cn.serendipityr._233bedwars.addons.shopItems.ToxicBall;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class DamageEventHandler implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (event.isCancelled()) {
            return;
        }
        if (victim instanceof Player) {
            victim = event.getEntity();
            Entity damager = event.getDamager();
            if (damager != null) {
                if (damager instanceof Player) {
                    damager = event.getDamager();
                    IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer((Player) damager);
                    if (arena != null && arena.getStatus() == GameState.playing && arena.isPlayer((Player) damager) && !arena.isReSpawning((Player) damager)) {
                        if (arena.getTeam((Player) damager) != arena.getTeam((Player) victim)) {
                            if (ConfigManager.addon_combatDetails) {
                                CombatDetails.sendDamageMsg((Player) damager, (Player) victim, event.getDamage(), event.getFinalDamage());
                                CombatDetails.checkStrengthEffect((Player) damager, (Player) victim);
                            }
                            if (ConfigManager.addon_xpResMode) {
                                XpResMode.handlePlayerDamage(arena, (Player) damager, (Player) victim, event.getFinalDamage());
                            }
                        }
                    }
                }
            }
            if (ConfigManager.addon_shopItemAddon) {
                if (SuicideBomber.settings_suicide_bomber_enable) {
                    SuicideBomber.handlePlayerDamage((Player) victim);
                }
                if (ToxicBall.settings_toxic_ball_enable) {
                    if (damager instanceof Projectile) {
                        ToxicBall.onProjectileHit((Projectile) damager, (Player) victim);
                    }
                }
            }
        }
        if (victim instanceof Item) {
            Entity damager = event.getDamager();
            if (damager != null) {
                if (damager instanceof Fireball || damager instanceof TNTPrimed) {
                    if (GlobalEvents.handleEntityDamageByEntity(damager, victim)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ConfigManager.addon_shopItemAddon) {
                if (FlightFirework.settings_flight_firework_enable) {
                    FlightFirework.onPlayerDamage(player);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (ConfigManager.addon_shopItemAddon && ShopItemAddon.handleEntityDeath(entity)) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }
}
