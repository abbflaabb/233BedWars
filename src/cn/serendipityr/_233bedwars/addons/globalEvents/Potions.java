package cn.serendipityr._233bedwars.addons.globalEvents;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.GlobalEvents;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Potions {
    public static Boolean enable;
    static Integer interval;
    static List<String[]> potions = new ArrayList<>();
    static Boolean share_effect;
    static Boolean death_keep;

    static ConcurrentHashMap<Player, Collection<PotionEffect>> deathKeepMap = new ConcurrentHashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        enable = cfg.getBoolean("events.potions.enable");
        if (enable) {
            GlobalEvents.enable_events.add("potions");
        }
        interval = cfg.getInt("events.potions.interval");
        potions.clear();
        for (String potion : cfg.getStringList("events.potions.potions")) {
            potions.add(potion.split(":"));
        }
        share_effect = cfg.getBoolean("events.potions.share_effect");
        death_keep = cfg.getBoolean("events.potions.death_keep");
    }

    public static void initEvent(IArena arena) {
        new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (arena.getStatus() != GameState.playing) {
                    this.cancel();
                }
                i--;
                if (i <= 0) {
                    i = interval;
                    if (share_effect) {
                        PotionEffect potion = getRandomEffect();
                        for (Player p : arena.getPlayers()) {
                            if (arena.getRespawnSessions().containsKey(p)) {
                                deathKeepMap.put(p, List.of(potion));
                            } else {
                                p.addPotionEffect(potion, true);
                            }
                        }
                    } else {
                        for (Player p : arena.getPlayers()) {
                            PotionEffect potion = getRandomEffect();
                            if (arena.getRespawnSessions().containsKey(p)) {
                                deathKeepMap.put(p, List.of(potion));
                            } else {
                                p.addPotionEffect(potion, true);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(_233BedWars.getInstance(), 0L, 20L);
    }

    public static void handlePlayerDeath(IArena arena, Player victim) {
        if (!"potions".equals(GlobalEvents.getApplyEvent(arena)) || !death_keep) {
            return;
        }
        Collection<PotionEffect> old_effects = victim.getActivePotionEffects();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.getRespawnSessions().containsKey(victim)) {
                    int respawnTime = arena.getRespawnSessions().get(victim) * 20 + 5;
                    Collection<PotionEffect> effects = new ArrayList<>();
                    for (PotionEffect effect : old_effects) {
                        effects.add(new PotionEffect(effect.getType(), effect.getDuration() - respawnTime, effect.getAmplifier(), effect.isAmbient()));
                    }
                    deathKeepMap.put(victim, effects);
                }
            }
        }.runTaskLater(_233BedWars.getInstance(), 5L);
    }

    public static void handlePlayerRespawn(Player player) {
        if (deathKeepMap.containsKey(player)) {
            for (PotionEffect potion : deathKeepMap.get(player)) {
                player.addPotionEffect(potion);
            }
            deathKeepMap.remove(player);
        }
    }

    private static PotionEffect getRandomEffect() {
        String[] potion = potions.get(new Random().nextInt(potions.size()));
        return new PotionEffect(PotionEffectType.getByName(potion[0]), interval * 20, Integer.parseInt(potion[1]), Boolean.getBoolean(potion[2]));
    }
}
