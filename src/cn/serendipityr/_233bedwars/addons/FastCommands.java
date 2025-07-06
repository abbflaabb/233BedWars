package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import cn.serendipityr._233bedwars.utils.TitleUtil;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FastCommands {
    static String quick_reminding;
    static Integer quick_reminding_threshold;
    static Integer quick_reminding_threshold_timeout;
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
    static List<ITeam> remindingTeams = new CopyOnWriteArrayList<>();
    static ConcurrentHashMap<Player, Integer> remindingThreshold = new ConcurrentHashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        quick_reminding = cfg.getString("quick_reminding").replace("&", "§");
        quick_reminding_threshold = cfg.getInt("quick_reminding_threshold");
        quick_reminding_threshold_timeout = cfg.getInt("quick_reminding_threshold_timeout");
        quick_reminding_sound = cfg.getString("quick_reminding_sound").split(":");
        quick_reminding_delay = cfg.getInt("quick_reminding_delay");
        quick_reminding_count = cfg.getInt("quick_reminding_count");
        quick_reminding_title_stay = cfg.getInt("quick_reminding_title_stay");
        error_bed_destroyed = cfg.getString("error_bed_destroyed").replace("&", "§");
        error_died = cfg.getString("error_died").replace("&", "§");
        gui_size = cfg.getInt("GUI.size");
        gui_title = cfg.getString("GUI.title").replace("&", "§");
        items.clear();
        gui_items.clear();
        for (String item : cfg.getConfigurationSection("Item").getKeys(false)) {
            ItemStack _item = ConfigManager.parseItem(cfg.getConfigurationSection("Item." + item));
            InteractEventHandler.addPreventDrop(_item);
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
            int threshold = remindingThreshold.containsKey(player) ? remindingThreshold.get(player) + 1 : 1;
            remindingThreshold.put(player, threshold);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (remindingThreshold.containsKey(player) && remindingThreshold.get(player) == threshold) {
                        remindingThreshold.remove(player);
                    }
                }
            }.runTaskLaterAsynchronously(_233BedWars.getInstance(), (long) quick_reminding_threshold_timeout * 20);
            ITeam team = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getTeam(player);
            if (!remindingTeams.contains(team) && threshold >= quick_reminding_threshold) {
                if (team == null) {
                    player.sendMessage(error_died);
                    return;
                }
                if (team.isBedDestroyed()) {
                    player.sendMessage(error_bed_destroyed);
                    return;
                }
                remindingTeams.add(team);
                for (Player p : team.getMembers()) {
                    for (int i = 0; i < quick_reminding_count; i++) {
                        int finalI = i;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                TitleUtil.send(p, "", quick_reminding.replace("{tColor}", team.getColor().chat().toString()).replace("{player}", player.getDisplayName()), 0, quick_reminding_title_stay, 0);
                                p.playSound(p.getLocation(), Sound.valueOf(quick_reminding_sound[0]), Float.parseFloat(quick_reminding_sound[1]), Float.parseFloat(quick_reminding_sound[2]));
                                if (finalI == quick_reminding_count - 1) {
                                    remindingThreshold.remove(player);
                                    remindingTeams.remove(team);
                                }
                            }
                        }.runTaskLaterAsynchronously(_233BedWars.getInstance(), (long) i * quick_reminding_delay);
                    }
                }
            }
        }
    }

    public static void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, gui_size, gui_title);

        for (Integer slot : gui_items.keySet()) {
            gui.setItem(slot, gui_items.get(slot));
        }

        player.openInventory(gui);
    }
}
