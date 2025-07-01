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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
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
                            p.addPotionEffect(potion);
                        }
                    } else {
                        for (Player p : arena.getPlayers()) {
                            p.addPotionEffect(getRandomEffect());
                        }
                    }
                }
            }
        }.runTaskTimer(_233BedWars.getInstance(), 0L, 20L);
    }

    public static void handlePlayerDamage(IArena arena, Player victim, double finalDamage) {
        if (!death_keep) {
            return;
        }
        double roundedHealth = roundDouble(Math.max(0, victim.getHealth() - finalDamage), 1);
        if (roundedHealth <= 0) {
            deathKeepMap.put(victim, victim.getActivePotionEffects());
        }
    }

    public static void handlePlayerRespawn(Player player) {
        if (!death_keep) {
            return;
        }
        if (deathKeepMap.containsKey(player)) {
            for (PotionEffect potion : deathKeepMap.get(player)) {
                player.addPotionEffect(potion);
            }
        }
    }

    private static PotionEffect getRandomEffect() {
        String[] potion = potions.get(new Random().nextInt(potions.size()));
        return new PotionEffect(PotionEffectType.getByName(potion[0]), interval, Integer.parseInt(potion[1]), Boolean.getBoolean(potion[2]));
    }

    private static Double roundDouble(double num, int scale) {
        double rounded = new BigDecimal(num).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        if (num > 0 && rounded == 0) {
            return 0.1D;
        } else {
            return rounded;
        }
    }
}
