package cn.serendipityr._233bedwars.overwrite;

import cn.serendipityr._233bedwars.addons.XpResMode;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.configuration.Sounds;
import com.andrei1058.bedwars.shop.ShopCache;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XPCategoryContent extends CategoryContent {
    private ShopCategory father;
    private String itemNamePath;
    private String itemLorePath;
    private byte weight;


    public XPCategoryContent(String path, String name, String categoryName, YamlConfiguration yml, ShopCategory father) {
        super(path, name, categoryName, yml, father);
        this.itemNamePath = getItemNamePath();
        this.itemLorePath = getItemLorePath();
        this.weight = getWeight();
        this.father = father;
    }

    private byte getWeight() {
        try {
            Field itemNamePathField = CategoryContent.class.getDeclaredField("weight");
            itemNamePathField.setAccessible(true);
            return (byte) itemNamePathField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return 0;
    }

    private String getItemNamePath() {
        try {
            Field itemNamePathField = CategoryContent.class.getDeclaredField("itemNamePath");
            itemNamePathField.setAccessible(true);
            return (String) itemNamePathField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }

    private String getItemLorePath() {
        try {
            Field itemNamePathField = CategoryContent.class.getDeclaredField("itemLorePath");
            itemNamePathField.setAccessible(true);
            return (String) itemNamePathField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }

    @Override
    public void execute(Player player, ShopCache shopCache, int slot) {
        if (shopCache.getCategoryWeight(this.father) <= this.weight) {
            if (shopCache.getContentTier(this.getIdentifier()) > getContentTiers().size()) {
                Bukkit.getLogger().severe("Wrong tier order at: " + this.getIdentifier());
            } else {
                IContentTier ct;
                if (shopCache.getContentTier(this.getIdentifier()) == getContentTiers().size()) {
                    if (this.isPermanent() && shopCache.hasCachedItem(this)) {
                        player.sendMessage(Language.getMsg(player, Messages.SHOP_ALREADY_BOUGHT));
                        Sounds.playSound("shop-insufficient-money", player);
                        return;
                    }

                    ct = getContentTiers().get(shopCache.getContentTier(this.getIdentifier()) - 1);
                } else if (!shopCache.hasCachedItem(this)) {
                    ct = getContentTiers().get(0);
                } else {
                    ct = getContentTiers().get(shopCache.getContentTier(this.getIdentifier()));
                }

                int money = calculateMoney(player, ct.getCurrency());
                if (money < ct.getPrice()) {
                    player.sendMessage(Language.getMsg(player, Messages.SHOP_INSUFFICIENT_MONEY).replace("{currency}", Language.getMsg(player, getCurrencyMsgPath())).replace("{amount}", String.valueOf(ct.getPrice() - money)));
                    Sounds.playSound("shop-insufficient-money", player);
                } else {
                    ShopBuyEvent event;
                    Bukkit.getPluginManager().callEvent(event = new ShopBuyEvent(player, Arena.getArenaByPlayer(player), this));
                    if (!event.isCancelled()) {
                        takeMoney(player, ct.getCurrency(), ct.getPrice());
                        shopCache.upgradeCachedItem(this, slot);
                        this.giveItems(player, shopCache, Arena.getArenaByPlayer(player));
                        Sounds.playSound("shop-bought", player);
                        if (this.itemNamePath != null && Language.getPlayerLanguage(player).getYml().get(this.itemNamePath) != null) {
                            player.sendMessage(Language.getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", ChatColor.stripColor(Language.getMsg(player, this.itemNamePath))).replace("{color}", "").replace("{tier}", ""));
                        } else {
                            ItemStack displayItem = ct.getItemStack();
                            if (displayItem.getItemMeta() != null && displayItem.getItemMeta().hasDisplayName()) {
                                player.sendMessage(Language.getMsg(player, Messages.SHOP_NEW_PURCHASE).replace("{item}", displayItem.getItemMeta().getDisplayName()));
                            }
                        }

                        shopCache.setCategoryWeight(this.father, this.weight);
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getItemStack(Player player, ShopCache shopCache) {
        IContentTier ct;
        if (shopCache.getContentTier(this.getIdentifier()) == this.getContentTiers().size()) {
            ct = this.getContentTiers().get(this.getContentTiers().size() - 1);
        } else if (shopCache.hasCachedItem(this)) {
            ct = this.getContentTiers().get(shopCache.getContentTier(this.getIdentifier()));
        } else {
            ct = this.getContentTiers().get(shopCache.getContentTier(this.getIdentifier()) - 1);
        }

        ItemStack i = ct.getItemStack();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im = i.getItemMeta().clone();
            boolean canAfford = calculateMoney(player, ct.getCurrency()) >= ct.getPrice();
            PlayerQuickBuyCache qbc = PlayerQuickBuyCache.getQuickBuyCache(player.getUniqueId());
            boolean hasQuick = qbc != null && this.hasQuick(qbc);
            String color = Language.getMsg(player, canAfford ? "shop-items-messages.can-buy-color" : "shop-items-messages.cant-buy-color");
            String translatedCurrency = Language.getMsg(player, getCurrencyMsgPath());
            ChatColor cColor = getCurrencyColor(ct.getCurrency());
            int tierI = ct.getValue();
            String tier = getRomanNumber(tierI);
            String buyStatus;
            if (this.isPermanent() && shopCache.hasCachedItem(this) && shopCache.getCachedItem(this).getTier() == this.getContentTiers().size()) {
                if (!BedWars.nms.isArmor(i)) {
                    buyStatus = Language.getMsg(player, "shop-lore-status-tier-maxed");
                } else {
                    buyStatus = Language.getMsg(player, "shop-lore-status-armor");
                }
            } else if (!canAfford) {
                buyStatus = Language.getMsg(player, "shop-lore-status-cant-afford").replace("{currency}", translatedCurrency);
            } else {
                buyStatus = Language.getMsg(player, "shop-lore-status-can-buy");
            }

            im.setDisplayName(Language.getMsg(player, this.itemNamePath).replace("{color}", color).replace("{tier}", tier));
            List<String> lore = new ArrayList<>();
            Iterator<String> var16 = Language.getList(player, this.itemLorePath).iterator();

            while(true) {
                String s;
                while(true) {
                    if (!var16.hasNext()) {
                        im.setLore(lore);
                        i.setItemMeta(im);
                        return i;
                    }

                    s = var16.next();
                    if (!s.contains("{quick_buy}")) {
                        break;
                    }

                    if (hasQuick) {
                        if (!ShopIndex.getIndexViewers().contains(player.getUniqueId())) {
                            continue;
                        }

                        s = Language.getMsg(player, "shop-lore-quick-remove");
                        break;
                    }

                    s = Language.getMsg(player, "shop-lore-quick-add");
                    break;
                }

                String var10000 = s.replace("{tier}", tier).replace("{color}", color);
                String var10002 = String.valueOf(cColor);
                var10000 = var10000.replace("{cost}", var10002 + String.valueOf(ct.getPrice()));
                var10002 = String.valueOf(cColor);
                s = var10000.replace("{currency}", var10002 + translatedCurrency).replace("{buy_status}", buyStatus);
                lore.add(s);
            }
        } else {
            return i;
        }
    }

    public static String getCurrencyMsgPath() {
        return "等级";
    }

    public static int calculateMoney(Player player, Material currency) {
        return player.getLevel();
    }

    public static void takeMoney(Player player, Material currency, int amount) {
        player.setLevel(player.getLevel() - XpResMode.calcExpLevel(currency, amount));
    }
}
