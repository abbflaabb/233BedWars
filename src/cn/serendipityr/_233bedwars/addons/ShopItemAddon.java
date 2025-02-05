package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.shopItems.*;
import cn.serendipityr._233bedwars.utils.ActionBarUtil;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.CategoryContent;
import com.andrei1058.bedwars.shop.main.QuickBuyButton;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import com.andrei1058.bedwars.shop.main.ShopIndex;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.github.paperspigot.Title;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;

public class ShopItemAddon {
    static List<String> shop_layout = new ArrayList<>();
    static List<String> shopItems = new ArrayList<>();
    static YamlConfiguration shopItemsYml;
    static HashMap<String, Integer> cooling_items = new HashMap<>();
    static Integer cooling_progress_length;
    static String cooling_progress_unit;
    static String cooling_progress_color_current;
    static String cooling_progress_color_left;
    static String cooling_actionbar;

    public static void loadConfig(YamlConfiguration cfg) {
        shop_layout = cfg.getStringList("shop_layout");
        shopItems.addAll(cfg.getConfigurationSection("items").getKeys(false));
        shopItemsYml = cfg;

        List<String> cooling = cfg.getStringList("settings.cooling.items");
        for (String str : cooling) {
            String[] _str = str.split(":");
            cooling_items.put(_str[0], Integer.parseInt(_str[1]));
        }
        cooling_progress_length = cfg.getInt("settings.cooling.progress.length");
        cooling_progress_unit = cfg.getString("settings.cooling.progress.unit");
        cooling_progress_color_current = cfg.getString("settings.cooling.progress.color_current").replace("&", "§");
        cooling_progress_color_left = cfg.getString("settings.cooling.progress.color_left").replace("&", "§");
        cooling_actionbar = cfg.getString("messages.cooling_actionbar").replace("&", "§");

        RecoverBed.loadConfig(cfg);
        SuicideBomber.loadConfig(cfg);
        Landmine.loadConfig(cfg);
        FlightFirework.loadConfig(cfg);
        LuckyBlock.loadConfig(cfg);
        Grenade.loadConfig(cfg);
        BridgeChicken.loadConfig(cfg);
        BridgeCat.loadConfig(cfg);
        Pillar.loadConfig(cfg);
        ToxicBall.loadConfig(cfg);
        ObsidianBreaker.loadConfig(cfg);
        PortalScroll.loadConfig(cfg);
    }

    public static void init() {
        editShop();
        for (String item : shopItems) {
            loadShopItem(item);
        }
    }

    public static void initGame(IArena arena) {
        if (RecoverBed.settings_recover_bed_enable) {
            RecoverBed.initArena(arena);
        }
    }

    public static void resetGame(IArena arena) {
        if (RecoverBed.settings_recover_bed_enable) {
            RecoverBed.resetArena(arena);
        }
    }

    public static boolean handleBlockPlace(Player player, Block block, ItemStack item) {
        if (!ProviderUtil.bw.getArenaUtil().isPlaying(player)) {
            return false;
        } else if (RecoverBed.settings_recover_bed_enable && RecoverBed.handleBlockPlace(block)) {
            return true;
        } else if ((Landmine.settings_landmine_enable || Landmine.settings_light_landmine_enable) && Landmine.handleBlockPlace(player, block)) {
            return true;
        } else if (LuckyBlock.settings_lucky_block_enable && LuckyBlock.handleBlockPlace(player, block, item)) {
            return true;
        } else if (Pillar.settings_pillar_enable && Pillar.handleBlockPlace(player, block, item)) {
            return true;
        }

        return false;
    }

    public static void handleBedDestroy(IArena arena, ITeam team) {
        if (RecoverBed.settings_recover_bed_enable) {
            RecoverBed.handleBedDestroy(arena, team);
        }
    }

    public static boolean handleBlockDestroy(Player player, Block block) {
        if (!ProviderUtil.bw.getArenaUtil().isPlaying(player)) {
            return false;
        }

        if ((Landmine.settings_landmine_enable || Landmine.settings_light_landmine_enable) && Landmine.handleBlockDestroy(block)) {
            return true;
        }

        if (LuckyBlock.settings_lucky_block_enable && LuckyBlock.handleBlockDestroy(player, block)) {
            return true;
        }

        return false;
    }

    public static boolean handleBlockInteract(Player player, Block block) {
        if (!ProviderUtil.bw.getArenaUtil().isPlaying(player)) {
            return false;
        }

        if (Landmine.settings_light_landmine_enable || Landmine.settings_landmine_enable) {
            Landmine.onBlockInteract(player, block);
        }

        return false;
    }

    public static boolean handleFireworkExplode(Firework firework) {
        if (FlightFirework.settings_flight_firework_enable && FlightFirework.handleFireworkExplode(firework)) {
            return true;
        }

        return false;
    }

