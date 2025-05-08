package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.arena.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MagicalWool {
    public static String magical_wool_material;
    public static String magical_wool_section;
    public static Boolean settings_magical_wool_enable = false;
    public static Integer settings_magical_wool_expand_length;
    public static Integer settings_magical_wool_expand_delay;
    public static String settings_magical_wool_place_sound;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_magical_wool_expand_length = cfg.getInt("settings.magical_wool.expand_length");
        settings_magical_wool_expand_delay = cfg.getInt("settings.magical_wool.expand_delay");
        settings_magical_wool_place_sound = cfg.getString("settings.magical_wool.place_sound");
    }

    public static void init(boolean enable, String material, String section) {
        magical_wool_material = material;
        magical_wool_section = section;
        settings_magical_wool_enable = enable;
    }

    public static boolean handleBlockPlace(Player player, Block block, Block against, ItemStack item) {
        if (isMagicalWool(player, item)) {
            if (ShopItemAddon.checkCooling(player, "magical_wool")) {
                return true;
            }
            ShopItemAddon.setCooling(player, "magical_wool");
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
            expand(player, block, against, arena);
        }
        return false;
    }

    private static boolean isMagicalWool(Player player, ItemStack item) {
        return (item.getType().toString().equals(magical_wool_material) || item.getType().toString().equals(magical_wool_material.replace("LEGACY_", ""))) && ShopItemAddon.compareAddonItem(player, item, magical_wool_section);
    }

    private static void expand(Player player, Block block, Block against, IArena arena) {
        BlockFace face = against.getFace(block);
        Location playerLoc = player.getLocation();
        TeamColor teamColor = arena.getTeam(player).getColor();
        Material wool = ProviderUtil.bw.getVersionSupport().woolMaterial();
        for (int i = 0; i < settings_magical_wool_expand_length; i++) {
            int dx = face.getModX() * i;
            int dy = face.getModY() * i;
            int dz = face.getModZ() * i;
            Location loc = new Location(player.getWorld(), block.getX() + dx, block.getY() + dy, block.getZ() + dz);
            Block replace = loc.getBlock();
            boolean isProtect = arena.isProtected(loc);
            if (i == 0 && !isProtect) {
                replace.setType(wool);
                ProviderUtil.bw.getVersionSupport().setBlockTeamColor(replace, teamColor);
                continue;
            }
            if (replace.getType() != Material.AIR || isProtect) {
                break;
            }
            // 防止玩家卡在方块中间
            if (loc.getBlockX() == playerLoc.getBlockX() &&
                    loc.getBlockZ() == playerLoc.getBlockZ() &&
                    (loc.getBlockY() == playerLoc.getBlockY() || loc.getBlockY() == playerLoc.getBlockY() + 1)) {
                break;
            }
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                replace.setType(wool);
                arena.addPlacedBlock(replace);
                String[] _sound = settings_magical_wool_place_sound.split(":");
                player.playSound(playerLoc, Sound.valueOf(_sound[0]), Float.parseFloat(_sound[1]), Float.parseFloat(_sound[2]));
                ProviderUtil.bw.getVersionSupport().setBlockTeamColor(replace, teamColor);
            }, (long) i * settings_magical_wool_expand_delay);
        }
    }
}
