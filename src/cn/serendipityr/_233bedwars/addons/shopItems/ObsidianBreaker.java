package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ObsidianBreaker {
    public static String obsidian_breaker_material;
    public static String obsidian_breaker_section;
    public static Boolean settings_obsidian_breaker_enable = false;
    public static Integer settings_obsidian_breaker_convert_time;
    public static String settings_obsidian_breaker_replace_block;
    public static Boolean settings_obsidian_breaker_complete_explosion;
    public static Integer settings_obsidian_breaker_explosion_damage;
    public static Boolean settings_obsidian_breaker_set_fire;
    public static Boolean settings_obsidian_breaker_break_block;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_obsidian_breaker_enable = cfg.getBoolean("settings.obsidian_breaker.enable");
        settings_obsidian_breaker_convert_time = cfg.getInt("settings.obsidian_breaker.convert_time");
        settings_obsidian_breaker_replace_block = cfg.getString("settings.obsidian_breaker.replace_block");
        settings_obsidian_breaker_complete_explosion = cfg.getBoolean("settings.obsidian_breaker.complete_explosion");
        settings_obsidian_breaker_explosion_damage = cfg.getInt("settings.obsidian_breaker.explosion_damage");
        settings_obsidian_breaker_set_fire = cfg.getBoolean("settings.obsidian_breaker.set_fire");
        settings_obsidian_breaker_break_block = cfg.getBoolean("settings.obsidian_breaker.break_block");
    }

    public static void init(boolean enable, String material, String section) {
        settings_obsidian_breaker_enable = enable;
        obsidian_breaker_material = material;
        obsidian_breaker_section = section;
    }

    public static boolean handleBlockItemInteract(Player player, ItemStack item, Block block) {
        if (isObsidianBreaker(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "obsidian_breaker")) {
                IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
                obsidianBreaker(arena, block);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "obsidian_breaker");
            }
            return true;
        }
        return false;
    }

    private static void obsidianBreaker(IArena arena, Block block) {
        if (block.getType().toString().contains("OBSIDIAN") && !arena.isProtected(block.getLocation())) {
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                if (settings_obsidian_breaker_complete_explosion) {
                    Location loc = block.getLocation();
                    block.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), settings_obsidian_breaker_explosion_damage, settings_obsidian_breaker_set_fire, settings_obsidian_breaker_break_block);
                }
                block.setType(Material.getMaterial(settings_obsidian_breaker_replace_block));
            }, settings_obsidian_breaker_convert_time * 20);
        }
    }

    private static boolean isObsidianBreaker(Player player, ItemStack item) {
        return item.getType().toString().equals(obsidian_breaker_material) && ShopItemAddon.compareAddonItem(player, item, obsidian_breaker_section);
    }
}
