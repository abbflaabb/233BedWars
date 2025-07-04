package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.player.PlayerInvisibilityPotionEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BalancedAdjustments {
    static Boolean bed_protect_enable;
    static Integer bed_protect_duration;
    static Boolean bed_protect_only_newbies;
    static Integer bed_protect_newbies_level_max;
    static Integer bed_protect_title_fadeIn;
    static Integer bed_protect_title_fadeStay;
    static Integer bed_protect_title_fadeOut;
    static Boolean team_benefit_enable;
    static String messages_bed_protect_beginning_tips;
    static String messages_bed_protect_beginning_title;
    static String messages_bed_protect_beginning_subtitle;
    static String messages_bed_protect_end_tips;
    static String messages_bed_protect_end_title;
    static String messages_bed_protect_end_subtitle;
    static String messages_bed_protect_bed_break_tips;
    static String messages_bed_protect_newbies_broadcast;
    static String messages_team_benefit_self_team_tips;
    static String messages_team_benefit_self_team_benefit_effects_tips;
    static String messages_team_benefit_self_team_benefit_resources_tips;
    static String messages_team_benefit_self_team_benefit_effects_expired;
    static String messages_team_benefit_benefit_broadcast;

    static HashMap<Integer, ConfigurationSection> benefits = new HashMap<>();
    static List<ITeam> bed_protect_teams = new CopyOnWriteArrayList<>();
    static ConcurrentHashMap<ITeam, Integer> benefit_teams = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Player, Collection<PotionEffect>> deathKeepMap = new ConcurrentHashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        bed_protect_enable = cfg.getBoolean("BeginningBedProtect.enable");
        bed_protect_duration = cfg.getInt("BeginningBedProtect.duration");
        bed_protect_only_newbies = cfg.getBoolean("BeginningBedProtect.only_newbies");
        bed_protect_newbies_level_max = cfg.getInt("BeginningBedProtect.newbies_level_max");
        bed_protect_title_fadeIn = cfg.getInt("BeginningBedProtect.title_settings.fade_in");
        bed_protect_title_fadeStay = cfg.getInt("BeginningBedProtect.title_settings.stay");
        bed_protect_title_fadeOut = cfg.getInt("BeginningBedProtect.title_settings.fade_out");
        team_benefit_enable = cfg.getBoolean("TeamMembersBenefit.enable");

        messages_bed_protect_beginning_tips = cfg.getString("BeginningBedProtect.messages.beginning_tips").replace("&", "§");
        messages_bed_protect_beginning_title = cfg.getString("BeginningBedProtect.messages.beginning_title").replace("&", "§");
        messages_bed_protect_beginning_subtitle = cfg.getString("BeginningBedProtect.messages.beginning_subtitle").replace("&", "§");
        messages_bed_protect_end_tips = cfg.getString("BeginningBedProtect.messages.end_tips").replace("&", "§");
        messages_bed_protect_end_title = cfg.getString("BeginningBedProtect.messages.end_title").replace("&", "§");
        messages_bed_protect_end_subtitle = cfg.getString("BeginningBedProtect.messages.end_subtitle").replace("&", "§");
        messages_bed_protect_bed_break_tips = cfg.getString("BeginningBedProtect.messages.bed_break_tips").replace("&", "§");
        messages_bed_protect_newbies_broadcast = cfg.getString("BeginningBedProtect.messages.newbies_broadcast").replace("&", "§");
        messages_team_benefit_self_team_tips = cfg.getString("TeamMembersBenefit.messages.self_team_tips").replace("&", "§");
        messages_team_benefit_self_team_benefit_effects_tips = cfg.getString("TeamMembersBenefit.messages.self_team_benefit_effects_tips").replace("&", "§");
        messages_team_benefit_self_team_benefit_resources_tips = cfg.getString("TeamMembersBenefit.messages.self_team_benefit_resources_tips").replace("&", "§");
        messages_team_benefit_self_team_benefit_effects_expired = cfg.getString("TeamMembersBenefit.messages.self_team_benefit_effects_expired").replace("&", "§");
        messages_team_benefit_benefit_broadcast = cfg.getString("TeamMembersBenefit.messages.benefit_broadcast").replace("&", "§");

        benefits.clear();
        for (String key : cfg.getConfigurationSection("TeamMembersBenefit.benefits").getKeys(false)) {
            benefits.put(Integer.valueOf(key), cfg.getConfigurationSection("TeamMembersBenefit.benefits." + key));
        }
    }

    public static void initArena(IArena arena) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (bed_protect_enable) {
                    initBedProtect(arena);
                }
                if (team_benefit_enable) {
                    initTeamBenefit(arena);
                }
            }
        }.runTaskLater(_233BedWars.getInstance(), 20L);
    }

    public static void resetArena(IArena arena) {
        arena.getTeams().forEach(team -> {
            bed_protect_teams.remove(team);
            benefit_teams.remove(team);
        });
    }

    public static Boolean handleBedBreak(Player player, Block block) {
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) {
            return false;
        }
        ITeam victim = getBedTeam(arena, block);
        if (victim == null) {
            return false;
        }
        if (bed_protect_enable && bed_protect_teams.contains(victim) && !arena.getTeam(player).equals(victim)) {
            if (!messages_bed_protect_bed_break_tips.trim().isEmpty()) {
                player.sendMessage(messages_bed_protect_bed_break_tips);
            }
            return true;
        }
        return false;
    }

    public static void handlePlayerDeath(IArena arena, Player victim) {
        ITeam team = arena.getTeam(victim);
        if (!benefit_teams.containsKey(team)) {
            return;
        }
        int benefit_level = benefit_teams.get(team);
        ConfigurationSection benefit = benefits.get(benefit_level);
        if (!benefit.getBoolean("effects.death_keep")) {
            return;
        }

        List<String> effect_content = benefit.getStringList("effects.content");
        Collection<PotionEffect> old_effects = victim.getActivePotionEffects();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.getRespawnSessions().containsKey(victim)) {
                    int respawnTime = arena.getRespawnSessions().get(victim) * 20 + 5;
                    Collection<PotionEffect> effects = new ArrayList<>();
                    for (PotionEffect effect : old_effects) {
                        for (String s : effect_content) {
                            String[] _s = s.split(":");
                            if (effect.getType().toString().contains(_s[0])) {
                                effects.add(new PotionEffect(effect.getType(), Math.max(effect.getDuration() - respawnTime, 0), effect.getAmplifier(), effect.isAmbient()));
                                break;
                            }
                        }
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

    private static void initBedProtect(IArena arena) {
        for (ITeam team : arena.getTeams()) {
            if (!bed_protect_only_newbies || isNewbieTeam(team)) {
                bed_protect_teams.add(team);
                if (bed_protect_only_newbies && !messages_bed_protect_newbies_broadcast.trim().isEmpty()) {
                    for (Player player : arena.getPlayers()) {
                        player.sendMessage(messages_bed_protect_newbies_broadcast
                                .replace("{tColor}", PlaceholderUtil.getTeamColor(team))
                                .replace("{tName}", PlaceholderUtil.getTeamName(team, player))
                        );
                    }
                }
                if (!messages_bed_protect_beginning_tips.trim().isEmpty()) {
                    ProviderUtil.sendTeamMessage(team, messages_bed_protect_beginning_tips);
                }
                if (!(messages_bed_protect_beginning_title.trim().isEmpty() && messages_bed_protect_beginning_subtitle.trim().isEmpty())) {
                    ProviderUtil.sendTeamTitle(team, messages_bed_protect_beginning_title, messages_bed_protect_beginning_subtitle, bed_protect_title_fadeIn, bed_protect_title_fadeStay, bed_protect_title_fadeOut);
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        bed_protect_teams.remove(team);
                        if (!messages_bed_protect_beginning_tips.trim().isEmpty()) {
                            ProviderUtil.sendTeamMessage(team, messages_bed_protect_end_tips);
                        }
                        if (!(messages_bed_protect_beginning_title.trim().isEmpty() && messages_bed_protect_beginning_subtitle.trim().isEmpty())) {
                            ProviderUtil.sendTeamTitle(team, messages_bed_protect_end_title, messages_bed_protect_end_subtitle, bed_protect_title_fadeIn, bed_protect_title_fadeStay, bed_protect_title_fadeOut);
                        }
                    }
                }.runTaskLaterAsynchronously(_233BedWars.getInstance(), bed_protect_duration * 20L);
            }
        }
    }

    private static ITeam getBedTeam(IArena arena, Block block) {
        for (ITeam team : arena.getTeams()) {
            if (block.getLocation().toVector().distance(team.getBed().toVector()) <= 2) {
                return team;
            }
        }
        return null;
    }

    private static void initTeamBenefit(IArena arena) {
        for (ITeam team : arena.getTeams()) {
            if (team.getSize() == 0) {
                continue;
            }
            Integer level = getBenefitLevel(arena.getMaxInTeam() - team.getSize(), benefits.keySet());
            if (level == null) {
                continue;
            }
            if (!messages_team_benefit_benefit_broadcast.trim().isEmpty()) {
                for (Player player : arena.getPlayers()) {
                    player.sendMessage(messages_team_benefit_benefit_broadcast
                            .replace("{tColor}", PlaceholderUtil.getTeamColor(team))
                            .replace("{tName}", PlaceholderUtil.getTeamName(team, player))
                            .replace("{benefit_level}", String.valueOf(level))
                    );
                }
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!messages_team_benefit_self_team_tips.trim().isEmpty()) {
                        ProviderUtil.sendTeamMessage(team, messages_team_benefit_self_team_tips.replace("{benefit_level}", String.valueOf(level)));
                    }
                    try {
                        applyBenefit(arena, team, benefits.get(level));
                    } catch (Exception e) {
                        LogUtil.consoleLog("&9233BedWars &3&l> &c无法解析队伍等级补助: (Level: " + level + ")");
                        e.printStackTrace();
                    }
                }
            }.runTaskLater(_233BedWars.getInstance(), 5L);
        }
    }

    private static Boolean isNewbieTeam(ITeam team) {
        for (Player player : team.getMembers()) {
            int level = ProviderUtil.bw.getLevelsUtil().getPlayerLevel(player);
            if (level < bed_protect_newbies_level_max) {
                return true;
            }
        }
        return false;
    }

    private static Integer getBenefitLevel(int n, Set<Integer> levels) {
        Integer result = null;
        for (Integer level : levels) {
            if (level <= n) {
                if (result == null || level > result) {
                    result = level;
                }
            }
        }
        return result;
    }

    private static void applyBenefit(IArena arena, ITeam team, ConfigurationSection section) {
        boolean resources_enable = section.getBoolean("resources.enable");
        int effect_duration = section.getInt("effects.duration");

        for (Player player : team.getMembers()) {
            if (resources_enable) {
                giveResources(player, section);
            }

            if (effect_duration != -1) {
                giveEffects(arena, player, section, effect_duration);
            }
        }
    }

    private static void giveResources(Player player, ConfigurationSection section) {
        String resources_description_normal = section.getString("resources_description.normal").replace("&", "§");
        String resources_description_exp = section.getString("resources_description.exp").replace("&", "§");
        List<ItemStack> items = parseResourceItems(section.getStringList("resources.normal"));
        if (XpResMode.isExpMode(player)) {
            int addExp = section.getInt("resources.exp");
            if (addExp == -1) {
                addExp = 0;
                for (ItemStack itemStack : items) {
                    addExp += XpResMode.calcExpLevel(itemStack.getType(), itemStack.getAmount(), false);
                }
            }
            player.giveExpLevels(addExp);
            if (!messages_team_benefit_self_team_benefit_resources_tips.trim().isEmpty()) {
                player.sendMessage(messages_team_benefit_self_team_benefit_resources_tips
                        .replace("{resources_description}", resources_description_exp)
                        .replace("{exp}", String.valueOf(addExp))
                );
            }
        } else {
            items.forEach(itemStack -> player.getInventory().addItem(itemStack));
            if (!messages_team_benefit_self_team_benefit_resources_tips.trim().isEmpty()) {
                player.sendMessage(messages_team_benefit_self_team_benefit_resources_tips
                        .replace("{resources_description}", resources_description_normal)
                );
            }
        }
    }

    private static void giveEffects(IArena arena, Player player, ConfigurationSection section, Integer duration) {
        List<PotionEffect> effects = parseEffects(section.getStringList("effects.content"), duration);
        effects.forEach(potionEffect -> {
            player.addPotionEffect(potionEffect);
            callInvisibilityEvent(arena, player, potionEffect);
        });
        if (!messages_team_benefit_self_team_benefit_effects_tips.trim().isEmpty()) {
            String description = section.getString("effect_description").replace("&", "§");
            player.sendMessage(messages_team_benefit_self_team_benefit_effects_tips
                    .replace("{effects_description}", description)
            );
        }
        if (!messages_team_benefit_self_team_benefit_effects_expired.trim().isEmpty()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(messages_team_benefit_self_team_benefit_effects_expired.replace("&", "§"));
                }
            }.runTaskLater(_233BedWars.getInstance(), duration * 20L);
        }
    }

    private static List<ItemStack> parseResourceItems(List<String> str) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (String s : str) {
            String[] item = s.split(":");
            switch (item[0]) {
                case "iron":
                    itemStacks.add(new ItemStack(Material.IRON_INGOT, Integer.parseInt(item[1])));
                    break;
                case "gold":
                    itemStacks.add(new ItemStack(Material.GOLD_INGOT, Integer.parseInt(item[1])));
                    break;
                case "diamond":
                    itemStacks.add(new ItemStack(Material.DIAMOND, Integer.parseInt(item[1])));
                    break;
                case "emerald":
                    itemStacks.add(new ItemStack(Material.EMERALD, Integer.parseInt(item[1])));
                    break;
            }
        }
        return itemStacks;
    }

    private static List<PotionEffect> parseEffects(List<String> str, Integer duration) {
        List<PotionEffect> potionEffects = new ArrayList<>();
        for (String s : str) {
            String[] effect = s.split(":");
            potionEffects.add(new PotionEffect(PotionEffectType.getByName(effect[0]), duration * 20, Integer.parseInt(effect[1]), Boolean.getBoolean(effect[2])));
        }
        return potionEffects;
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
