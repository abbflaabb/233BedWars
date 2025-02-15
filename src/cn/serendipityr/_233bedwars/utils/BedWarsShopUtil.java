package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.addons.XpResMode;
import cn.serendipityr._233bedwars.config.ConfigManager;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import com.andrei1058.bedwars.shop.quickbuy.QuickBuyElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

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
            LogUtil.consoleLog("&9233BedWars &3&l > &e[BedWarsShopUtil] &c发生致命错误！");
            e.printStackTrace();
        }
    }

    public static void handleShopOpen(Player player, Inventory shopInv) {
        if (!XpResMode.isExpMode(player)) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (isQuickBuy(player, shopInv)) {
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
                setContentInventory(player, content, shopCache, shopInv, slot, price, true);
            }
        } else {
            ShopCategory category = getCategoryFromInventory(player, shopInv);
            if (category == null) {
                return;
            }
            // 处理其他分类
            ShopCache shopCache = ShopCache.getShopCache(uuid);
            for (CategoryContent content : category.getCategoryContentList()) {
                int slot = content.getSlot();
                int price = getCategoryContentPrice(shopCache, content);
                setContentInventory(player, content, shopCache, shopInv, slot, price, false);
            }
        }
    }

    public static boolean handleShopClick(Player player, Inventory shopInv, int slot) {
        if (!XpResMode.isExpMode(player)) {
            return false;
        }
        UUID uuid = player.getUniqueId();
        CategoryContent content;
        if (isQuickBuy(player, shopInv)) {
            // 处理快速购买
            PlayerQuickBuyCache quickBuyCache = PlayerQuickBuyCache.getQuickBuyCache(uuid);
            if (quickBuyCache == null) {
                return false;
            }
            content = getCategoryContentFromQuickBuy(quickBuyCache, slot);
        } else {
            ShopCategory category = getCategoryFromInventory(player, shopInv);
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
        buyItem(player, shopCache, content, shopInv, slot);
        return true;
    }

    private static boolean isQuickBuy(Player player, Inventory shopInv) {
        return ProviderUtil.bw.getPlayerLanguage(player).m("shop-items-messages.inventory-name").equals(shopInv.getTitle());
    }

    private static ShopCategory getCategoryFromInventory(Player player, Inventory shopInv) {
        for (ShopCategory shopCategory : ShopManager.getShop().getCategoryList()) {
            String name = getCategoryName(player, shopCategory.getName());
            if (name.equals(shopInv.getTitle())) {
                return shopCategory;
            }
        }
        return null;
    }

    private static String getCategoryName(Player player, String category) {
        return ProviderUtil.bw.getPlayerLanguage(player).m("shop-items-messages." + category + ".inventory-name");
    }

    private static void setContentInventory(Player player, CategoryContent content, ShopCache shopCache, Inventory shopInv, int slot, int price, boolean isQuickBuy) {
        List<String> lores = getCategoryContentLore(player, content);

        ItemStack itemStack = shopInv.getItem(slot);
        if (itemStack == null) {
            return;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();

        boolean affordable = isAffordable(player, price);
        lores.replaceAll(s -> s
                .replace("{buy_status}", getBuyStatus(player, content, shopCache, affordable))
                .replace("{quick_buy}", getQuickBuyTips(player, isQuickBuy))
                .replace("{tier}", intToRoman(getContentTier(shopCache, content)))
                .replace("{cost}", XpResMode.currency_color + price)
                .replace("{currency}", XpResMode.currency_color + XpResMode.currency)
                .replace("&", "§"));
        if (affordable) {
            itemMeta.setDisplayName(getCategoryContentName(player, content).replace("{color}", "§a"));
        } else {
            itemMeta.setDisplayName(getCategoryContentName(player, content).replace("{color}", "§c"));
        }
        itemMeta.setLore(lores);
        if (ConfigManager.addon_shopItemAddon) {
            if (itemStack.getType().toString().contains("SKULL")) {
                String texture = ShopItemAddon.getSkullTexture(content.getIdentifier().split("\\.")[2]);
                if (!texture.trim().isEmpty()) {
                    SkullMeta skullMeta = (SkullMeta) itemMeta;
                    GameProfile profile = new GameProfile(UUID.randomUUID(), null);
                    profile.getProperties().put("textures", new Property("textures", texture));
                    try {
                        Field profileField = skullMeta.getClass().getDeclaredField("profile");
                        profileField.setAccessible(true);
                        profileField.set(skullMeta, profile);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    itemStack.setItemMeta(skullMeta);
                }
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
        return shopCache.getContentTier(content.getIdentifier());
    }

    private static String getQuickBuyTips(Player player, boolean isQuickBuy) {
        return Language.getMsg(player, isQuickBuy ? Messages.SHOP_LORE_QUICK_REMOVE : Messages.SHOP_LORE_QUICK_ADD);
    }

    private static Integer getCategoryContentPrice(ShopCache shopCache, CategoryContent content) {
        int tier = shopCache.getContentTier(content.getIdentifier());
        IContentTier contentTier = content.getContentTiers().get(tier - 1);
        return XpResMode.calcExpLevel(contentTier.getCurrency(), contentTier.getPrice());
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

    private static void buyItem(Player player, ShopCache shopCache, CategoryContent content, Inventory shopInv, int slot) {
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
                contentTier = content.getContentTiers().get(tier - 1);
            } else {
                if (!shopCache.hasCachedItem(content)) {
                    contentTier = content.getContentTiers().get(0);
                } else {
                    contentTier = content.getContentTiers().get(tier);
                }
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
            if (isQuickBuy(player, shopInv)) {
                PlayerQuickBuyCache quickBuyCache = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
                if (quickBuyCache == null) {
                    return;
                }
                for (QuickBuyElement quickBuyElement : quickBuyCache.getElements()) {
                    CategoryContent categoryContent = quickBuyElement.getCategoryContent();
                    int c_slot = quickBuyElement.getSlot();
                    int c_price = getCategoryContentPrice(shopCache, categoryContent);
                    setContentInventory(player, categoryContent, shopCache, shopInv, c_slot, c_price, true);
                }
            } else {
                for (CategoryContent categoryContent : category.getCategoryContentList()) {
                    int c_slot = categoryContent.getSlot();
                    int c_price = getCategoryContentPrice(shopCache, categoryContent);
                    setContentInventory(player, categoryContent, shopCache, shopInv, c_slot, c_price, false);
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
