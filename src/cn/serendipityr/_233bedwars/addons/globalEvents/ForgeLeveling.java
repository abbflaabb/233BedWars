package cn.serendipityr._233bedwars.addons.globalEvents;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.CombatDetails;
import cn.serendipityr._233bedwars.addons.GlobalEvents;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.LogUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.upgrades.UpgradeAction;
import com.andrei1058.bedwars.upgrades.menu.MenuUpgrade;
import com.andrei1058.bedwars.upgrades.menu.UpgradeTier;
import com.andrei1058.bedwars.upgrades.upgradeaction.GeneratorEditAction;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ForgeLeveling {
    public static boolean enable;
    static Integer interval;
    static Integer max_tier;
    static String message_forge_leveling_upgrade;

    public static void loadConfig(YamlConfiguration cfg) {
        enable = cfg.getBoolean("events.forge_leveling.enable");
        if (enable) {
            GlobalEvents.enable_events.add("forge_leveling");
        }
        interval = cfg.getInt("events.forge_leveling.interval");
        max_tier = cfg.getInt("events.forge_leveling.max_tier") - 1;
        message_forge_leveling_upgrade = cfg.getString("messages.forge_leveling_upgrade").replace("&", "§");
    }

    public static void initEvent(IArena arena) {
        MenuUpgrade forge_upgrade = GlobalEvents.getForgeUpgrade(arena);
        if (forge_upgrade != null) {
            new BukkitRunnable() {
                int i = 0;
                boolean first = false;

                public void run() {
                    if (arena.getStatus() != GameState.playing) {
                        this.cancel();
                    }
                    i--;
                    if (i <= 0) {
                        i = interval;
                        if (!first) {
                            first = true;
                            return;
                        }
                        for (ITeam team : arena.getTeams()) {
                            upgradeForge(team, forge_upgrade);
                        }
                    }
                }
            }.runTaskTimer(_233BedWars.getInstance(), 0L, 20L);
        } else {
            LogUtil.consoleLog("&9233BedWars &3&l > &c游戏 &b" + arena.getArenaName() + "&7(" + arena.getGroup() + ") &c在应用全域事件时发生错误：获取锻炉团队升级失败。");
        }
    }

    private static void upgradeForge(ITeam team, MenuUpgrade mu) {
        int tier = team.getTeamUpgradeTiers().getOrDefault(mu.getName(), -1) + 1;
        List<UpgradeTier> tiers = mu.getTiers();
        if (tier > max_tier || tier >= tiers.size()) {
            return;
        }
        team.getTeamUpgradeTiers().put(mu.getName(), tier);
        for (UpgradeAction ua : tiers.get(tier).getUpgradeActions()) {
            if (ua instanceof GeneratorEditAction) {
                GeneratorEditAction editAction = (GeneratorEditAction) ua;
                editAction.onBuy(null, team);
            }
        }
        ShopItemAddon.sendTeamMessage(team, message_forge_leveling_upgrade
                .replace("{tier}", String.valueOf(tier + 1))
                .replace("{tier_roman}", CombatDetails.intToRoman(tier + 1))
        );
    }
}