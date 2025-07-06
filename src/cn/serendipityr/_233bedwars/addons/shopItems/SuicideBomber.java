package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class SuicideBomber {
    public static String suicide_bomber_material;
    public static String suicide_bomber_section;
    public static Boolean settings_suicide_bomber_enable = false;
    public static String settings_suicide_bomber_use_sound;
    public static Integer settings_suicide_bomber_active_time;
    public static String messages_suicide_bomber_active;
    public static String messages_suicide_bomber_fuse;

    public static void init(boolean enable, String material, String section) {
        suicide_bomber_material = material;
        suicide_bomber_section = section;
        settings_suicide_bomber_enable = enable;
    }

    public static void loadConfig(YamlConfiguration cfg) {
        settings_suicide_bomber_use_sound = cfg.getString("settings.suicide_bomber.use_sound");
        settings_suicide_bomber_active_time = cfg.getInt("settings.suicide_bomber.active_time");
        messages_suicide_bomber_active = cfg.getString("messages.suicide_bomber_active").replace("&", "§").replace("{bomb_time}", String.valueOf(settings_suicide_bomber_active_time));
        messages_suicide_bomber_fuse = cfg.getString("messages.suicide_bomber_fuse").replace("&", "§");
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isSuicideBomber(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "suicide_bomber")) {
                suicideBomber(player);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "suicide_bomber");
            }
            return true;
        }
        return false;
    }

    public static void handlePlayerDamage(Player player) {
        if (isCarryBomb(player)) {
            fuseBomb(player);
        }
    }

    private static void suicideBomber(Player player) {
        player.sendMessage(messages_suicide_bomber_active);
        String[] _sound = settings_suicide_bomber_use_sound.split(":");
        player.playSound(player.getLocation(), Sound.valueOf(_sound[0]), Float.parseFloat(_sound[1]), Float.parseFloat(_sound[2]));
        player.getInventory().setHelmet(new ItemStack(Material.TNT));
        player.setMetadata("suicide_bomber", new FixedMetadataValue(_233BedWars.getInstance(), ""));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isCarryBomb(player)) {
                    fuseBomb(player);
                }
            }
        }.runTaskLater(_233BedWars.getInstance(), settings_suicide_bomber_active_time * 20L);
    }

    private static boolean isSuicideBomber(Player player, ItemStack item) {
        return (item.getType().toString().equals(suicide_bomber_material) || item.getType().toString().equals(suicide_bomber_material.replace("LEGACY_", ""))) && ShopItemAddon.compareAddonItem(player, item, suicide_bomber_section);
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
