package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CombatDetails {
    static String damageMsg;
    static List<String> damageInfo;
    static String damageFormat;
    static String healthFormat;
    static String extraHealthFormat;
    static String protectionFormat;
    static String resistance;
    static String resistanceInfo;
    static String regeneration;
    static String regenerationInfo;
    static String protectionMax;
    static String death;
    static String strengthEffectHint;
    static HashMap<Player, Player> strengthEffectMap = new HashMap<>();
    static HashMap<Integer, String> killStreakMsg = new HashMap<>();
    static HashMap<Player, Integer> killStreak = new HashMap<>();
    static HashMap<Player, Double> killDistance = new HashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        damageMsg = cfg.getString("damageMsg");
        damageInfo = cfg.getStringList("damageInfo");
        damageFormat = cfg.getString("damageFormat");
        healthFormat = cfg.getString("healthFormat");
        extraHealthFormat = cfg.getString("extraHealthFormat");
        protectionMax = cfg.getString("protectionMax");
        protectionFormat = cfg.getString("protectionFormat");
        resistance = cfg.getString("resistance");
        resistanceInfo = cfg.getString("resistanceInfo");
        regeneration = cfg.getString("regeneration");
        regenerationInfo = cfg.getString("regenerationInfo");
        death = cfg.getString("death");
        strengthEffectHint = cfg.getString("strengthEffectHint");
        for (String a : cfg.getStringList("killStreakMsg")) {
            String[] str = a.split("\\|");
            killStreakMsg.put(Integer.parseInt(str[0]), str[1]);
        }
    }

    public static void sendDamageMsg(Player damager, Player victim, double damage, double finalDamage) {
        double roundedDamage = roundDouble(damage, 1);
        double roundedFinalDamage = roundDouble(finalDamage, 1);
        double roundedReduction = roundDouble(damage - finalDamage, 1);
        double roundedExtraHealth = roundDouble(Math.max(0, getExtraHealth(victim) - damage), 1);
        double roundedHealth = roundDouble(Math.max(0, victim.getHealth() - finalDamage), 1);

        TextComponent msg = new TextComponent(damageMsg
                .replace("{formatted_protection}", protectionFormat)
                .replace("{formatted_damage}", damageFormat)
                .replace("{formatted_health}", healthFormat)
                .replace("{formatted_extra_health}", (getExtraHealth(victim) > 0) ? extraHealthFormat : "")
                .replace("{victim}", victim.getDisplayName())
                .replace("{protection_max}", getProtectionMax(victim))
                .replace("{death}", getDeath(roundedHealth))
                .replace("{reduction}", String.valueOf(roundedReduction))
                .replace("{base_damage}", String.valueOf(roundedDamage))
                .replace("{final_damage}", String.valueOf(roundedFinalDamage))
                .replace("{extra_health}", String.valueOf(roundedExtraHealth))
                .replace("{health}", String.valueOf(roundedHealth))
                .replace("{armor_level}", String.valueOf(getProtectionLevel(victim) + 1))
                .replace("{formatted_resistance}", resistanceInfo)
                .replace("{resistance}", getResistance(victim))
                .replace("{resistance_level}", String.valueOf(getEffectLevel(victim, PotionEffectType.DAMAGE_RESISTANCE)))
                .replace("{formatted_regeneration}", regenerationInfo)
                .replace("{regeneration}", getRegeneration(victim))
                .replace("{regeneration_level}", String.valueOf(getEffectLevel(victim, PotionEffectType.REGENERATION)))
                .replace("{unicode_square}", "▊")
                .replace("{unicode_heart}", "❤")
                .replace("{unicode_block}", "☲")
                .replace("{unicode_cross}", "✚")
                .replace("&", "§"));

        // 构建悬浮消息列表
        List<String> hoverMessages = new ArrayList<>();
        for (String m : damageInfo) {
            if (m.equals("{formatted_protection}") && (getProtectionLevel(victim) == 0)) {
                continue;
            }
            if (m.equals("{formatted_extra_health}") && (getExtraHealth(victim) == 0)) {
                continue;
            }
            if (m.equals("{formatted_resistance}") && getEffectLevel(victim, PotionEffectType.DAMAGE_RESISTANCE) == 0) {
                continue;
            }
            if (m.equals("{formatted_regeneration}") && getEffectLevel(victim, PotionEffectType.REGENERATION) == 0) {
                continue;
            }
            String hoverMsg = m
                    .replace("{formatted_protection}", protectionFormat)
                    .replace("{formatted_damage}", damageFormat)
                    .replace("{formatted_health}", healthFormat)
                    .replace("{formatted_extra_health}", (getExtraHealth(victim) > 0) ? extraHealthFormat : "")
                    .replace("{victim}", victim.getDisplayName())
                    .replace("{protection_max}", getProtectionMax(victim))
                    .replace("{death}", getDeath(roundedHealth))
                    .replace("{reduction}", String.valueOf(roundedReduction))
                    .replace("{base_damage}", String.valueOf(roundedDamage))
                    .replace("{final_damage}", String.valueOf(roundedFinalDamage))
                    .replace("{extra_health}", String.valueOf(roundedExtraHealth))
                    .replace("{health}", String.valueOf(roundedHealth))
                    .replace("{armor_level}", String.valueOf(getProtectionLevel(victim) + 1))
                    .replace("{formatted_resistance}", resistanceInfo)
                    .replace("{resistance}", getResistance(victim))
                    .replace("{resistance_level}", String.valueOf(getEffectLevel(victim, PotionEffectType.DAMAGE_RESISTANCE)))
                    .replace("{formatted_regeneration}", regenerationInfo)
                    .replace("{regeneration}", getRegeneration(victim))
                    .replace("{regeneration_level}", String.valueOf(getEffectLevel(victim, PotionEffectType.REGENERATION)))
                    .replace("{unicode_square}", "▊")
                    .replace("{unicode_heart}", "❤")
                    .replace("{unicode_block}", "☲")
                    .replace("{unicode_cross}", "✚")
                    .replace("&", "§");
            hoverMessages.add(hoverMsg);
        }

        // 构建并设置悬浮事件
        ComponentBuilder hoverComponentBuilder = new ComponentBuilder("");
        boolean first = true;
        for (String hoverMsg : hoverMessages) {
            if (!first) {
                hoverComponentBuilder.append("\n"); // 新的一行
            }
            hoverComponentBuilder.append(hoverMsg);
            first = false;
        }
        msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponentBuilder.create()));

        // 发送消息给造成伤害的玩家
        damager.sendMessage(msg);
    }

    private static Double roundDouble(double num, int scale) {
        double rounded = new BigDecimal(num).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        if (num > 0 && rounded == 0) {
            return 0.1D;
        } else {
            return rounded;
        }
    }

    private static String getServerVersion() {
        String packageName = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static float getExtraHealth(Player player) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            return (float) (Float) handle.getClass().getMethod("getAbsorptionHearts").invoke(handle);
        } catch (Exception e) {
            LogUtil.consoleLog("&9233BedWars &3&l > &c发生致命错误！");
            e.printStackTrace();
            return 0.0f;
        }
    }

    public static void calcKillStreak(IArena arena, Player killer, Player victim) {
        if (!killStreak.containsKey(killer)) {
            killStreak.put(killer, 1);
            killStreak.remove(victim);
        } else {
            Integer kills = killStreak.get(killer) + 1;
            killStreak.put(killer, kills);
            if (killStreakMsg.containsKey(kills)) {
                for (Player p : arena.getPlayers()) {
                    p.sendMessage(killStreakMsg.get(kills).replace("{playerName}", killer.getDisplayName()).replace("&", "§"));
                }
            } else {
                Optional<Integer> maxKey = killStreakMsg.keySet().stream().max(Integer::compare);
                maxKey.ifPresent(key -> {
                    for (Player p : arena.getPlayers()) {
                        p.sendMessage(killStreakMsg.get(key).replace("{playerName}", killer.getDisplayName()).replace("&", "§"));
                    }
                });
            }
        }
    }

    public static void checkStrengthEffect(Player damager, Player victim) {
        for (PotionEffect potionEffect : victim.getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                if (!strengthEffectMap.containsKey(damager) || !strengthEffectMap.get(damager).equals(victim)) {
                    String level = intToRoman(potionEffect.getAmplifier() + 1);
                    damager.sendMessage(strengthEffectHint.replace("{level}", level).replace("&", "§"));
                    strengthEffectMap.put(damager, victim);
                    Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                        if (strengthEffectMap.get(damager).equals(victim)) {
                            strengthEffectMap.remove(damager);
                        }
                    }, 300L);
                }
            }
        }
    }

    public static String intToRoman(int num) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D",
                "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L",
                "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V",
                "VI", "VII", "VIII", "IX"};

        return thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

    private static String getProtectionMax(Player player) {
        return (getProtectionLevel(player) == 3) ? protectionMax : "";
    }

    private static String getResistance(Player player) {
        return (getEffectLevel(player, PotionEffectType.DAMAGE_RESISTANCE) > 0) ? resistance : "";
    }

    private static String getRegeneration(Player player) {
        return (getEffectLevel(player, PotionEffectType.REGENERATION) > 0) ? regeneration : "";
    }

    private static int getEffectLevel(Player player, PotionEffectType effect) {
        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            if (potionEffect.getType().equals(effect)) {
                return potionEffect.getAmplifier() + 1;
            }
        }
        return 0;
    }

    public static void checkPlayerKillDistance(Player killer, Player victim) {
        killDistance.put(victim, roundDouble(killer.getLocation().toVector().distance(victim.getLocation().toVector()), 2));
    }

    private static int getProtectionLevel(Player player) {
        return ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getTeam(player).getTeamUpgradeTiers().getOrDefault("upgrade-armor", 0);
    }

    private static String getDeath(double remain) {
        return (remain <= 0) ? death : "";
    }
}