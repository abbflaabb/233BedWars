package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.github.paperspigot.Title;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FastCommands {
    static String quick_reminding;
    static String[] quick_reminding_sound;
    static Integer quick_reminding_delay;
    static Integer quick_reminding_count;
    static Integer quick_reminding_title_stay;
    static String error_bed_destroyed;
    static String error_died;
    static Integer gui_size;
    static String gui_title;
    static HashMap<Integer, ItemStack> items = new HashMap<>();
    static HashMap<Integer, ItemStack> gui_items = new HashMap<>();
    static List<Player> remindingPlayers = new CopyOnWriteArrayList<>();

    public static void loadConfig(YamlConfiguration cfg) {
        quick_reminding = cfg.getString("quick_reminding");
        quick_reminding_sound = cfg.getString("quick_reminding_sound").split(":");
        quick_reminding_delay = cfg.getInt("quick_reminding_delay");
        quick_reminding_count = cfg.getInt("quick_reminding_count");
        quick_reminding_title_stay = cfg.getInt("quick_reminding_title_stay");
        error_bed_destroyed = cfg.getString("error_bed_destroyed");
        error_died = cfg.getString("error_died");
        gui_size = cfg.getInt("GUI.size");
        gui_title = cfg.getString("GUI.title");
        items.clear();
        gui_items.clear();
        for (String item : cfg.getConfigurationSection("Item").getKeys(false)) {
            ItemStack _item = ConfigManager.parseItem(cfg.getConfigurationSection("Item." + item));
            InteractEventHandler.preventDrops.add(_item);
            items.put(Integer.parseInt(item), _item);
        }
        for (String item : cfg.getConfigurationSection("GUI.items").getKeys(false)) {
            gui_items.put(Integer.parseInt(item), ConfigManager.parseItem(cfg.getConfigurationSection("GUI.items." + item)));
        }
    }

    public static void giveItems(Player player) {
        if (ConfigManager.addon_fastCommands) {
            for (Integer slot : items.keySet()) {
                player.getInventory().setItem(slot, items.get(slot));
            }
        }
    }

    public static void handleShiftToggle(Player player) {
        if (player.getLocation().getPitch() <= -80) {
            if (!remindingPlayers.contains(player)) {
                ITeam team = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getTeam(player);
                if (team == null) {
                    player.sendMessage(error_died.replace("&", "§"));
                    return;
                }
                if (team.isBedDestroyed()) {
                    player.sendMessage(error_bed_destroyed.replace("&", "§"));
                    return;
                }
                remindingPlayers.add(player);
                for (Player p : team.getMembers()) {
                    for (int i = 0; i < quick_reminding_count; i++) {
                        int finalI = i;
                        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                            p.sendTitle(new Title("", quick_reminding.replace("{tColor}", team.getColor().chat().toString()).replace("{player}", player.getDisplayName()).replace("&", "§"), 0, quick_reminding_title_stay, 0));
                            p.playSound(p.getLocation(), Sound.valueOf(quick_reminding_sound[0]), Float.parseFloat(quick_reminding_sound[1]), Float.parseFloat(quick_reminding_sound[2]));
                            if (finalI == quick_reminding_count - 1) {
                                remindingPlayers.remove(player);
                            }
                        }, (long) i * quick_reminding_delay);
                    }
                }
            }
        }
    }

    public static void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, gui_size, gui_title.replace("&", "§"));

        for (Integer slot : gui_items.keySet()) {
            gui.setItem(slot, gui_items.get(slot));
        }

        player.openInventory(gui);
    }
}
