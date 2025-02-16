package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ToxicBall {
    public static String toxic_ball_material;
    public static String toxic_ball_section;
    public static Boolean settings_toxic_ball_enable = false;
    public static String settings_toxic_ball_effect;
    public static Integer settings_toxic_ball_effect_level;
    public static Integer settings_toxic_ball_effect_duration;
    public static Boolean settings_toxic_ball_effect_particle;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_toxic_ball_effect = cfg.getString("settings.toxic_ball.effect");
        settings_toxic_ball_effect_level = cfg.getInt("settings.toxic_ball.effect_level");
        settings_toxic_ball_effect_duration = cfg.getInt("settings.toxic_ball.effect_duration");
        settings_toxic_ball_effect_particle = cfg.getBoolean("settings.toxic_ball.effect_particle");
    }

    public static void init(boolean enable, String material, String section) {
        settings_toxic_ball_enable = enable;
        toxic_ball_material = material;
        toxic_ball_section = section;
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isToxicBall(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "toxic_ball")) {
                toxicBall(player);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "toxic_ball");
            }
            return true;
        }
        return false;
    }

    private static boolean isToxicBall(Player player, ItemStack item) {
        return item.getType().toString().equals(toxic_ball_material) && ShopItemAddon.compareAddonItem(player, item, toxic_ball_section);
    }

    private static void toxicBall(Player player) {
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setMetadata("toxic_ball", new FixedMetadataValue(_233BedWars.getInstance(), ""));
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> snowball.setShooter(null), 2L);
    }

    public static void onProjectileHit(Projectile projectile, Player victim) {
        if (projectile instanceof Snowball && projectile.hasMetadata("toxic_ball")) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.getByName(settings_toxic_ball_effect), settings_toxic_ball_effect_duration * 20, settings_toxic_ball_effect_level, settings_toxic_ball_effect_particle), true);
        }
    }
}
