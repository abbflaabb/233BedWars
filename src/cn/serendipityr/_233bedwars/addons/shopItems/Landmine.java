package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Landmine {
    public static String landmine_material;
    public static String landmine_section;
    public static Boolean settings_landmine_enable = false;
    public static Integer settings_landmine_explosion_damage;
    public static Boolean settings_landmine_set_fire;
    public static Boolean settings_landmine_break_block;
    public static String light_landmine_material;
    public static String light_landmine_section;
    public static Boolean settings_light_landmine_enable = false;
    public static Integer settings_light_landmine_explosion_damage;
    public static Boolean settings_light_landmine_set_fire;
    public static Boolean settings_light_landmine_break_block;
    public static String messages_landmine_place;
    public static String messages_landmine_fuse;
    public static String messages_landmine_remove;

    public static void init(boolean enable, String material, String section) {
        landmine_material = material;
        landmine_section = section;
        settings_landmine_enable = enable;
    }

    public static void light_init(boolean enable, String material, String section) {
        light_landmine_material = material;
        light_landmine_section = section;
        settings_light_landmine_enable = enable;
    }

    public static void loadConfig(YamlConfiguration cfg) {
        settings_landmine_explosion_damage = cfg.getInt("settings.landmine.explosion_damage");
        settings_landmine_set_fire = cfg.getBoolean("settings.landmine.set_fire");
        settings_landmine_break_block = cfg.getBoolean("settings.landmine.break_block");
        settings_light_landmine_explosion_damage = cfg.getInt("settings.light_landmine.explosion_damage");
        settings_light_landmine_set_fire = cfg.getBoolean("settings.light_landmine.set_fire");
        settings_light_landmine_break_block = cfg.getBoolean("settings.light_landmine.break_block");
        messages_landmine_fuse = cfg.getString("messages.landmine_fuse").replace("&", "§");
        messages_landmine_place = cfg.getString("messages.landmine_place").replace("&", "§");
        messages_landmine_remove = cfg.getString("messages.landmine_remove").replace("&", "§");
    }

    public static HashMap<Block, Player> landmineMap = new HashMap<>();
    public static HashMap<Block, ITeam> landmineTeamMap = new HashMap<>();

    public static boolean handleBlockPlace(Player player, Block block) {
        boolean check = false;
        if (block.getType().toString().equals(landmine_material) || block.getType().toString().equals(landmine_material.replace("LEGACY_", ""))) {
            if (ShopItemAddon.checkCooling(player, "landmine")) {
                return true;
            }
            check = true;
            ShopItemAddon.setCooling(player, "landmine");
        }

        if (block.getType().toString().equals(light_landmine_material) || block.getType().toString().equals(light_landmine_material.replace("LEGACY_", ""))) {
            if (ShopItemAddon.checkCooling(player, "light_landmine")) {
                return true;
            }
            check = true;
            ShopItemAddon.setCooling(player, "light_landmine");
        }

        if (check) {
            player.sendMessage(messages_landmine_place);
            landmineMap.put(block, player);
            ITeam team = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getTeam(player);
            landmineTeamMap.put(block, team);
        }

        return false;
    }

    public static boolean handleBlockDestroy(Player player, Block block) {
        if (landmineMap.containsKey(block)) {
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
            if (arena != null && arena.isReSpawning(player)) {
                return true;
            }
            block.setType(Material.AIR);
            Player placer = landmineMap.get(block);
            placer.sendMessage(messages_landmine_remove);
            landmineMap.remove(block);
            landmineTeamMap.remove(block);
            return true;
        }

        return false;
    }

    public static void onBlockInteract(Player player, Block block) {
        if (landmineMap.containsKey(block)) {
            ITeam team = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getTeam(player);
            if (!team.equals(landmineTeamMap.get(block))) {
                Player placer = landmineMap.get(block);
                placer.sendMessage(messages_landmine_fuse);
                fuse(block);
            }
        }
    }

    public static void onBlockRedstone(Block block, int old_state, int new_state) {
        if (landmineMap.containsKey(block) && old_state == 0 && new_state != 0) {
            if (block.getType().toString().equals(landmine_material) || block.getType().toString().equals(landmine_material.replace("LEGACY_", ""))) {
                for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1)) {
                    if (entity.getType() == EntityType.PLAYER) {
                        Player player = (Player) entity;
                        Player placer = landmineMap.get(block);
                        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(placer);
                        if (arena.getTeam(placer) == arena.getTeam(player)) {
                            continue;
                        }
                        placer.sendMessage(messages_landmine_fuse);
                        fuse(block);
                        break;
                    }
                }
            }
            if (block.getType().toString().equals(light_landmine_material) || block.getType().toString().equals(light_landmine_material.replace("LEGACY_", ""))) {
                for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1)) {
                    if (entity.getType() == EntityType.DROPPED_ITEM) {
                        Player placer = landmineMap.get(block);
                        placer.sendMessage(messages_landmine_fuse);
                        fuse(block);
                        break;
                    }
                }
            }
        }
    }

    private static void fuse(Block block) {
        Location loc = block.getLocation();
        if (block.getType().toString().equals(light_landmine_material)) {
            loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), settings_light_landmine_explosion_damage, settings_light_landmine_set_fire, settings_light_landmine_break_block);
        } else {
            loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), settings_landmine_explosion_damage, settings_landmine_set_fire, settings_landmine_break_block);
        }
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> block.setType(Material.AIR), 2L);
        landmineMap.remove(block);
        landmineTeamMap.remove(block);
    }
}