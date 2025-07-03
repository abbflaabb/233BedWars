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
        Entity victim_entity = event.getEntity();
        if (event.isCancelled()) {
            return;
        }
        if (victim_entity instanceof Player) {
            Player victim = (Player) event.getEntity();
            Entity damager_entity = event.getDamager();
            if (damager_entity != null) {
                if (damager_entity instanceof Player) {
                    Player damager = (Player) event.getDamager();
                    IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(damager);
                    if (arena != null && arena.getStatus() == GameState.playing && arena.isPlayer(damager) && !arena.isReSpawning(damager)) {
                        if (arena.getTeam(damager) != arena.getTeam(victim)) {
                            if (ConfigManager.addon_combatDetails) {
                                CombatDetails.sendDamageMsg(damager, victim, event.getDamage(), event.getFinalDamage(), false);
                                CombatDetails.checkStrengthEffect(damager, victim);
                            }
                            if (ConfigManager.addon_xpResMode) {
                                XpResMode.handlePlayerDamage(arena, damager, victim, event.getFinalDamage());
                            }
                        }
                    }
                }
                if (damager_entity instanceof Arrow) {
                    Arrow arrow = (Arrow) damager_entity;
                    if (arrow.getShooter() instanceof Player) {
                        Player shooter = (Player) arrow.getShooter();
                        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(shooter);
                        if (arena != null && arena.getStatus() == GameState.playing && arena.isPlayer(shooter) && !arena.isReSpawning(shooter)) {
                            if (arena.getTeam(shooter) != arena.getTeam(victim)) {
                                if (ConfigManager.addon_combatDetails) {
                                    CombatDetails.sendDamageMsg(shooter, victim, event.getDamage(), event.getFinalDamage(), true);
                                    CombatDetails.checkStrengthEffect(shooter, victim);
                                }
                                if (ConfigManager.addon_xpResMode) {
                                    XpResMode.handlePlayerDamage(arena, shooter, victim, event.getFinalDamage());
                                }
                            }
                        }
                    }
                }
            }
            if (ConfigManager.addon_shopItemAddon) {
                if (SuicideBomber.settings_suicide_bomber_enable) {
                    SuicideBomber.handlePlayerDamage(victim);
                }
                if (ToxicBall.settings_toxic_ball_enable) {
                    if (damager_entity instanceof Projectile) {
                        ToxicBall.onProjectileHit((Projectile) damager_entity, victim);
                    }
                }
            }
        }
        if (victim_entity instanceof Item) {
            Entity damager_entity = event.getDamager();
            if (damager_entity != null) {
                if (damager_entity instanceof Fireball || damager_entity instanceof TNTPrimed) {
                    if (GlobalEvents.handleEntityDamageByEntity(damager_entity, victim_entity)) {
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
