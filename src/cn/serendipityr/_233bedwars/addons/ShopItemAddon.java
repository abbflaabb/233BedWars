package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars.addons.shopItems.RecoverBed;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.QuickBuyButton;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.github.paperspigot.Title;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ShopItemAddon {
    static List<String> shop_layout = new ArrayList<>();
    static List<String> shopItems = new ArrayList<>();
    static YamlConfiguration shopItemsYml;

    public static void loadConfig(YamlConfiguration cfg) {
        shop_layout = cfg.getStringList("shop_layout");
        shopItems.addAll(cfg.getConfigurationSection("items").getKeys(false));
        shopItemsYml = cfg;
        RecoverBed.loadConfig(cfg);
    }

    public static void init() {
        editShop();
        for (String item : shopItems) {
            loadShopItem(item);
        }
    }

    public static void handleBedDestroy(IArena arena, ITeam team) {
        if (RecoverBed.settings_recover_bed_enable) {
            RecoverBed.handleBedDestroy(arena, team);
        }
    }

    public static boolean handleShopBuy(Player player, IArena arena, ICategoryContent content) {
        if (RecoverBed.settings_recover_bed_enable && RecoverBed.handleShopBuy(player, arena, content)) {
            return true;
        }
        return false;
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (!ProviderUtil.bw.getArenaUtil().isPlaying(player)) {
            return false;
        }

        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        ITeam team = arena.getTeam(player);

        if (RecoverBed.settings_recover_bed_enable && RecoverBed.handleItemInteract(player, item, arena, team)) {
            return true;
        }

        return false;
    }

    public static boolean isBeforeInstant(Instant instant, long seconds) {
        return Instant.now().isBefore(instant.plusSeconds(seconds));
    }

    public static void sendGlobalMessage(IArena arena, String msg) {
        for (Player arenaPlayer : arena.getPlayers()) {
            arenaPlayer.sendMessage(msg);
        }
    }

    public static void sendTeamMessage(ITeam team, String msg) {
        for (Player teamPlayer : team.getMembers()) {
            teamPlayer.sendMessage(msg);
        }
    }

    public static void sendTeamTitle(ITeam team, String title, String subTitle, int stay) {
        for (Player teamPlayer : team.getMembers()) {
            teamPlayer.sendTitle(new Title(title, subTitle, 0, stay, 0));
        }
    }

    public static void playTeamSound(ITeam team, Sound sound, float volume, float pitch) {
        for (Player teamPlayer : team.getMembers()) {
            teamPlayer.playSound(teamPlayer.getLocation(), sound, volume, pitch);
        }
    }

    public static boolean compareAddonItem(Player player, ItemStack itemStack, String section) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return Language.getMsg(player, "shop-items-messages." + section + "-name").equals(itemStack.getItemMeta().getDisplayName());
        }
        return false;
    }

    public static void editShop() {
        for (String _layout : shop_layout) {
            String[] layout = _layout.split(":");
            int slot = Integer.parseInt(layout[0]);
            String category = layout[1];
            if ("quick-buy-category".equals(category)) {
                setQuickButtonSlot(ShopManager.getShop(), slot);
                continue;
            }
            for (ShopCategory shopCategory : ShopManager.getShop().getCategoryList()) {
                if (shopCategory.getName().equals(category)) {
                    setShopCategorySlot(shopCategory, slot);
                }
            }
        }
    }

    private static void setQuickButtonSlot(ShopIndex shopIndex, int slot) {
        try {
            Field quickBuyButtonField = ShopIndex.class.getDeclaredField("quickBuyButton");
            quickBuyButtonField.setAccessible(true);
            Field quickBuyButtonItemField = QuickBuyButton.class.getDeclaredField("itemStack");
            quickBuyButtonItemField.setAccessible(true);
            ItemStack button = (ItemStack) quickBuyButtonItemField.get(ShopManager.getShop().getQuickBuyButton());
            quickBuyButtonField.set(shopIndex, new QuickBuyButton(slot, button, Messages.SHOP_QUICK_BUY_NAME, Messages.SHOP_QUICK_BUY_LORE));
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.consoleLog("&9233BedWars &3&l > &e[ShopItemAddon] &c发生致命错误！");
        }
    }

    private static void setShopCategorySlot(ShopCategory shopCategory, int slot) {
        try {
            Field shopCategorySlot = ShopCategory.class.getDeclaredField("slot");
            shopCategorySlot.setAccessible(true);
            shopCategorySlot.set(shopCategory, slot);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.consoleLog("&9233BedWars &3&l > &e[ShopItemAddon] &c发生致命错误！");
        }
    }

    private static void loadShopItem(String section) {
        String category = shopItemsYml.getString("items." + section + ".category");
        int slot = shopItemsYml.getInt("items." + section + ".slot");
        String material = shopItemsYml.getString("items." + section + ".material");
        String cost = shopItemsYml.getString("items." + section + ".cost");

        ShopCategory shopCategory = getCategory(category);
        if (shopCategory == null) {
            return;
        }

        if (section.equals("recover-bed") && RecoverBed.settings_recover_bed_enable) {
            RecoverBed.recover_bed_material = material;
            RecoverBed.recover_bed_section = "shop-items-messages." + category + ".content-item-" + section;
            addBedWarsShopItemCfg(section, category, slot, material, cost);
            addCategoryContent(shopCategory, new CategoryContent(shopCategory.getName() + ".category-content." + section, section, shopCategory.getName(), ProviderUtil.bw.getConfigs().getShopConfig().getYml(), shopCategory));
        }
    }

    private static void addBedWarsShopItemCfg(String section, String category, int slot, String material, String cost) {
        YamlConfiguration bwCfg = ProviderUtil.bw.getConfigs().getShopConfig().getYml();
        StringBuilder path = new StringBuilder(category + "." + ConfigPath.SHOP_CATEGORY_CONTENT_PATH + "." + section + ".");
        bwCfg.addDefault(path + ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_SLOT, slot);
        bwCfg.addDefault(path + ConfigPath.SHOP_CATEGORY_CONTENT_IS_PERMANENT, false);
        bwCfg.addDefault(path + ConfigPath.SHOP_CATEGORY_CONTENT_IS_DOWNGRADABLE, false);
        path.append(ConfigPath.SHOP_CATEGORY_CONTENT_CONTENT_TIERS + ".tier1.");
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_MATERIAL, material);
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA, 0);
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT, 1);
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED, false);
        String[] _cost = cost.split(":");
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY, _cost[0]);
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST, Integer.parseInt(_cost[1]));
        path.append(ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH + ".").append(section).append(".");
        bwCfg.addDefault(path + "material", material);
        bwCfg.addDefault(path + "data", 0);
        bwCfg.addDefault(path + "amount", 1);
    }

    private static ShopCategory getCategory(String name) {
        return ShopManager.getShop().getCategoryList().stream().filter(category -> category.getName().equals(name)).findFirst().orElse(null);
    }

    private static void addCategoryContent(ShopCategory shopCategory, CategoryContent content) {
        if (shopCategory == null) {
            return;
        }
        if (content.isLoaded()) {
            shopCategory.getCategoryContentList().add(content);
        }
    }
}
