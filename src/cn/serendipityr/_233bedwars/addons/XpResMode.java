package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class XpResMode {
    static double ratio_iron;
    static double ratio_gold;
    static double ratio_diamond;
    static double ratio_emerald;
    public static String currency;
    public static String currency_color;
    static String selected;
    static String unselected;
    static String choose_normal;
    static String choose_exp;
    static String[] pick_up_sound;
    static Integer kill_reclaim_exp;
    static String kill_reclaim_message;
    static Integer gui_size;
    static String gui_title;
    static HashMap<Integer, ItemStack> items = new HashMap<>();
    static HashMap<Integer, ItemStack> gui_items = new HashMap<>();
    static ConcurrentHashMap<Player, Boolean> playerResType = new ConcurrentHashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        ratio_iron = cfg.getDouble("resRatio.IRON");
        ratio_gold = cfg.getDouble("resRatio.GOLD");
        ratio_diamond = cfg.getDouble("resRatio.DIAMOND");
        ratio_emerald = cfg.getDouble("resRatio.EMERALD");
        currency = cfg.getString("currency").replace("&", "§");
        currency_color = cfg.getString("currency_color").replace("&", "§");
        selected = cfg.getString("selected").replace("&", "§");
        unselected = cfg.getString("unselected").replace("&", "§");
        choose_normal = cfg.getString("choose_normal").replace("&", "§");
        choose_exp = cfg.getString("choose_exp").replace("&", "§");
        pick_up_sound = cfg.getString("pick_up_sound").split(":");
        kill_reclaim_exp = cfg.getInt("kill_reclaim_exp");
        kill_reclaim_message = cfg.getString("kill_reclaim_message").replace("&", "§");
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

    public static void initPlayer(Player player) {
        playerResType.put(player, true);
    }

    public static void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, gui_size, gui_title);
        for (Integer slot : gui_items.keySet()) {
            ItemStack item = gui_items.get(slot).clone();
            ItemMeta im = item.getItemMeta();
            if (im != null) {
                List<String> lores = new ArrayList<>();
                for (String lore : im.getLore()) {
                    lores.add(lore.replace("{choose_exp}", isExpMode(player) ? unselected : selected).replace("{choose_normal}", isExpMode(player) ? selected : unselected));
                }
                im.setLore(lores);
            }
            item.setItemMeta(im);
            gui.setItem(slot, item);
        }

        player.openInventory(gui);
    }

    public static void giveItems(Player player) {
        if (ConfigManager.addon_xpResMode) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(_233BedWars.getInstance(), () -> {
                for (Integer slot : items.keySet()) {
                    player.getInventory().setItem(slot, items.get(slot));
                }
            }, 16L);
        }
    }

    public static boolean handlePickUp(Player player, Item item) {
        ItemStack itemStack = item.getItemStack();
        int giveLevels = calcExpLevel(itemStack.getType(), itemStack.getAmount());
        if (playerResType.get(player)) {
            if (giveLevels != -1) {
                item.remove();
                player.setLevel(player.getLevel() + giveLevels);
                player.playSound(player.getLocation(), Sound.valueOf(pick_up_sound[0]), Float.parseFloat(pick_up_sound[1]), Float.parseFloat(pick_up_sound[2]));
                return true;
            }
        }
        return false;
    }

    public static void setResMode(Player player, String mode) {
        if ("exp".equals(mode)) {
            playerResType.put(player, true);
            player.closeInventory();
            player.sendMessage(choose_exp);
        } else {
            playerResType.put(player, false);
            player.closeInventory();
            player.sendMessage(choose_normal);
        }
    }

    public static boolean isExpMode(Player player) {
        return playerResType.getOrDefault(player, false);
    }

    public static int calcExpLevel(Material material, int amount) {
        int giveLevels = -1;
        switch (material) {
            case IRON_INGOT:
                giveLevels = (int) Math.round(amount * ratio_iron);
                break;
            case GOLD_INGOT:
                giveLevels = (int) Math.round(amount * ratio_gold);
                break;
            case DIAMOND:
                giveLevels = (int) Math.round(amount * ratio_diamond);
                break;
            case EMERALD:
                giveLevels = (int) Math.round(amount * ratio_emerald);
                break;
        }
        return giveLevels;
    }

    public static void handlePlayerDamage(IArena arena, Player killer, Player victim, double finalDamage) {
        if (kill_reclaim_exp == 0) {
            return;
        }
        double roundedHealth = roundDouble(Math.max(0, victim.getHealth() - finalDamage), 1);
        if (roundedHealth <= 0) {
            int claimExp = Math.round(victim.getLevel() * ((float) kill_reclaim_exp / 100));
            if (claimExp == 0) {
                return;
            }
            killer.setLevel(killer.getLevel() + claimExp);
            if (kill_reclaim_message.trim().isEmpty()) {
                return;
            }
            killer.sendMessage(kill_reclaim_message
                    .replace("{vtColor}", PlaceholderUtil.getTeamColor(arena.getTeam(victim)))
                    .replace("{victim}", victim.getName())
                    .replace("{exp}", String.valueOf(claimExp)));
        }
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