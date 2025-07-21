package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import cn.serendipityr._233bedwars.utils.MathUtil;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
    public static Boolean replace_upgrade_shop;
    static Integer gui_size;
    static String gui_title;
    static HashMap<String, Integer> special_item_cost = new HashMap<>();
    public static HashMap<String, String> force_groups = new HashMap<>();
    static HashMap<Integer, ItemStack> items = new HashMap<>();
    static HashMap<Integer, ItemStack> gui_items = new HashMap<>();
    static ConcurrentHashMap<UUID, Boolean> playerResType = new ConcurrentHashMap<>();

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
        force_groups.clear();
        for (String s : cfg.getStringList("force_groups")) {
            String[] _s = s.split(":");
            force_groups.put(_s[0], _s[1]);
        }
        pick_up_sound = cfg.getString("pick_up_sound").split(":");
        kill_reclaim_exp = cfg.getInt("kill_reclaim_exp");
        kill_reclaim_message = cfg.getString("kill_reclaim_message").replace("&", "§");
        replace_upgrade_shop = cfg.getBoolean("replace_upgrade_shop");
        special_item_cost.clear();
        for (String kv : cfg.getStringList("special_item_cost")) {
            String[] _kv = kv.split(":");
            special_item_cost.put(_kv[0], Integer.parseInt(_kv[1]));
        }
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

    public static void initPlayer(Player player, IArena arena) {
        playerResType.put(player.getUniqueId(), force_groups.getOrDefault(arena.getGroup(), "exp").equals("exp"));
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

    public static void giveItems(Player player, IArena arena) {
        if (ConfigManager.addon_xpResMode) {
            if (force_groups.containsKey(arena.getGroup())) {
                return;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Integer slot : items.keySet()) {
                        player.getInventory().setItem(slot, items.get(slot));
                    }
                }
            }.runTaskLater(_233BedWars.getInstance(), 16L);
        }
    }

    public static boolean handlePickUp(Player player, Item item) {
        ItemStack itemStack = item.getItemStack();
        int giveLevels = calcExpLevel(itemStack.getType(), itemStack.getAmount(), false, null);
        if (playerResType.get(player.getUniqueId())) {
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
            playerResType.put(player.getUniqueId(), true);
            player.closeInventory();
            player.sendMessage(choose_exp);
        } else {
            playerResType.put(player.getUniqueId(), false);
            player.closeInventory();
            player.sendMessage(choose_normal);
        }
    }

    public static boolean isExpMode(Player player) {
        return playerResType.getOrDefault(player.getUniqueId(), false);
    }

    public static int calcExpLevel(Material material, int amount, boolean shop, CategoryContent content) {
        if (content != null) {
            String id = content.getIdentifier().split("\\.")[2];
            if (special_item_cost.containsKey(id)) {
                return special_item_cost.get(id);
            }
        }
        int giveLevels = -1;
        if (shop) {
            Material iron_ingot = BedWars.getAPI().getShopUtil().getCurrency("iron");
            Material gold_ingot = BedWars.getAPI().getShopUtil().getCurrency("gold");
            Material diamond = BedWars.getAPI().getShopUtil().getCurrency("diamond");
            Material emerald = BedWars.getAPI().getShopUtil().getCurrency("emerald");
            if (material == iron_ingot) {
                return (int) Math.round(amount * ratio_iron);
            } else if (material == gold_ingot) {
                return (int) Math.round(amount * ratio_gold);
            } else if (material == diamond && XpResMode.replace_upgrade_shop) {
                return (int) Math.round(amount * ratio_diamond);
            } else if (material == emerald) {
                return (int) Math.round(amount * ratio_emerald);
            }
        }

        switch (material) {
            case IRON_INGOT:
                giveLevels = (int) Math.round(amount * ratio_iron);
                break;
            case GOLD_INGOT:
                giveLevels = (int) Math.round(amount * ratio_gold);
                break;
            case DIAMOND:
                if (XpResMode.replace_upgrade_shop) {
                    giveLevels = (int) Math.round(amount * ratio_diamond);
                }
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
        double roundedHealth = MathUtil.roundDouble(Math.max(0, victim.getHealth() - finalDamage), 1);
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
}