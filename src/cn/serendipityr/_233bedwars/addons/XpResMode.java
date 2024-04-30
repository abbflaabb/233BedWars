package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import cn.serendipityr._233bedwars.overwrite.XPShopCategory;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class XpResMode {
    static double ratio_iron;
    static double ratio_gold;
    static double ratio_diamond;
    static double ratio_emerald;
    static String selected;
    static String unselected;
    static String error_unaffordable;
    static String choose_normal;
    static String choose_exp;
    static String[] pick_up_sound;
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
        selected = cfg.getString("selected");
        unselected = cfg.getString("unselected");
        error_unaffordable = cfg.getString("error_unaffordable");
        choose_normal = cfg.getString("choose_normal");
        choose_exp = cfg.getString("choose_exp");
        pick_up_sound = cfg.getString("pick_up_sound").split(":");
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

    static ShopIndex xpShop;
    public static void init() {
        ShopIndex normalShop = ShopManager.getShop();
        xpShop = new ShopIndex(normalShop.getNamePath(), normalShop.getQuickBuyButton(), Messages.SHOP_SEPARATOR_NAME, Messages.SHOP_SEPARATOR_LORE, normalShop.separatorSelected, normalShop.separatorStandard);
        List<ShopCategory> xpCategoryList = new ArrayList<>();
        for (ShopCategory category : xpShop.getCategoryList()) {
            xpCategoryList.add(new XPShopCategory(category.getName(), ProviderUtil.bw.getConfigs().getShopConfig().getYml()));
        }
        try {
            Field categoryListField = ShopIndex.class.getDeclaredField("categoryList");
            categoryListField.setAccessible(true);
            categoryListField.set(xpShop, xpCategoryList);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
    }

    public static void initPlayer(Player player) {
        playerResType.put(player, true);
    }

    public static void resetPlayers(IArena arena) {
        for (Player player : arena.getPlayers()) {
            playerResType.remove(player);
        }
    }

    public static void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, gui_size, gui_title.replace("&", "§"));
        for (Integer slot : gui_items.keySet()) {
            ItemStack item = gui_items.get(slot).clone();
            ItemMeta im = item.getItemMeta();
            if (im != null) {
                List<String> lores = new ArrayList<>();
                for (String lore : im.getLore()) {
                    lores.add(lore.replace("{choose_exp}", isExpMode(player) ? unselected : selected).replace("{choose_normal}", isExpMode(player) ? selected : unselected).replace("&", "§"));
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
        if (playerResType.get(player)) {
            int giveLevels = calcExpLevel(itemStack.getType(), itemStack.getAmount());
            if (giveLevels != -1) {
                item.remove();
                player.setLevel(player.getLevel() + giveLevels);
                player.playSound(player.getLocation(), Sound.valueOf(pick_up_sound[0]), Float.parseFloat(pick_up_sound[1]), Float.parseFloat(pick_up_sound[2]));
            }
            return true;
        }
        return false;
    }

    public static void setResMode(Player player, String mode) {
        if ("exp".equals(mode)) {
            playerResType.put(player, true);
            player.closeInventory();
            player.sendMessage(choose_exp.replace("&", "§"));
        } else {
            playerResType.put(player, false);
            player.closeInventory();
            player.sendMessage(choose_normal.replace("&", "§"));
        }
    }

    public static boolean isExpMode(Player player) {
        return playerResType.get(player);
    }

    public static void handleShopOpen(Player player) {
        if (playerResType.containsKey(player)) {
            xpShop.open(player, PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId()), false);
        } else {
            ShopManager.getShop().open(player, PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId()), false);
        }
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
}