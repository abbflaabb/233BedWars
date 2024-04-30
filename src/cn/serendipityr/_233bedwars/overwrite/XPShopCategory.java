package cn.serendipityr._233bedwars.overwrite;

import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class XPShopCategory extends ShopCategory {
    private final String name;
    private String invNamePath;

    public XPShopCategory(String path, YamlConfiguration yml) {
        super(path, yml);
        name = path;
        this.invNamePath = "shop-items-messages.%category%.inventory-name".replace("%category%", path);
        List<CategoryContent> categoryContents = new ArrayList<>();
        Field nameField = null;
        try {
            nameField = CategoryContent.class.getDeclaredField("contentName");
            for (CategoryContent content : getCategoryContentList()) {
                categoryContents.add(new XPCategoryContent(content.getIdentifier(), (String) nameField.get(content), path, yml, this));
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    @Override
    public void open(Player player, ShopIndex index, ShopCache shopCache) {
        if (player.getOpenInventory().getTopInventory() == null) return;
        ShopIndex.indexViewers.remove(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, index.getInvSize(), Language.getMsg(player, invNamePath));

        inv.setItem(index.getQuickBuyButton().getSlot(), index.getQuickBuyButton().getItemStack(player));

        for (ShopCategory sc : index.getCategoryList()) {
            inv.setItem(sc.getSlot(), sc.getItemStack(player));
        }

        index.addSeparator(player, inv);

        inv.setItem(getSlot() + 9, index.getSelectedItem(player));

        shopCache.setSelectedCategory(getSlot());

        for (CategoryContent cc : getCategoryContentList()) {
            inv.setItem(cc.getSlot(), cc.getItemStack(player, shopCache));
        }

        player.openInventory(inv);
        if (!categoryViewers.contains(player.getUniqueId())) {
            categoryViewers.add(player.getUniqueId());
        }
    }
}
