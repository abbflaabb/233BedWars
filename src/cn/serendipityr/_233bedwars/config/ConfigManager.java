package cn.serendipityr._233bedwars.config;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.*;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.List;

public class ConfigManager {
    public static YamlConfiguration cfg;
    public static String serverID;
    public static Boolean addon_scoreBoardEditor;
    public static Boolean addon_teamNameThemes;
    public static Boolean addon_combatDetails;
    public static Boolean addon_fastCommands;
    public static Boolean addon_dalaoWarning;
    public static Boolean addon_balancedAdjustments;
    public static Boolean addon_generatorEditor;
    public static Boolean addon_actionBar;
    public static Boolean addon_globalEvents;
    public static Boolean addon_advancedItems;
    public static Boolean addon_xpResourceMode;

    public static void loadConfig() {
        cfg = YamlConfiguration.loadConfiguration(getCfgFile("config.yml"));
        // 读取配置项
        serverID = cfg.getString("serverID");
        addon_scoreBoardEditor = cfg.getBoolean("addons.ScoreBoardEditor");
        addon_teamNameThemes = cfg.getBoolean("addons.TeamNameThemes");
        addon_combatDetails = cfg.getBoolean("addons.CombatDetails");
        addon_fastCommands = cfg.getBoolean("addons.FastCommands");
        addon_dalaoWarning = cfg.getBoolean("addons.DalaoWarning");
        addon_balancedAdjustments = cfg.getBoolean("addons.BalancedAdjustments");
        addon_generatorEditor = cfg.getBoolean("addons.GeneratorEditor");
        addon_actionBar = cfg.getBoolean("addons.ActionBar");
        loadAddonsCfg();
    }

    static File getCfgFile(String fileName) {
        File cfgFile = new File(_233BedWars.getInstance().getDataFolder(), fileName);
        if (!cfgFile.exists()) {
            _233BedWars.getInstance().saveResource(fileName, false);
        }
        return cfgFile;
    }

    static void loadAddonsCfg() {
        TeamNameThemes.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/teamNameThemes.yml")));
        ScoreboardEditor.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/scoreboard.yml")));
        CombatDetails.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/combatDetails.yml")));
        FastCommands.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/fastCommands.yml")));
        DalaoWarning.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/dalaoWarning.yml")));
        BalancedAdjustments.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/balancedAdjustments.yml")));
        GeneratorEditor.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/generatorEditor.yml")));
        PlaceholderUtil.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/extraPlaceHolder.yml")));
        ActionBar.loadConfig(YamlConfiguration.loadConfiguration(getCfgFile("addons/actionBar.yml")));
    }

    public static ItemStack parseItem(ConfigurationSection section) {
        String material = section.getString("material");
        int amount = (section.getInt("amount") == 0) ? 1 : section.getInt("amount");
        String name = section.getString("name").replace("&", "§");
        List<String> lore = section.getStringList("lore");
        lore.replaceAll(s -> s.replace("&", "§"));
        String execute = section.getString("execute").replace("&", "§");
        try {
            ItemStack item = new ItemStack(Material.getMaterial(material), amount);
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(name);
                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
            }
            InteractEventHandler.executes.put(item, execute);
            return item;
        } catch (Exception e) {
            LogUtil.consoleLog("&9233BedWars &3&l > &c无法解析物品: (Material: " + material + " | Amount: " + amount + ")");
            e.printStackTrace();
            return new ItemStack(Material.AIR);
        }
    }
}
