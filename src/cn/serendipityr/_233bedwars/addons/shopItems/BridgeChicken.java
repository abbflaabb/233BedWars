package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class BridgeChicken {
    public static String bridge_chicken_material;
    public static String bridge_chicken_section;
    public static String bridge_chicken_texture;
    public static Boolean settings_bridge_chicken_enable = false;
    public static Double settings_bridge_chicken_build_delay;
    public static Integer settings_bridge_chicken_max_blocks;
    public static Boolean settings_bridge_chicken_complete_explosion;
    public static Integer settings_bridge_chicken_explosion_damage;
    public static Boolean settings_bridge_chicken_set_fire;
    public static Boolean settings_bridge_chicken_break_block;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_bridge_chicken_build_delay = cfg.getDouble("settings.bridge_chicken.build_delay");
        settings_bridge_chicken_max_blocks = cfg.getInt("settings.bridge_chicken.max_blocks");
        settings_bridge_chicken_complete_explosion = cfg.getBoolean("settings.bridge_chicken.complete_explosion");
        settings_bridge_chicken_explosion_damage = cfg.getInt("settings.bridge_chicken.explosion_damage");
        settings_bridge_chicken_set_fire = cfg.getBoolean("settings.bridge_chicken.set_fire");
        settings_bridge_chicken_break_block = cfg.getBoolean("settings.bridge_chicken.break_block");
    }

    public static void init(boolean enable, String material, String section, String texture) {
        bridge_chicken_material = material;
        bridge_chicken_section = section;
        bridge_chicken_texture = texture;
        settings_bridge_chicken_enable = enable;
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isBridgeChicken(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "bridge_chicken")) {
                bridge(player);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "bridge_chicken");
            }
            return true;
        }
        return false;
    }

    public static boolean handleEntityDeath(Entity entity) {
        return entity.hasMetadata("bridge_chicken");
    }

    private static boolean isBridgeChicken(Player player, ItemStack item) {
        return (item.getType().toString().equals(bridge_chicken_material) || (item.getType().toString().equals("SKULL_ITEM") && bridge_chicken_material.equals("PLAYER_HEAD"))) && ShopItemAddon.compareAddonItem(player, item, bridge_chicken_section) && ShopItemAddon.compareSkullTexture(item, bridge_chicken_texture);
    }

    private static void bridge(Player player) {
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        Location loc = player.getLocation();
        Vector direction = loc.getDirection().normalize();
        Location start = loc.getBlock().getLocation().add(direction);
        double initY = start.getBlockY();

        Chicken chicken = start.getWorld().spawn(start.getBlock().getLocation().clone().add(0, 1, 0), Chicken.class);
        chicken.setMetadata("bridge_chicken", new FixedMetadataValue(_233BedWars.getInstance(), ""));

        new BukkitRunnable() {
            int blocksPlaced = 0;
            int yIncrement = 0;
            int step = 0;
            boolean reachedPeak = false;

            public void run() {
                step++;
                if (step == 2) {
                    step = 0;
                    if (reachedPeak) {
                        yIncrement--;
                    } else {
                        yIncrement++;
                    }
                }
                if (yIncrement > 7) {
                    reachedPeak = true;
                    step = 1;
                }

                Block block = start.getBlock();
                start.add(direction);
                start.setY(initY + yIncrement);
                if (block.getLocation().equals(start.getBlock().getLocation())) {
                    return;
                }

                if (blocksPlaced >= settings_bridge_chicken_max_blocks || arena.isProtected(start) || chicken.isDead()) {
                    chicken.setHealth(0);
                    completeExplosion(start);
                    this.cancel();
                    return;
                }

                blocksPlaced++;
                placeBridgeBlock(start, arena, player);
                chicken.teleport(start.clone().add(0, 1, 0));

                if (reachedPeak && start.getBlockY() == initY) {
                    chicken.setHealth(0);
                    completeExplosion(start);
                    this.cancel();
                }
            }
        }.runTaskTimer(_233BedWars.instance, 0L, (long) (settings_bridge_chicken_build_delay * 20));
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
        if (settings_bridge_chicken_complete_explosion) {
            location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), settings_bridge_chicken_explosion_damage, settings_bridge_chicken_set_fire, settings_bridge_chicken_break_block);
        }
    }
}

