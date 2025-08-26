package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class RescuePlatform {
    public static String rescue_platform_material;
    public static String rescue_platform_section;
    public static Boolean settings_rescue_platform_enable = false;
    public static Integer settings_rescue_platform_remove_delay;
    public static Integer settings_rescue_platform_y_offset;
    public static String settings_rescue_platform_platform_material;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_rescue_platform_remove_delay = cfg.getInt("settings.rescue_platform.remove_delay");
        settings_rescue_platform_y_offset = cfg.getInt("settings.rescue_platform.y_offset");
        settings_rescue_platform_platform_material = cfg.getString("settings.rescue_platform.platform_material");
    }

    public static void init(boolean enable, String material, String section) {
        rescue_platform_material = material;
        rescue_platform_section = section;
        settings_rescue_platform_enable = enable;
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isRescuePlatform(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "rescue_platform")) {
                IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
                generatePlatform(player, arena);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "rescue_platform");
            }
            return true;
        }
        return false;
    }

    private static boolean isRescuePlatform(Player player, ItemStack item) {
        return (item.getType().toString().equals(rescue_platform_material) || item.getType().toString().equals(rescue_platform_material.replace("LEGACY_", ""))) && ShopItemAddon.compareAddonItem(player, item, rescue_platform_section);
    }

    private static void generatePlatform(Player player, IArena arena) {
        Location center = player.getLocation().clone().add(0, settings_rescue_platform_y_offset, 0);
        Material platformMat = Material.getMaterial(settings_rescue_platform_platform_material);
        int[][] platform = new int[][]{
                {}, {-1, 2}, {}, {1, 2}, {},
                {-2, 1}, {-1, 1}, {0, 1}, {1, 1}, {2, 1},
                {}, {-1, 0}, {0, 0}, {1, 0}, {},
                {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {2, -1},
                {}, {-1, -2}, {}, {1, -2}, {}
        };
        List<Block> placed = new ArrayList<>();
        for (int[] locOffset : platform) {
            if (locOffset.length != 2) {
                continue;
            }
            Location blockLoc = center.clone().add(locOffset[0], 0, locOffset[1]);
            Block block = blockLoc.getBlock();
            if (arena.isProtected(blockLoc) || block.getState().getData().getItemType() != Material.AIR) {
                continue;
            }
            block.setType(platformMat);
            block.getState().update();
            placed.add(block);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : placed) {
                    block.setType(Material.AIR);
                    block.getState().update();
                }
                placed.clear();
            }
        }.runTaskLater(_233BedWars.getInstance(), settings_rescue_platform_remove_delay * 20L);
    }
}
