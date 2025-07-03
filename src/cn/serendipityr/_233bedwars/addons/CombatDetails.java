package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.*;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CombatDetails {
    static String damageMsg;
    static String damageTitle;
    static String damageSubTitle;
    static String arrowDamageTitle;
    static String arrowDamageSubTitle;
    static List<String[]> healthColor = new ArrayList<>();
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
    static Integer killStreakKeepTime;
    static ConcurrentHashMap<Player, Player> strengthEffectMap = new ConcurrentHashMap<>();
    static HashMap<Integer, String> killStreakMsg = new HashMap<>();
    static ConcurrentHashMap<Player, Integer> killStreak = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Player, Double> killDistance = new ConcurrentHashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        damageMsg = cfg.getString("damageMsg").replace("&", "§");
        damageTitle = cfg.getString("damageTitle").replace("&", "§");
        damageSubTitle = cfg.getString("damageSubtitle").replace("&", "§");
        arrowDamageTitle = cfg.getString("arrowDamageTitle").replace("&", "§");
        arrowDamageSubTitle = cfg.getString("arrowDamageSubtitle").replace("&", "§");
        healthColor.clear();
        for (String color : cfg.getStringList("healthColor")) {
            healthColor.add(color.replace("&", "§").split(":"));
        }
        damageInfo = cfg.getStringList("damageInfo");
        damageInfo.replaceAll(s -> s.replace("&", "§"));
        damageFormat = cfg.getString("damageFormat").replace("&", "§");
        healthFormat = cfg.getString("healthFormat").replace("&", "§");
        extraHealthFormat = cfg.getString("extraHealthFormat").replace("&", "§");
        protectionMax = cfg.getString("protectionMax").replace("&", "§");
        protectionFormat = cfg.getString("protectionFormat").replace("&", "§");
        resistance = cfg.getString("resistance").replace("&", "§");
        resistanceInfo = cfg.getString("resistanceInfo").replace("&", "§");
        regeneration = cfg.getString("regeneration").replace("&", "§");
        regenerationInfo = cfg.getString("regenerationInfo").replace("&", "§");
        death = cfg.getString("death").replace("&", "§");
        strengthEffectHint = cfg.getString("strengthEffectHint").replace("&", "§");
        killStreakKeepTime = cfg.getInt("killStreakKeepTime");
        killStreakMsg.clear();
        for (String a : cfg.getStringList("killStreakMsg")) {
            String[] str = a.replace("&", "§").split("\\|");
            killStreakMsg.put(Integer.parseInt(str[0]), str[1]);
        }

        if (ConfigManager.addon_combatDetails && !(arrowDamageTitle.trim().isEmpty() && arrowDamageSubTitle.trim().isEmpty())) {
            for (Language lang : Language.getLanguages()) {
                lang.getYml().set(Messages.PLAYER_HIT_BOW, "");
            }
        }
    }

    public static void sendDamageMsg(Player damager, Player victim, double damage, double finalDamage, boolean isArrow) {
        double roundedDamage = MathUtil.roundDouble(damage, 1);
        double roundedFinalDamage = MathUtil.roundDouble(finalDamage, 1);
        double roundedReduction = MathUtil.roundDouble(damage - finalDamage, 1);
        double roundedExtraHealth = MathUtil.roundDouble(Math.max(0, getExtraHealth(victim) - damage), 1);
        double roundedHealth = MathUtil.roundDouble(Math.max(0, victim.getHealth() - finalDamage), 1);

        TextComponent msg = new TextComponent(setPlaceHolder(damageMsg, damager, victim, roundedHealth, roundedReduction, roundedDamage, roundedFinalDamage, roundedExtraHealth));

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
            String hoverMsg = setPlaceHolder(m, damager, victim, roundedHealth, roundedReduction, roundedDamage, roundedFinalDamage, roundedExtraHealth);
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

        if (isArrow) {
            if (!(arrowDamageTitle.trim().isEmpty() && arrowDamageSubTitle.trim().isEmpty())) {
                String title = setPlaceHolder(arrowDamageTitle, damager, victim, roundedHealth, roundedReduction, roundedDamage, roundedFinalDamage, roundedExtraHealth);
                String subtitle = setPlaceHolder(arrowDamageSubTitle, damager, victim, roundedHealth, roundedReduction, roundedDamage, roundedFinalDamage, roundedExtraHealth);
                TitleUtil.send(damager, title, subtitle, 0, 20, 0);
            }
        } else {
            if (!(damageTitle.trim().isEmpty() && damageSubTitle.trim().isEmpty())) {
                String title = setPlaceHolder(damageTitle, damager, victim, roundedHealth, roundedReduction, roundedDamage, roundedFinalDamage, roundedExtraHealth);
                String subtitle = setPlaceHolder(damageSubTitle, damager, victim, roundedHealth, roundedReduction, roundedDamage, roundedFinalDamage, roundedExtraHealth);
                TitleUtil.send(damager, title, subtitle, 0, 20, 0);
            }
        }
    }

    public static float getExtraHealth(Player player) {
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + ProviderUtil.getServerVersion() + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            return (float) (Float) handle.getClass().getMethod("getAbsorptionHearts").invoke(handle);
        } catch (Exception e) {
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
                    String msg = killStreakMsg.get(kills).replace("{playerName}", killer.getDisplayName()).replace("&", "§");
                    p.sendMessage(msg);
                    ActionBarUtil.send(p, msg);
                }
            } else {
                Optional<Integer> maxKey = killStreakMsg.keySet().stream().max(Integer::compare);
                maxKey.ifPresent(key -> {
                    for (Player p : arena.getPlayers()) {
                        String msg = killStreakMsg.get(key).replace("{playerName}", killer.getDisplayName()).replace("&", "§");
                        p.sendMessage(msg);
                        ActionBarUtil.send(p, msg);
                    }
                });
            }
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                if (killStreak.get(killer).equals(kills)) {
                    killStreak.remove(killer);
                }
            }, killStreakKeepTime * 20L);
        }
    }

    public static void checkStrengthEffect(Player damager, Player victim) {
        for (PotionEffect potionEffect : victim.getActivePotionEffects()) {
            if (potionEffect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                if (!strengthEffectMap.containsKey(damager) || !strengthEffectMap.get(damager).equals(victim)) {
                    String level = MathUtil.intToRoman(potionEffect.getAmplifier() + 1);
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

    public static void checkPlayerKillDistance(Player killer, Player victim) {
        killDistance.put(victim, MathUtil.roundDouble(killer.getLocation().toVector().distance(victim.getLocation().toVector()), 2));
    }

    private static String getHealthColor(double health) {
        int maxMatched = -1;
        String color = "";

        for (String[] c : healthColor) {
            int threshold = Integer.parseInt(c[0]);
            String colorCode = c[1];

            if (health >= threshold && threshold > maxMatched) {
                maxMatched = threshold;
                color = colorCode;
            }
        }

        return color;
    }

    private static String setPlaceHolder(String text, Player damager, Player victim, double roundedHealth, double roundedReduction, double roundedDamage, double roundedFinalDamage, double roundedExtraHealth) {
        return text
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
                .replace("{health_color}", getHealthColor(roundedHealth))
                .replace("{left_arrows}", String.valueOf(getLeftArrows(damager)))
                .replace("{unicode_square}", "▊")
                .replace("{unicode_heart}", "❤")
                .replace("{unicode_block}", "☲")
                .replace("{unicode_cross}", "✚")
                .replace("{unicode_arrow}", "➹")
                ;
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

    private static int getProtectionLevel(Player player) {
        return ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getTeam(player).getTeamUpgradeTiers().getOrDefault("upgrade-armor", 0);
    }

    private static int getLeftArrows(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) {
                continue;
            }
            if (item.getType().toString().contains("ARROW")) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private static String getDeath(double remain) {
        return (remain <= 0) ? death : "";
    }
}