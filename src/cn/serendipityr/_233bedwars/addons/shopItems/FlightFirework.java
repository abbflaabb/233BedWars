package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.TitleUtil;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class FlightFirework {
    public static String flight_firework_material;
    public static String flight_firework_section;
    public static Boolean settings_flight_firework_enable = false;
    public static Integer settings_flight_firework_fuse_delay;
    public static Integer settings_flight_firework_explosion_damage;
    public static Boolean settings_flight_firework_set_fire;
    public static Boolean settings_flight_firework_break_block;
    public static String messages_flight_firework_use;
    public static String messages_flight_firework_title;

    public static void init(boolean enable, String material, String section) {
        flight_firework_material = material;
        flight_firework_section = section;
        settings_flight_firework_enable = enable;
    }

    public static void loadConfig(YamlConfiguration cfg) {
        settings_flight_firework_fuse_delay = cfg.getInt("settings.flight_firework.fuse_delay");
        settings_flight_firework_explosion_damage = cfg.getInt("settings.flight_firework.explosion_damage");
        settings_flight_firework_set_fire = cfg.getBoolean("settings.flight_firework.set_fire");
        settings_flight_firework_break_block = cfg.getBoolean("settings.flight_firework.break_block");
        messages_flight_firework_use = cfg.getString("messages.flight_firework_use").replace("&", "§");
        messages_flight_firework_title = cfg.getString("messages.flight_firework_title").replace("&", "§");
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isFlightFirework(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "flight_firework")) {
                flightFirework(player);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "flight_firework");
            }
            return true;
        }
        return false;
    }

    public static void onPlayerDamage(Player player) {
        if (player.isInsideVehicle() && player.getVehicle().getType().equals(EntityType.FIREWORK)) {
            Firework firework = (Firework) player.getVehicle();
            if (firework.hasMetadata("flight_firework")) {
                firework.detonate();
            }
        }
    }

    public static boolean handleFireworkExplode(Firework firework) {
        if (firework.hasMetadata("flight_firework")) {
            firework.removeMetadata("flight_firework", _233BedWars.getInstance());
            Location loc = firework.getLocation();
            loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), settings_flight_firework_explosion_damage, settings_flight_firework_set_fire, settings_flight_firework_break_block);
        }

        return false;
    }

    private static void flightFirework(Player player) {
        player.sendMessage(messages_flight_firework_use);
        TitleUtil.send(player, messages_flight_firework_title, "", 0, settings_flight_firework_fuse_delay * 20, 0);
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.setPower(settings_flight_firework_fuse_delay);
        fireworkMeta.addEffect(FireworkEffect.builder()
                .withColor(Color.RED)
                .withFade(Color.ORANGE)
                .with(FireworkEffect.Type.BALL)
                .trail(true)
                .flicker(true)
                .build());
        firework.setFireworkMeta(fireworkMeta);
        firework.setPassenger(player);
        firework.setMetadata("flight_firework", new FixedMetadataValue(_233BedWars.getInstance(), ""));
    }

    private static boolean isFlightFirework(Player player, ItemStack item) {
        return (item.getType().toString().equals(flight_firework_material) || item.getType().toString().equals(flight_firework_material.replace("LEGACY_", ""))) && ShopItemAddon.compareAddonItem(player, item, flight_firework_section);
    }
}