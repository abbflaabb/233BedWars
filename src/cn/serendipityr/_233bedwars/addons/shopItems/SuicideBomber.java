package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class SuicideBomber {
    public static String suicide_bomber_material;
    public static String suicide_bomber_section;
    public static Boolean settings_suicide_bomber_enable = false;
    public static String settings_suicide_bomber_use_sound;
    public static Integer settings_suicide_bomber_active_time;
    public static String messages_suicide_bomber_active;
    public static String messages_suicide_bomber_fuse;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_suicide_bomber_use_sound = cfg.getString("settings.suicide_bomber.use_sound");
        settings_suicide_bomber_active_time = cfg.getInt("settings.suicide_bomber.active_time");
        messages_suicide_bomber_active = cfg.getString("messages.suicide_bomber_active").replace("&", "§").replace("{bomb_time}", String.valueOf(settings_suicide_bomber_active_time));
        messages_suicide_bomber_fuse = cfg.getString("messages.suicide_bomber_fuse").replace("&", "§");
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (settings_suicide_bomber_enable) {
            if (ShopItemAddon.checkCooling(player, "suicide_bomber")) {
                return false;
            }
            return suicideBomber(player, item);
        }
        return false;
    }

    public static void handlePlayerDamage(Player player) {
        if (isCarryBomb(player)) {
            fuseBomb(player);
        }
    }

    public static boolean handleShopBuy(Player player, ICategoryContent content) {
        if (content.getIdentifier().contains("suicide_bomber")) {
            for (IContentTier tier : content.getContentTiers()) {
                for (IBuyItem buyItem : tier.getBuyItemsList()) {
                    ItemStack itemStack = buyItem.getItemStack().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(Language.getMsg(player, SuicideBomber.suicide_bomber_section + "-name"));
                    itemStack.setItemMeta(itemMeta);
                    buyItem.setItemStack(itemStack);
                }
            }
        }
        return false;
    }

    private static boolean suicideBomber(Player player, ItemStack item) {
        if (isSuicideBomber(player, item)) {
            ShopItemAddon.consumeItem(player, item, 1);
            player.sendMessage(messages_suicide_bomber_active);
            String[] _sound = settings_suicide_bomber_use_sound.split(":");
            player.playSound(player.getLocation(), Sound.valueOf(_sound[0]), Float.parseFloat(_sound[1]), Float.parseFloat(_sound[2]));
            player.getInventory().setHelmet(new ItemStack(Material.TNT));
            player.setMetadata("suicide_bomber", new FixedMetadataValue(_233BedWars.getInstance(), ""));
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                if (isCarryBomb(player)) {
                    fuseBomb(player);
                }
            }, settings_suicide_bomber_active_time * 20L);
            ShopItemAddon.setCooling(player, "suicide_bomber");
            return true;
        }

        return false;
    }

    private static boolean isSuicideBomber(Player player, ItemStack item) {
        return item.getType() == Material.getMaterial(suicide_bomber_material) && ShopItemAddon.compareAddonItem(player, item, suicide_bomber_section);
    }

    private static boolean isCarryBomb(Player player) {
        return player.hasMetadata("suicide_bomber");
    }

    private static void fuseBomb(Player player) {
        TNTPrimed tnt = player.getWorld().spawn(player.getLocation(), TNTPrimed.class);
        tnt.setFuseTicks(0);
        player.removeMetadata("suicide_bomber", _233BedWars.getInstance());
        player.sendMessage(messages_suicide_bomber_fuse);
    }
}