    public static boolean handleBlockItemInteract(Player player, ItemStack item, Block block) {
        if (ObsidianBreaker.settings_obsidian_breaker_enable && ObsidianBreaker.handleBlockItemInteract(player, item, block)) {
            return true;
        }

        return false;
    }

    public static boolean handleShopBuy(Player player, IArena arena, ICategoryContent content) {
        if (RecoverBed.settings_recover_bed_enable && RecoverBed.handleShopBuy(player, arena, content)) {
            return true;
        } else if (SuicideBomber.settings_suicide_bomber_enable && handleShopBuy(player, content, "suicide_bomber", SuicideBomber.suicide_bomber_section)) {
            return true;
        } else if (Landmine.settings_landmine_enable && handleShopBuy(player, content, "landmine", Landmine.landmine_section)) {
            return true;
        } else if (Landmine.settings_light_landmine_enable && handleShopBuy(player, content, "light_landmine", Landmine.light_landmine_section)) {
            return true;
        } else if (FlightFirework.settings_flight_firework_enable && handleShopBuy(player, content, "flight_firework", FlightFirework.flight_firework_section)) {
            return true;
        } else if (LuckyBlock.settings_lucky_block_enable && handleShopBuy(player, content, "lucky_block", LuckyBlock.lucky_block_section)) {
            return true;
        } else if (Grenade.settings_grenade_enable && handleShopBuy(player, content, "grenade", Grenade.grenade_section)) {
            return true;
        } else if (BridgeChicken.settings_bridge_chicken_enable && handleShopBuy(player, content, "bridge_chicken", BridgeChicken.bridge_chicken_section)) {
            return true;
        } else if (BridgeCat.settings_bridge_cat_enable && handleShopBuy(player, content, "bridge_cat", BridgeCat.bridge_cat_section)) {
            return true;
        } else if (Pillar.settings_pillar_enable && handleShopBuy(player, content, "pillar", Pillar.pillar_section)) {
            return true;
        } else if (ToxicBall.settings_toxic_ball_enable && handleShopBuy(player, content, "toxic_ball", ToxicBall.toxic_ball_section)) {
            return true;
        } else if (ObsidianBreaker.settings_obsidian_breaker_enable && handleShopBuy(player, content, "obsidian_breaker", ObsidianBreaker.obsidian_breaker_section)) {
            return true;
        } else if (PortalScroll.settings_portal_scroll_enable && handleShopBuy(player, content, "portal_scroll", PortalScroll.portal_scroll_section)) {
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
        } else if (SuicideBomber.settings_suicide_bomber_enable && SuicideBomber.handleItemInteract(player, item)) {
            return true;
        } else if (FlightFirework.settings_flight_firework_enable && FlightFirework.handleItemInteract(player, item)) {
            return true;
        } else if (Grenade.settings_grenade_enable && Grenade.handleItemInteract(player, item)) {
            return true;
        } else if (BridgeChicken.settings_bridge_chicken_enable && BridgeChicken.handleItemInteract(player, item)) {
            return true;
        } else if (BridgeCat.settings_bridge_cat_enable && BridgeCat.handleItemInteract(player, item)) {
            return true;
        } else if (ToxicBall.settings_toxic_ball_enable && ToxicBall.handleItemInteract(player, item)) {
            return true;
        } else if (PortalScroll.settings_portal_scroll_enable && PortalScroll.handleItemInteract(player, item)) {
            return true;
        }

        return false;
    }

    public static boolean handlePlayerMovement(Player player, Location from, Location to) {
        if (PortalScroll.settings_portal_scroll_enable && PortalScroll.handlePlayerMovement(player, from, to)) {
            return true;
        }

        return false;
    }

    public static void handleBlockRedstone(Block block, int old_state, int new_state) {
        Landmine.onBlockRedstone(block, old_state, new_state);
    }

    public static boolean handleEntityDeath(Entity entity) {
        if (BridgeChicken.settings_bridge_chicken_enable && BridgeChicken.handleEntityDeath(entity)) {
            return true;
        } else if (BridgeCat.settings_bridge_cat_enable && BridgeCat.handleEntityDeath(entity)) {
            return true;
        }

        return false;
    }

