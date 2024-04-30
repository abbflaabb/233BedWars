package cn.serendipityr._233bedwars;

import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.addons.XpResMode;
import cn.serendipityr._233bedwars.commands.CommandManager;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.events.EventManager;
import cn.serendipityr._233bedwars.utils.*;
import com.andrei1058.bedwars.api.BedWars;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class _233BedWars extends JavaPlugin {
    public static _233BedWars instance;

    @Override
    public void onEnable() {
        instance = this;
        LogUtil.consoleLog("&3&l&m--------&9 233BedWars &fby &6SerendipityR &3&l&m--------");
        LogUtil.consoleLog("&3 > &e插件装载中...");
        LogUtil.consoleLog("&3 > &e正在加载配置...");
        ConfigManager.loadConfig();
        LogUtil.consoleLog("&3 > &eHooking BedWars1058...");
        ProviderUtil.bw = (BedWars) ProviderUtil.hookPlugin("BedWars1058", BedWars.class);
        LogUtil.consoleLog("&3 > &eHooking TheAPI...");
        if (!ProviderUtil.getAPIProvider()) {
            LogUtil.consoleLog("&3 > &cFailed to hook TheAPI! CraftBukkit Ver: " + Bukkit.getBukkitVersion());
            LogUtil.consoleLog("&3 > &c请检查前置插件 [TheAPI] 是否存在，若已安装请尝试获取更新。");
            LogUtil.consoleLog("&3 > &c从此链接下载前置插件: https://www.spigotmc.org/resources/theapi.72679/");
            disablePlugin();
            return;
        }
        LogUtil.consoleLog("&3 > &eHooking PlaceholderAPI...");
        PlaceholderUtil.hookPlaceHolderAPI();
        LogUtil.consoleLog("&3 > &e正在注册命令...");
        new CommandManager().regCommands();
        LogUtil.consoleLog("&3 > &e正在注册事件...");
        EventManager.regEventHandlers();
        LogUtil.consoleLog("&3 > &e正在激活模块...");
        ScoreBoardUtil.init();
        TaskUtil.initOneTickTask();
        XpResMode.init();
        ShopItemAddon.init(false);
        LogUtil.consoleLog("&3 > &a插件已启用！");
        LogUtil.consoleLog("&3&l&m--------------------------------------------");
    }

    @Override
    public void onDisable() {
        LogUtil.consoleLog("&3&l&m--------&9 233BedWars &fby &6SerendipityR &3&l&m--------");
        LogUtil.consoleLog("&3&l > &c插件已被卸载。");
        LogUtil.consoleLog("&3&l&m--------------------------------------------");
    }

    public static _233BedWars getInstance() {
        return instance;
    }

    public void disablePlugin() {
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
