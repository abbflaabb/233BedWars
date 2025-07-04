package cn.serendipityr._233bedwars.addons.globalEvents;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.GlobalEvents;
import cn.serendipityr._233bedwars.utils.MathUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.player.PlayerInvisibilityPotionEvent;
import org.bukkit.Bukkit;
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
    static String messages_potions_give;

    static Random random = new Random();

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
        messages_potions_give = cfg.getString("messages.potions_give").replace("&", "§");
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
                        String[] effect = getRandomEffect(random);
                        PotionEffect potion = getPotionEffect(effect);
                        for (Player p : arena.getPlayers()) {
                            if (arena.getRespawnSessions().containsKey(p)) {
                                deathKeepMap.put(p, List.of(potion));
                            } else {
                                p.addPotionEffect(potion, true);
                                callInvisibilityEvent(arena, p, potion);
                            }
                            if (messages_potions_give.trim().isEmpty()) {
                                continue;
                            }
                            p.sendMessage(messages_potions_give
                                    .replace("{effect_name}", effect[3])
                                    .replace("{effect_level}", MathUtil.intToRoman(Integer.parseInt(effect[1]) + 1))
                            );
                        }
                    } else {
                        for (Player p : arena.getPlayers()) {
                            String[] effect = getRandomEffect(random);
                            PotionEffect potion = getPotionEffect(effect);
                            if (arena.getRespawnSessions().containsKey(p)) {
                                deathKeepMap.put(p, List.of(potion));
                            } else {
                                p.addPotionEffect(potion, true);
                                callInvisibilityEvent(arena, p, potion);
                            }
                            if (messages_potions_give.trim().isEmpty()) {
                                continue;
                            }
                            p.sendMessage(messages_potions_give
                                    .replace("{effect_name}", effect[3])
                                    .replace("{effect_level}", MathUtil.intToRoman(Integer.parseInt(effect[1]) + 1))
                            );
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
                        effects.add(new PotionEffect(effect.getType(), Math.max(effect.getDuration() - respawnTime, 0), effect.getAmplifier(), effect.isAmbient()));
                    }
                    deathKeepMap.put(victim, effects);
                }
            }
        }.runTaskLater(_233BedWars.getInstance(), 5L);
    }

    public static void handlePlayerRespawn(IArena arena, Player player) {
        if (deathKeepMap.containsKey(player)) {
            for (PotionEffect potion : deathKeepMap.get(player)) {
                player.addPotionEffect(potion, true);
                callInvisibilityEvent(arena, player, potion);
            }
            deathKeepMap.remove(player);
        }
    }

    private static String[] getRandomEffect(Random random) {
        return potions.get(random.nextInt(potions.size()));
    }

    private static PotionEffect getPotionEffect(String[] potion) {
        return new PotionEffect(PotionEffectType.getByName(potion[0]), interval * 20, Integer.parseInt(potion[1]), Boolean.getBoolean(potion[2]));
    }

    private static void callInvisibilityEvent(IArena arena, Player player, PotionEffect potion) {
        if (potion.getType().equals(PotionEffectType.INVISIBILITY)) {
            ITeam team = arena.getTeam(player);
            for (Player p : arena.getPlayers()) {
                if (!team.isMember(p)) {
                    ProviderUtil.bw.getVersionSupport().hideArmor(player, p);
                }
            }
            arena.getShowTime().put(player, potion.getDuration() / 20);
            Bukkit.getPluginManager().callEvent(new PlayerInvisibilityPotionEvent(PlayerInvisibilityPotionEvent.Type.ADDED, team, player, arena));
        }
    }
}