    public static void setCooling(Player player, String identity) {
        if (cooling_items.containsKey(identity)) {
            player.setMetadata(identity, new FixedMetadataValue(_233BedWars.getInstance(), ""));
            Bukkit.getScheduler().runTaskAsynchronously(_233BedWars.getInstance(), () -> {
                int total = cooling_items.get(identity) * 10;
                int cooling = total;
                while (cooling > 0) {
                    cooling--;
                    int current = Math.round((float) cooling / total * cooling_progress_length);
                    int left = cooling_progress_length - current;
                    String progress = cooling_progress_color_current + String.join("", Collections.nCopies(left, cooling_progress_unit)) + cooling_progress_color_left + String.join("", Collections.nCopies(current, cooling_progress_unit));
                    String msg = cooling_actionbar
                            .replace("{progress}", progress)
                            .replace("{cooling_time}", String.valueOf((double) cooling / 10))
                            .replace("{item}", Language.getMsg(player, sectionMap.get(identity) + "-name"));
                    ActionBarUtil.send(player, msg);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
                player.removeMetadata(identity, _233BedWars.getInstance());
            });
        }
    }

    public static boolean handleShopBuy(Player player, ICategoryContent content, String identity, String section) {
        if (content.getIdentifier().contains(identity)) {
            for (IContentTier tier : content.getContentTiers()) {
                for (IBuyItem buyItem : tier.getBuyItemsList()) {
                    ItemStack itemStack = buyItem.getItemStack().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(Language.getMsg(player, section + "-name").replace("{color}", "§e"));
                    if (itemStack.getType().toString().contains("SKULL")) {
                        String texture = ShopItemAddon.getSkullTexture(identity);
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
                    itemStack.setItemMeta(itemMeta);
                    buyItem.setItemStack(itemStack);
                }
            }
        }
        return false;
    }

    public static boolean checkCooling(Player player, String identity) {
        return player.hasMetadata(identity);
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
            return Language.getMsg(player, section + "-name").replace("{color}", "§e").equals(itemStack.getItemMeta().getDisplayName());
        }
        return false;
    }

    public static void consumeItem(Player player, ItemStack item, int count) {
        if (item.getAmount() - count <= 0) {
            player.setItemInHand(new ItemStack(Material.AIR));
        } else {
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - count);
        }
        player.updateInventory();
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

    public static String getSkullTexture(String identity) {
        String texture = shopItemsYml.getString("items." + identity + ".texture");
        return texture == null ? "" : texture;
    }

    public static String getSkullTextureFromItemStack(ItemStack skullItem) {
        if (!skullItem.getType().toString().contains("SKULL")) {
            return "";
        }

        SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = (GameProfile) profileField.get(skullMeta);
            if (profile != null) {
                for (Property property : profile.getProperties().get("textures")) {
                    return property.getValue();
                }
            }
        } catch (Exception ignored) {
        }

        return "";
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

    static HashMap<String, String> sectionMap = new HashMap<>();

    private static void loadShopItem(String section) {
        boolean enable = shopItemsYml.getBoolean("settings." + section + ".enable");
        String category = shopItemsYml.getString("items." + section + ".category");
        int slot = shopItemsYml.getInt("items." + section + ".slot");
        String material = shopItemsYml.getString("items." + section + ".material");
        String cost = shopItemsYml.getString("items." + section + ".cost");

        ShopCategory shopCategory = getCategory(category);
        if (shopCategory == null) {
            return;
        }

        if (enable) {
            addBedWarsShopItemCfg(section, category, slot, material, cost);
            addCategoryContent(shopCategory, new CategoryContent(shopCategory.getName() + ".category-content." + section, section, shopCategory.getName(), ProviderUtil.bw.getConfigs().getShopConfig().getYml(), shopCategory));
        }

        String secLoc = "shop-items-messages." + category + ".content-item-" + section;
        sectionMap.put(section, secLoc);

        switch (section) {
            case "recover_bed":
                RecoverBed.init(enable, material, secLoc);
                break;
            case "suicide_bomber":
                SuicideBomber.init(enable, material, secLoc);
                break;
            case "landmine":
                Landmine.init(enable, material, secLoc);
                break;
            case "light_landmine":
                Landmine.light_init(enable, material, secLoc);
                break;
            case "flight_firework":
                FlightFirework.init(enable, material, secLoc);
                break;
            case "lucky_block":
                LuckyBlock.init(enable, material, secLoc);
                break;
            case "grenade":
                Grenade.init(enable, material, secLoc);
                break;
            case "bridge_chicken":
                BridgeChicken.init(enable, material, secLoc, getSkullTexture(section));
                break;
            case "bridge_cat":
                BridgeCat.init(enable, material, secLoc, getSkullTexture(section));
                break;
            case "pillar":
                Pillar.init(enable, material, secLoc);
                break;
            case "toxic_ball":
                ToxicBall.init(enable, material, secLoc);
                break;
            case "obsidian_breaker":
               ObsidianBreaker.init(enable, material, secLoc);
                break;
            case "portal_scroll":
                PortalScroll.init(enable, material, secLoc);
                break;
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
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_DATA, getSkullTexture(section).trim().isEmpty() ? 0 : 3);
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_AMOUNT, 1);
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_ITEM_ENCHANTED, false);
        String[] _cost = cost.split(":");
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_CURRENCY, _cost[0]);
        bwCfg.addDefault(path + ConfigPath.SHOP_CONTENT_TIER_SETTINGS_COST, Integer.parseInt(_cost[1]));
        path.append(ConfigPath.SHOP_CONTENT_BUY_ITEMS_PATH + ".").append(section).append(".");
        bwCfg.addDefault(path + "material", material);
        bwCfg.addDefault(path + "data", getSkullTexture(section).trim().isEmpty() ? 0 : 3);
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
