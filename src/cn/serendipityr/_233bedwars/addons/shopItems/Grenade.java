package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class Grenade {
    public static String grenade_material;
    public static String grenade_section;
    public static Boolean settings_grenade_enable = false;
    public static Integer settings_grenade_explosion_damage;
    public static Integer settings_grenade_fuse_delay;
    public static Boolean settings_grenade_set_fire;
    public static Boolean settings_grenade_break_block;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_grenade_explosion_damage = cfg.getInt("settings.grenade.explosion_damage");
        settings_grenade_fuse_delay = cfg.getInt("settings.grenade.fuse_delay");
        settings_grenade_set_fire = cfg.getBoolean("settings.grenade.set_fire");
        settings_grenade_break_block = cfg.getBoolean("settings.grenade.break_block");
    }

    public static void init(boolean enable, String material, String section) {
        grenade_material = material;
        grenade_section = section;
        settings_grenade_enable = enable;
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isGrenade(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "grenade")) {
                grenade(player);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "grenade");
            }
            return true;
        }
        return false;
    }

    private static boolean isGrenade(Player player, ItemStack item) {
        return item.getType().toString().equals(grenade_material) && ShopItemAddon.compareAddonItem(player, item, grenade_section);
    }

    private static void grenade(Player player) {
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setMetadata("grenade", new FixedMetadataValue(_233BedWars.getInstance(), ""));
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> snowball.setShooter(null), 2L);
    }

    public static void onProjectileHit(Projectile projectile) {
        if (projectile instanceof Snowball && projectile.hasMetadata("grenade")) {
            Location loc = projectile.getLocation();
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), settings_grenade_explosion_damage, settings_grenade_set_fire, settings_grenade_break_block), settings_grenade_fuse_delay * 20L);
        }
    }
}
