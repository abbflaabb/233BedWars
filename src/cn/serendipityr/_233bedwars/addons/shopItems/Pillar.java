package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Pillar {
    public static String pillar_material;
    public static String pillar_section;
    public static Boolean settings_pillar_enable = false;
    public static Integer settings_pillar_build_height;
    public static String messages_pillar_build;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_pillar_build_height = cfg.getInt("settings.pillar.build_height");
        messages_pillar_build = cfg.getString("messages.pillar_build").replace("&", "§");
    }


    public static void init(boolean enable, String material, String section) {
        pillar_material = material;
        pillar_section = section;
        settings_pillar_enable = enable;
    }

    public static boolean handleBlockPlace(Player player, Block block, ItemStack item) {
        if (isPillar(player, item)) {
            if (ShopItemAddon.checkCooling(player, "pillar")) {
                return true;
            }
            ShopItemAddon.setCooling(player, "pillar");
            player.sendMessage(messages_pillar_build);
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
            pillar(block, arena);
        }
        return false;
    }

    private static void pillar(Block block, IArena arena) {
        for (int i = 0; i < settings_pillar_build_height - 1; i++) {
            Block b = block.getLocation().add(0, 1 + i, 0).getBlock();
            if (!b.getType().equals(Material.AIR) || arena.isProtected(b.getLocation())) {
                break;
            }
            b.setType(Material.getMaterial(pillar_material));
            arena.addPlacedBlock(b);
        }
    }

    private static boolean isPillar(Player player, ItemStack item) {
        return item.getType().toString().equals(pillar_material) && ShopItemAddon.compareAddonItem(player, item, pillar_section);
    }
}
