package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.QuickBuyButton;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopItemAddon {
    static YamlConfiguration shopYml;
    static List<String> shop_layout = new ArrayList<>();
    static HashMap<Integer, String> categories = new HashMap<>();

    public static void loadConfig(YamlConfiguration cfg) {
        shop_layout = cfg.getStringList("shop_layout");
        init(ProviderUtil.bw == null);
    }

    public static void init(boolean preload) {
        if (preload) {
            return;
        }
        shopYml = ProviderUtil.bw.getConfigs().getShopConfig().getYml();
        editShop();
    }

    public static void editShop() {
        categories.clear();
        for (String _layout : shop_layout) {
            String[] layout = _layout.split(":");
            int slot = Integer.parseInt(layout[0]);
            String category = layout[1];
            categories.put(slot, category);
            if ("quick-buy-category".equals(category)) {
                setQuickButtonSlot(ShopManager.getShop(), slot);
                if (XpResMode.xpShop != null) {
                    setQuickButtonSlot(XpResMode.xpShop, slot);
                }
                continue;
            }
            for (ShopCategory shopCategory : ShopManager.getShop().getCategoryList()) {
                if (shopCategory.getName().equals(category)) {
                    setShopCategorySlot(shopCategory, slot);
                }
            }
            if (XpResMode.xpShop != null) {
                for (ShopCategory shopCategory : XpResMode.xpShop.getCategoryList()) {
                    if (shopCategory.getName().equals(category)) {
                        setShopCategorySlot(shopCategory, slot);
                    }
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
}
