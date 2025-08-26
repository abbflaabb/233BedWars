package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.addons.XpResMode;
import cn.serendipityr._233bedwars.config.ConfigManager;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.events.upgrades.UpgradeBuyEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.upgrades.MenuContent;
import com.andrei1058.bedwars.api.upgrades.UpgradeAction;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement;
import com.andrei1058.bedwars.upgrades.UpgradesManager;
import com.andrei1058.bedwars.upgrades.menu.*;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class BedWarsShopUtil {
    static Field weightField;
    static Field categoryField;

    public static void init() {
        try {
            weightField = CategoryContent.class.getDeclaredField("weight");
            weightField.setAccessible(true);
            categoryField = CategoryContent.class.getDeclaredField("father");
            categoryField.setAccessible(true);
        } catch (Exception e) {
            LogUtil.consoleLog("&9233BedWars &3&l> &e[BedWarsShopUtil] &c发生致命错误！");
            e.printStackTrace();
        }
    }

    public static void handleShopOpen(Player player, Inventory shopInv, String title) {
        UUID uuid = player.getUniqueId();
        if (!XpResMode.isExpMode(player)) {
            if (isQuickBuy(player, title)) {
                PlayerQuickBuyCache quickBuyCache = PlayerQuickBuyCache.getQuickBuyCache(uuid);
                if (quickBuyCache == null) {
                    return;
                }
                for (QuickBuyElement element : quickBuyCache.getElements()) {
                    CategoryContent content = element.getCategoryContent();
                    int slot = element.getSlot();
                    fixContentInventory(content, shopInv, slot);
                }
            } else {
                ShopCategory category = getCategoryFromInventory(player, title);
                if (category == null) {
                    return;
                }
                // 处理其他分类
                for (CategoryContent content : category.getCategoryContentList()) {
                    int slot = content.getSlot();
                    fixContentInventory(content, shopInv, slot);
                }
            }
            return;
        }
        if (isQuickBuy(player, title)) {
            // 处理快速购买
            PlayerQuickBuyCache quickBuyCache = PlayerQuickBuyCache.getQuickBuyCache(uuid);
            ShopCache shopCache = ShopCache.getShopCache(uuid);
            if (quickBuyCache == null) {
                return;
            }
            for (QuickBuyElement element : quickBuyCache.getElements()) {
                CategoryContent content = element.getCategoryContent();
                int slot = element.getSlot();
                int price = getCategoryContentPrice(shopCache, content);
                setContentInventory(player, content, shopCache, shopInv, title, slot, price, true);
            }
        } else {
            ShopCategory category = getCategoryFromInventory(player, title);
            if (category == null) {
                return;
            }
            // 处理其他分类
            ShopCache shopCache = ShopCache.getShopCache(uuid);
            for (CategoryContent content : category.getCategoryContentList()) {
                int slot = content.getSlot();
                int price = getCategoryContentPrice(shopCache, content);
                setContentInventory(player, content, shopCache, shopInv, title, slot, price, false);
            }
        }
    }

    public static boolean handleShopClick(Player player, Inventory shopInv, String title, int slot) {
        if (!XpResMode.isExpMode(player)) {
            return false;
        }
        UUID uuid = player.getUniqueId();
        CategoryContent content;
        if (isQuickBuy(player, title)) {
            // 处理快速购买
            PlayerQuickBuyCache quickBuyCache = PlayerQuickBuyCache.getQuickBuyCache(uuid);
            if (quickBuyCache == null) {
                return false;
            }
            content = getCategoryContentFromQuickBuy(quickBuyCache, slot);
        } else {
            ShopCategory category = getCategoryFromInventory(player, title);
            if (category == null) {
                return false;
            }
            // 处理其他分类
            content = getCategoryContentFromShopCategory(category, slot);
        }

        if (content == null) {
            return false;
        }

        ShopCache shopCache = ShopCache.getShopCache(uuid);
        buyItem(player, shopCache, content, shopInv, title, slot);
        return true;
    }

    public static void handleUpgradeShopOpen(Player player, Inventory shopInv) {
        if (!XpResMode.isExpMode(player) || !isWatchingUpgradeShop(player) || !XpResMode.replace_upgrade_shop) {
            return;
        }
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        ImmutableMap<Integer, MenuContent> index = UpgradesManager.getMenuForArena(arena).getMenuContentBySlot();
        updateUpgradeShop(index, player, shopInv, arena);
    }

    public static boolean handleUpgradeShopClick(Player player, Inventory shopInv, int slot) {
        if (!XpResMode.isExpMode(player) || !XpResMode.replace_upgrade_shop || !isWatchingUpgradeShop(player) || shopInv.getItem(slot) == null) {
            return false;
        }
        MenuContent mc = UpgradesManager.getMenuContent(shopInv.getItem(slot));
        if (mc == null) {
            return false;
        }
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        ITeam team = arena.getTeam(player);
        if (mc instanceof MenuUpgrade) {
            MenuUpgrade mu = (MenuUpgrade) mc;
            String name = mu.getName();
            List<UpgradeTier> tiers = mu.getTiers();
            int tier = -1;
            if (team.getTeamUpgradeTiers().containsKey(name)) {
                tier = team.getTeamUpgradeTiers().get(name);
            }
            if (tiers.size() - 1 > tier) {
                UpgradeTier ut = tiers.get(tier + 1);
                int cost = XpResMode.calcExpLevel(ut.getCurrency(), ut.getCost(), true, null);
                if (player.getLevel() < cost) {
                    Sounds.playSound("shop-insufficient-money", player);
                    player.sendMessage(Language.getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", XpResMode.currency).replace("{amount}", String.valueOf(cost - player.getLevel())));
                    player.closeInventory();
                    return true;
                }
                UpgradeBuyEvent event;
                Bukkit.getPluginManager().callEvent(event = new UpgradeBuyEvent(mu, player, team));
                if (event.isCancelled()) {
                    return true;
                }
                if (team.getTeamUpgradeTiers().containsKey(name)) {
                    team.getTeamUpgradeTiers().replace(name, team.getTeamUpgradeTiers().get(name) + 1);
                } else {
                    team.getTeamUpgradeTiers().put(name, 0);
                }
                takeMoney(player, cost);
                Sounds.playSound("shop-bought", player);
                for (UpgradeAction ua : ut.getUpgradeActions()) {
                    ua.onBuy(player, team);
                }
                for (Player member : team.getMembers()) {
                    member.sendMessage(Language.getMsg(member, Messages.UPGRADES_UPGRADE_BOUGHT_CHAT).replace("{playername}", player.getName()).replace("{player}", player.getDisplayName()).replace("{upgradeName}", ChatColor.stripColor(Language.getMsg(member, Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", name.replace("upgrade-", "")).replace("{tier}", ut.getName())))).replace("{color}", ""));
                }
                ImmutableMap<Integer, MenuContent> index = UpgradesManager.getMenuForArena(arena).getMenuContentBySlot();
                updateUpgradeShop(index, player, shopInv, arena);
            }
        }
        if (mc instanceof MenuCategory) {
            MenuCategory mcg = (MenuCategory) mc;
            String name = mcg.getName();
            if (name.equalsIgnoreCase("category-traps")) {
                int queueLimit = UpgradesManager.getConfiguration().getInt(team.getArena().getGroup().toLowerCase() + "-upgrades-settings.trap-queue-limit");
                if (queueLimit == 0) {
                    queueLimit = UpgradesManager.getConfiguration().getInt("default-upgrades-settings.trap-queue-limit");
                }
                if (queueLimit <= team.getActiveTraps().size()) {
                    player.sendMessage(Language.getMsg(player, Messages.UPGRADES_TRAP_QUEUE_LIMIT));
                    return true;
                }
            }
            Inventory inv = Bukkit.createInventory(null, 45, Language.getMsg(player, Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + name.replace("category-", "")));
            try {
                Field field = mcg.getClass().getDeclaredField("menuContentBySlot");
                field.setAccessible(true);
                HashMap<Integer, MenuContent> menuContentBySlot = (HashMap<Integer, MenuContent>) field.get(mcg);
                for (int s : menuContentBySlot.keySet()) {
                    inv.setItem(s, menuContentBySlot.get(s).getDisplayItem(player, team));
                }
                player.openInventory(inv);
                UpgradesManager.setWatchingUpgrades(player.getUniqueId());
                updateCategoryMenu(menuContentBySlot, player.getOpenInventory().getTopInventory(), player, team);
            } catch (Exception ignored) {
            }
        }
        if (mc instanceof MenuBaseTrap) {
            MenuBaseTrap mbt = (MenuBaseTrap) mc;
            String name = mbt.getName();
            if (name.contains("trap-slot")) {
                return true;
            }
            int queueLimit = UpgradesManager.getConfiguration().getInt(team.getArena().getGroup().toLowerCase() + "-upgrades-settings.trap-queue-limit");
            if (queueLimit == 0) {
                queueLimit = UpgradesManager.getConfiguration().getInt("default-upgrades-settings.trap-queue-limit");
            }
            if (queueLimit <= team.getActiveTraps().size()) {
                player.sendMessage(Language.getMsg(player, Messages.UPGRADES_TRAP_QUEUE_LIMIT));
            } else {
                int cost = getTrapCost(team);
                if (player.getLevel() < cost) {
                    Sounds.playSound("shop-insufficient-money", player);
                    player.sendMessage(Language.getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", XpResMode.currency).replace("{amount}", String.valueOf(cost - player.getLevel())));
                    player.closeInventory();
                } else {
                    UpgradeBuyEvent event;
                    Bukkit.getPluginManager().callEvent(event = new UpgradeBuyEvent(mbt, player, team));
                    if (!event.isCancelled()) {
                        takeMoney(player, cost);
                        Sounds.playSound("shop-bought", player);
                        team.getActiveTraps().add(mbt);
                        for (Player p : team.getArena().getPlayers()) {
                            if (!team.isMember(p) && !team.getArena().isReSpawning(p) && p.getLocation().distance(team.getBed()) <= (double) team.getArena().getIslandRadius()) {
                                team.getActiveTraps().remove(0).trigger(team, p);
                                break;
                            }
                        }
                        for (Player m : team.getMembers()) {
                            String msg = Language.getMsg(m, Messages.UPGRADES_UPGRADE_BOUGHT_CHAT).replace("{playername}", player.getName()).replace("{player}", player.getDisplayName());
                            m.sendMessage(msg.replace("{upgradeName}", ChatColor.stripColor(Language.getMsg(m, Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + name.replace("base-trap-", "")).replace("{color}", ""))));
                        }
                        UpgradesManager.getMenuForArena(team.getArena()).open(player);
                    }
                }
            }
        }
        return !(mc instanceof MenuSeparator);
    }

    private static void updateCategoryMenu(HashMap<Integer, MenuContent> menuContentBySlot, Inventory inv, Player player, ITeam team) {
        for (int slot : menuContentBySlot.keySet()) {
            MenuContent mc = menuContentBySlot.get(slot);
            if (mc instanceof MenuBaseTrap) {
                MenuBaseTrap mbt = (MenuBaseTrap) mc;
                String name = mbt.getName();
                int cost = getTrapCost(team);
                ItemStack i = mbt.getItemStack().clone();
                ItemMeta im = i.getItemMeta();
                if (im != null) {
                    boolean afford = player.getLevel() >= cost;
                    String color;
                    if (afford) {
                        color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CAN_AFFORD);
                    } else {
                        color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CANT_AFFORD);
                    }

                    im.setDisplayName(Language.getMsg(player, Messages.UPGRADES_BASE_TRAP_ITEM_NAME_PATH + name.replace("base-trap-", "")).replace("{color}", color));
                    List<String> lore = Language.getList(player, Messages.UPGRADES_BASE_TRAP_ITEM_LORE_PATH + name.replace("base-trap-", ""));
                    String currencyMsg = XpResMode.currency;
                    lore.add(Language.getMsg(player, Messages.FORMAT_UPGRADE_TRAP_COST).replace("{cost}", String.valueOf(cost)).replace("{currency}", currencyMsg).replace("{currencyColor}", XpResMode.currency_color));
                    lore.add("");
                    if (afford) {
                        lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY).replace("{color}", color));
                    } else {
                        lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY).replace("{currency}", currencyMsg).replace("{color}", color));
                    }

                    im.setLore(lore);
                    im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    i.setItemMeta(im);
                }
                inv.setItem(slot, i);
            }
        }
        player.updateInventory();
    }

    private static void updateUpgradeShop(ImmutableMap<Integer, MenuContent> index, Player player, Inventory shopInv, IArena arena) {
        ITeam team = arena.getTeam(player);
        String currencyMsg = XpResMode.currency;
        for (int slot : index.keySet()) {
            if (shopInv.getItem(slot) == null) {
                return;
            }
            MenuContent mc = index.get(slot);
            if (mc instanceof MenuUpgrade) {
                MenuUpgrade mu = (MenuUpgrade) mc;
                String name = mu.getName();
                List<UpgradeTier> tiers = mu.getTiers();
                if (tiers.isEmpty()) {
                    continue;
                }
                int tier = 0;
                if (team.getTeamUpgradeTiers().containsKey(name)) {
                    tier = team.getTeamUpgradeTiers().get(name);
                }
                ItemStack i = new ItemStack(tiers.get(tier).getDisplayItem());
                ItemMeta im = i.getItemMeta();
                if (!i.getType().equals(shopInv.getItem(slot).getType())) {
                    continue;
                }
                UpgradeTier ut = tiers.get(tier);
                boolean highest = tiers.size() == tier + 1 && team.getTeamUpgradeTiers().containsKey(name);
                boolean afford = isAffordable(player, XpResMode.calcExpLevel(ut.getCurrency(), ut.getCost(), true, null));
                String color;
                if (!highest) {
                    if (afford) {
                        color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CAN_AFFORD);
                    } else {
                        color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_CANT_AFFORD);
                    }
                } else {
                    color = Language.getMsg(player, Messages.FORMAT_UPGRADE_COLOR_UNLOCKED);
                }
                im.setDisplayName(Language.getMsg(player, Messages.UPGRADES_UPGRADE_TIER_ITEM_NAME.replace("{name}", name.replace("upgrade-", "")).replace("{tier}", ut.getName())).replace("{color}", color));
                List<String> lore = new ArrayList<>();
                for (String s : Language.getList(player, Messages.UPGRADES_UPGRADE_TIER_ITEM_LORE.replace("{name}", name.replace("upgrade-", "")))) {
                    if (s.contains("{tier_")) {
                        String result = s.replaceAll(".*_([0-9]+)_.*", "$1");
                        String tierColor = Messages.FORMAT_UPGRADE_TIER_LOCKED;
                        if (Integer.parseInt(result) - 1 <= team.getTeamUpgradeTiers().getOrDefault(name, -1)) {
                            tierColor = Messages.FORMAT_UPGRADE_TIER_UNLOCKED;
                        }
                        UpgradeTier upgradeTier = tiers.get(Integer.parseInt(result) - 1);
                        lore.add(s.replace("{tier_" + result + "_cost}", String.valueOf(XpResMode.calcExpLevel(upgradeTier.getCurrency(), upgradeTier.getCost(), true, null)))
                                .replace("{tier_" + result + "_currency}", currencyMsg)
                                .replace("{tier_" + result + "_color}", Language.getMsg(player, tierColor)));

                    } else {
                        lore.add(s.replace("{color}", color));
                    }
                }
                if (highest) {
                    lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_UNLOCKED).replace("{color}", color));
                } else if (afford) {
                    lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_CLICK_TO_BUY).replace("{color}", color));
                } else {
                    lore.add(Language.getMsg(player, Messages.UPGRADES_LORE_REPLACEMENT_INSUFFICIENT_MONEY).replace("{currency}", currencyMsg).replace("{color}", color));
                }
                im.setLore(lore);
                im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                i.setItemMeta(im);
                shopInv.setItem(slot, i);
            }
            if (mc instanceof MenuTrapSlot) {
                MenuTrapSlot mts = (MenuTrapSlot) mc;
                String name = mts.getName();
                ItemStack i = shopInv.getItem(slot);
                if (i == null) {
                    continue;
                }
                ItemMeta im = i.getItemMeta();
                List<String> lore = new ArrayList<>();
                int cost = getTrapCost(team);
                for (String s : Language.getList(player, Messages.UPGRADES_TRAP_SLOT_ITEM_LORE1_PATH + name.replace("trap-slot-", ""))) {
                    lore.add(s.replace("{cost}", String.valueOf(cost)).replace("{currency}", currencyMsg));
                }
                lore.add("");
                for (String s : Language.getList(player, Messages.UPGRADES_TRAP_SLOT_ITEM_LORE2_PATH + name.replace("trap-slot-", ""))) {
                    lore.add(s.replace("{cost}", String.valueOf(cost)).replace("{currency}", currencyMsg));
                }
                im.setLore(lore);
                i.setItemMeta(im);
                shopInv.setItem(slot, i);
            }
        }
        player.updateInventory();
    }

    private static boolean isWatchingUpgradeShop(Player player) {
        return ProviderUtil.bw.getTeamUpgradesUtil().isWatchingGUI(player);
    }

    private static int getTrapCost(ITeam team) {
        String curr = UpgradesManager.getConfiguration().getString(team.getArena().getArenaName().toLowerCase() + "-upgrades-settings.trap-currency");
        if (curr == null) {
            curr = UpgradesManager.getConfiguration().getString("default-upgrades-settings.trap-currency");
        }
        int cost = UpgradesManager.getConfiguration().getInt(team.getArena().getArenaName().toLowerCase() + "-upgrades-settings.trap-start-price");
        if (cost == 0) {
            cost = UpgradesManager.getConfiguration().getInt("default-upgrades-settings.trap-start-price");
        }
        if (!team.getActiveTraps().isEmpty()) {
            int multiplier = UpgradesManager.getConfiguration().getInt(team.getArena().getArenaName().toLowerCase() + "-upgrades-settings.trap-increment-price");
            if (multiplier == 0) {
                multiplier = UpgradesManager.getConfiguration().getInt("default-upgrades-settings.trap-increment-price");
            }
            cost += team.getActiveTraps().size() * multiplier;
        }
        cost = XpResMode.calcExpLevel(Material.getMaterial(curr.toUpperCase()), cost, true, null);
        return cost;
    }

    private static boolean isQuickBuy(Player player, String title) {
        return ProviderUtil.bw.getPlayerLanguage(player).m("shop-items-messages.inventory-name").equals(title);
    }

    private static ShopCategory getCategoryFromInventory(Player player, String title) {
        for (ShopCategory shopCategory : ShopManager.getShop().getCategoryList()) {
            String name = getCategoryName(player, shopCategory.getName());
            if (name.equals(title)) {
                return shopCategory;
            }
        }
        return null;
    }

    private static String getCategoryName(Player player, String category) {
        return ProviderUtil.bw.getPlayerLanguage(player).m("shop-items-messages." + category + ".inventory-name");
    }

    private static void fixContentInventory(CategoryContent content, Inventory shopInv, int slot) {
        ItemStack itemStack = shopInv.getItem(slot);
        if (itemStack == null) {
            return;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (ConfigManager.addon_shopItemAddon) {
            if (itemStack.getType().toString().contains("SKULL")) {
                String texture = ShopItemAddon.getSkullTexture(content.getIdentifier().split("\\.")[2]);
                SkullMeta skullMeta = (SkullMeta) itemMeta;
                SkullUtil.setSkullTexture(skullMeta, texture);
            }
        }
        itemStack.setItemMeta(itemMeta);
        shopInv.setItem(slot, itemStack);
    }

    private static void setContentInventory(Player player, CategoryContent content, ShopCache shopCache, Inventory shopInv, String title, int slot, int price, boolean isQuickBuy) {
        List<String> lores = getCategoryContentLore(player, content);

        ItemStack itemStack = shopInv.getItem(slot);
        if (itemStack == null) {
            return;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();

        boolean affordable = isAffordable(player, price);
        int tier_index = getContentTier(shopCache, content);
        if (tier_index >= content.getContentTiers().size()) {
            tier_index = content.getContentTiers().size() - 1;
        }
        String tier = intToRoman(tier_index + 1);
        lores.replaceAll(s -> s
                .replace("{buy_status}", getBuyStatus(player, content, shopCache, affordable))
                .replace("{quick_buy}", getQuickBuyTips(player, isQuickBuy))
                .replace("{tier}", tier)
                .replace("{cost}", XpResMode.currency_color + price)
                .replace("{currency}", XpResMode.currency_color + XpResMode.currency)
                .replace("&", "§"));
        if (affordable) {
            itemMeta.setDisplayName(getCategoryContentName(player, content).replace("{color}", "§a").replace("{tier}", tier));
        } else {
            itemMeta.setDisplayName(getCategoryContentName(player, content).replace("{color}", "§c").replace("{tier}", tier));
        }
        itemMeta.setLore(lores);
        if (ConfigManager.addon_shopItemAddon) {
            if (itemStack.getType().toString().contains("SKULL")) {
                String texture = ShopItemAddon.getSkullTexture(content.getIdentifier().split("\\.")[2]);
                SkullMeta skullMeta = (SkullMeta) itemMeta;
                SkullUtil.setSkullTexture(skullMeta, texture);
            }
        }
        itemStack.setItemMeta(itemMeta);
        shopInv.setItem(slot, itemStack);
    }

    private static String getCategoryContentName(Player player, CategoryContent content) {
        String[] identifier = content.getIdentifier().split("\\.");
        return ProviderUtil.bw.getPlayerLanguage(player).m("shop-items-messages." + identifier[0] + ".content-item-" + identifier[2] + "-name");
    }

    private static List<String> getCategoryContentLore(Player player, CategoryContent content) {
        String[] identifier = content.getIdentifier().split("\\.");
        return ProviderUtil.bw.getPlayerLanguage(player).l("shop-items-messages." + identifier[0] + ".content-item-" + identifier[2] + "-lore");
    }

    private static String getBuyStatus(Player player, CategoryContent content, ShopCache shopCache, boolean affordable) {
        if (content.isPermanent() && shopCache.hasCachedItem(content) && shopCache.getCachedItem(content).getTier() == content.getContentTiers().size()) {
            if (!(BedWars.nms.isArmor(content.getItemStack(player)))) {
                return Language.getMsg(player, Messages.SHOP_LORE_STATUS_MAXED);
            } else {
                return Language.getMsg(player, Messages.SHOP_LORE_STATUS_ARMOR);
            }
        } else if (!affordable) {
            return Language.getMsg(player, Messages.SHOP_LORE_STATUS_CANT_AFFORD).replace("{currency}", XpResMode.currency);
        } else {
            return Language.getMsg(player, Messages.SHOP_LORE_STATUS_CAN_BUY);
        }
    }

    private static Integer getContentTier(ShopCache shopCache, CategoryContent content) {
        // ?当商店缓存物品不存在时默认返回的是1
        if (!shopCache.hasCachedItem(content)) {
            return 0;
        }
        return shopCache.getContentTier(content.getIdentifier());
    }

    private static String getQuickBuyTips(Player player, boolean isQuickBuy) {
        return Language.getMsg(player, isQuickBuy ? Messages.SHOP_LORE_QUICK_REMOVE : Messages.SHOP_LORE_QUICK_ADD);
    }

    private static Integer getCategoryContentPrice(ShopCache shopCache, CategoryContent content) {
        int tier = getContentTier(shopCache, content);
        if (tier >= content.getContentTiers().size()) {
            tier = content.getContentTiers().size() - 1;
        }
        IContentTier contentTier = content.getContentTiers().get(tier);
        return XpResMode.calcExpLevel(contentTier.getCurrency(), contentTier.getPrice(), true, content);
    }

    private static CategoryContent getCategoryContentFromQuickBuy(PlayerQuickBuyCache quickBuyCache, int slot) {
        for (QuickBuyElement element : quickBuyCache.getElements()) {
            if (element.getSlot() == slot) {
                return element.getCategoryContent();
            }
        }
        return null;
    }

    private static CategoryContent getCategoryContentFromShopCategory(ShopCategory shopCategory, int slot) {
        for (CategoryContent content : shopCategory.getCategoryContentList()) {
            if (content.getSlot() == slot) {
                return content;
            }
        }
        return null;
    }

    private static Byte getCategoryContentWeight(CategoryContent content) {
        try {
            return (byte) weightField.get(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static ShopCategory getCategoryContentCategory(CategoryContent content) {
        try {
            return (ShopCategory) categoryField.get(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String intToRoman(int num) {
        String[] thousands = {"", "M", "MM", "MMM"};
        String[] hundreds = {"", "C", "CC", "CCC", "CD", "D",
                "DC", "DCC", "DCCC", "CM"};
        String[] tens = {"", "X", "XX", "XXX", "XL", "L",
                "LX", "LXX", "LXXX", "XC"};
        String[] ones = {"", "I", "II", "III", "IV", "V",
                "VI", "VII", "VIII", "IX"};

        return thousands[num / 1000] +
                hundreds[(num % 1000) / 100] +
                tens[(num % 100) / 10] +
                ones[num % 10];
    }

    private static void buyItem(Player player, ShopCache shopCache, CategoryContent content, Inventory shopInv, String title, int slot) {
        int tier = getContentTier(shopCache, content);
        int price = getCategoryContentPrice(shopCache, content);

        ShopCategory category = getCategoryContentCategory(content);
        if (category == null) {
            return;
        }
        byte categoryWeight = shopCache.getCategoryWeight(category);
        byte contentWeight = getCategoryContentWeight(content);
        if (categoryWeight > contentWeight) {
            return;
        }

        if (isAffordable(player, price)) {
            IContentTier contentTier;
            if (tier >= content.getContentTiers().size()) {
                if (content.isPermanent() && shopCache.hasCachedItem(content)) {
                    player.sendMessage(Language.getMsg(player, "shop-already-bought"));
                    Sounds.playSound("shop-insufficient-money", player);
                    return;
                }
                contentTier = content.getContentTiers().get(content.getContentTiers().size() - 1);
            } else {
                contentTier = content.getContentTiers().get(tier);
            }

            ShopBuyEvent event;
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
            Bukkit.getPluginManager().callEvent(event = new ShopBuyEvent(player, arena, content));
            if (event.isCancelled()) {
                return;
            }
            takeMoney(player, price);
            shopCache.upgradeCachedItem(content, slot);
            for (IBuyItem buyItem : contentTier.getBuyItemsList()) {
                buyItem.give(player, arena);
            }
            Sounds.playSound("shop-bought", player);
            player.sendMessage(Language.getMsg(player, "shop-new-purchase").replace("{item}", ChatColor.stripColor(getCategoryContentName(player, content)).replace("{color}", "").replace("{tier}", "")));
            // 更新其他物品信息
            if (isQuickBuy(player, title)) {
                PlayerQuickBuyCache quickBuyCache = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
                if (quickBuyCache == null) {
                    return;
                }
                for (QuickBuyElement quickBuyElement : quickBuyCache.getElements()) {
                    CategoryContent categoryContent = quickBuyElement.getCategoryContent();
                    int c_slot = quickBuyElement.getSlot();
                    int c_price = getCategoryContentPrice(shopCache, categoryContent);
                    setContentInventory(player, categoryContent, shopCache, shopInv, title, c_slot, c_price, true);
                }
            } else {
                for (CategoryContent categoryContent : category.getCategoryContentList()) {
                    int c_slot = categoryContent.getSlot();
                    int c_price = getCategoryContentPrice(shopCache, categoryContent);
                    setContentInventory(player, categoryContent, shopCache, shopInv, title, c_slot, c_price, false);
                }
            }
            player.updateInventory();
            shopCache.setCategoryWeight(category, contentWeight);
        } else {
            player.sendMessage(Language.getMsg(player, "shop-insuff-money").replace("{currency}", XpResMode.currency).replace("{amount}", String.valueOf(price - player.getLevel())));
            Sounds.playSound("shop-insufficient-money", player);
        }
    }

    private static boolean isAffordable(Player player, int price) {
        return player.getLevel() >= price;
    }

    private static void takeMoney(Player player, int price) {
        player.setLevel(player.getLevel() - price);
    }
}
