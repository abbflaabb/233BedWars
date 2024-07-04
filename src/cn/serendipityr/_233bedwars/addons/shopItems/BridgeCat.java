package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BridgeCat {
    public static String bridge_cat_material;
    public static String bridge_cat_section;
    public static String bridge_cat_texture;
    public static Boolean settings_bridge_cat_enable = false;
    public static Double settings_bridge_cat_build_delay;
    public static Integer settings_bridge_cat_max_blocks;
    public static Boolean settings_bridge_cat_complete_explosion;
    public static Integer settings_bridge_cat_explosion_damage;
    public static Boolean settings_bridge_cat_set_fire;
    public static Boolean settings_bridge_cat_break_block;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_bridge_cat_build_delay = cfg.getDouble("settings.bridge_cat.build_delay");
        settings_bridge_cat_max_blocks = cfg.getInt("settings.bridge_cat.max_blocks");
        settings_bridge_cat_complete_explosion = cfg.getBoolean("settings.bridge_cat.complete_explosion");
        settings_bridge_cat_explosion_damage = cfg.getInt("settings.bridge_cat.explosion_damage");
        settings_bridge_cat_set_fire = cfg.getBoolean("settings.bridge_cat.set_fire");
        settings_bridge_cat_break_block = cfg.getBoolean("settings.bridge_cat.break_block");
    }

    public static void init(boolean enable, String material, String section, String texture) {
        bridge_cat_material = material;
        bridge_cat_section = section;
        bridge_cat_texture = texture;
        settings_bridge_cat_enable = enable;
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isBridgeCat(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "bridge_cat")) {
                bridge(player);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "bridge_cat");
            }
            return true;
        }
        return false;
    }

    public static boolean handleEntityDeath(Entity entity) {
        return entity.hasMetadata("bridge_cat");
    }

    private static boolean isBridgeCat(Player player, ItemStack item) {
        return item.getType().toString().equals(bridge_cat_material) && ShopItemAddon.compareAddonItem(player, item, bridge_cat_section) && bridge_cat_texture.equals(ShopItemAddon.getSkullTextureFromItemStack(item));
    }

    private static void bridge(Player player) {
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        Location loc = player.getLocation();
        Vector direction = loc.getDirection().normalize();
        Location start = loc.getBlock().getLocation().add(direction);
        double initY = start.getBlockY();

        Ocelot cat = start.getWorld().spawn(start.getBlock().getLocation().clone().add(0, 1, 0), Ocelot.class);
        cat.setMetadata("bridge_cat", new FixedMetadataValue(_233BedWars.getInstance(), ""));

        new BukkitRunnable() {
            int blocksPlaced = 0;

            public void run() {
                Block block = start.getBlock();
                if (block == start.add(direction).getBlock()) {
                    return;
                }
                start.setY(initY);

                if (blocksPlaced >= settings_bridge_cat_max_blocks || start.getBlock().getType() != Material.AIR || arena.isProtected(start) || cat.isDead()) {
                    cat.setHealth(0);
                    completeExplosion(start);
                    this.cancel();
                    return;
                }

                placeBridgeBlock(start, arena, player);
                cat.teleport(start.clone().add(0, 1, 0));
                blocksPlaced++;
            }
        }.runTaskTimer(_233BedWars.instance, 0L, (long) (settings_bridge_cat_build_delay * 20));
    }


    private static void placeBridgeBlock(Location location, IArena arena, Player player) {
        Block b = location.getBlock();
        if (b.getType() != Material.AIR) {
            return;
        }
        b.setType(ProviderUtil.bw.getVersionSupport().woolMaterial());
        ProviderUtil.bw.getVersionSupport().setBlockTeamColor(b, arena.getTeam(player).getColor());
        arena.addPlacedBlock(b);
    }

    private static void completeExplosion(Location location) {
        if (settings_bridge_cat_complete_explosion) {
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), settings_bridge_cat_explosion_damage, settings_bridge_cat_set_fire, settings_bridge_cat_break_block);
        }
    }
}
