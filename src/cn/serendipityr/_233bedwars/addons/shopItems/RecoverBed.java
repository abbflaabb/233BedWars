package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;

public class RecoverBed {
    public static String recover_bed_material;
    public static String recover_bed_section;
    public static Boolean settings_recover_bed_enable = false;
    static String settings_recover_bed_recover_sound;
    static Integer settings_recover_bed_valid_minutes;
    static Integer settings_recover_bed_use_count_limit;
    static Integer settings_recover_bed_title_stay;
    static String messages_recover_bed_destroy_tips;
    static String messages_recover_bed_success_title;
    static String messages_recover_bed_success_subtitle;
    static String messages_recover_bed_success_msg;
    static String messages_recover_bed_invalid_msg;
    static String messages_recover_bed_failed_msg;
    static String messages_recover_bed_cant_buy_alive;
    static String messages_recover_bed_cant_buy_invalid;
    static String messages_recover_bed_limited;
    static List<String> messages_recover_bed_success_broadcast;


    public static void loadConfig(YamlConfiguration cfg) {
        settings_recover_bed_recover_sound = cfg.getString("settings.recover_bed.recover_sound");
        settings_recover_bed_valid_minutes = cfg.getInt("settings.recover_bed.valid_minutes");
        settings_recover_bed_use_count_limit = cfg.getInt("settings.recover_bed.use_count_limit");
        settings_recover_bed_title_stay = cfg.getInt("settings.recover_bed.title_stay");

        messages_recover_bed_destroy_tips = cfg.getString("messages.recover_bed_destroy_tips").replace("&", "§");
        messages_recover_bed_success_title = cfg.getString("messages.recover_bed_success_title").replace("&", "§");
        messages_recover_bed_success_subtitle = cfg.getString("messages.recover_bed_success_subtitle").replace("&", "§");
        messages_recover_bed_success_msg = cfg.getString("messages.recover_bed_success_msg").replace("&", "§");
        messages_recover_bed_success_broadcast = cfg.getStringList("messages.recover_bed_success_broadcast");
        messages_recover_bed_success_broadcast.replaceAll(s -> s.replace("&", "§"));
        messages_recover_bed_invalid_msg = cfg.getString("messages.recover_bed_invalid").replace("&", "§");
        messages_recover_bed_failed_msg = cfg.getString("messages.recover_bed_failed").replace("&", "§");
        messages_recover_bed_cant_buy_alive = cfg.getString("messages.recover_bed_cant_buy_alive").replace("&", "§");
        messages_recover_bed_cant_buy_invalid = cfg.getString("messages.recover_bed_cant_buy_invalid").replace("&", "§");
        messages_recover_bed_limited = cfg.getString("messages.recover_bed_limited").replace("&", "§");
    }

    public static boolean handleBlockPlace(Block block) {
        return block.getType().toString().contains("BED");
    }

    public static void handleBedDestroy(IArena arena, ITeam team) {
        if (settings_recover_bed_enable) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(_233BedWars.getInstance(), () -> {
                if (ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60) && (!limit_use_map.containsKey(team) || limit_use_map.get(team) < settings_recover_bed_use_count_limit)) {
                    ShopItemAddon.sendTeamMessage(team, messages_recover_bed_destroy_tips);
                } else {
                    ShopItemAddon.sendTeamMessage(team, messages_recover_bed_invalid_msg);
                }
            }, 1L);
        }
    }

    public static boolean handleShopBuy(Player player, IArena arena, ICategoryContent content) {
        if (content.getIdentifier().contains("recover_bed")) {
            ITeam team = arena.getTeam(player);
            if (!team.isBedDestroyed()) {
                player.sendMessage(messages_recover_bed_cant_buy_alive);
                return true;
            }
            if (!ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60)) {
                player.sendMessage(messages_recover_bed_cant_buy_invalid);
                return true;
            }

            for (IContentTier tier : content.getContentTiers()) {
                for (IBuyItem buyItem : tier.getBuyItemsList()) {
                    ItemStack itemStack = buyItem.getItemStack().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(Language.getMsg(player, RecoverBed.recover_bed_section + "-name"));
                    itemStack.setItemMeta(itemMeta);
                    buyItem.setItemStack(itemStack);
                }
            }
        }
        return false;
    }

    public static boolean handleItemInteract(Player player, ItemStack item, IArena arena, ITeam team) {
        if (settings_recover_bed_enable) {
            return recoverBed(player, item, arena, team);
        }
        return false;
    }

    private static boolean isRecoverBed(Player player, ItemStack item) {
        return item.getType() == Material.getMaterial(recover_bed_material) && ShopItemAddon.compareAddonItem(player, item, recover_bed_section);
    }

    static HashMap<ITeam, Integer> limit_use_map = new HashMap<>();

    static HashMap<Location, MaterialData> beds = new HashMap<>();

    public static void initArena(IArena arena) {
        for (ITeam team : arena.getTeams()) {
            Location bed_loc = team.getBed();
            beds.put(bed_loc, bed_loc.getBlock().getState().getData().clone());
            Location[] directions = {
                    bed_loc.clone().add(1, 0, 0),  // East
                    bed_loc.clone().add(-1, 0, 0), // West
                    bed_loc.clone().add(0, 0, 1),  // South
                    bed_loc.clone().add(0, 0, -1)  // North
            };
            for (Location loc : directions) {
                if (loc.getBlock().getType().toString().contains("BED")) {
                    beds.put(loc, loc.getBlock().getState().getData());
                    break;
                }
            }
        }
    }

    public static void resetArena(IArena arena) {
        for (ITeam team : arena.getTeams()) {
            limit_use_map.remove(team);
            Location bed_loc = team.getBed();
            Location[] directions = {
                    bed_loc.clone().add(1, 0, 0),  // East
                    bed_loc.clone().add(-1, 0, 0), // West
                    bed_loc.clone().add(0, 0, 1),  // South
                    bed_loc.clone().add(0, 0, -1)  // North
            };
            beds.remove(bed_loc);
            for (Location loc : directions) {
                beds.remove(loc);
            }
        }
    }

    private static boolean recoverBed(Player player, ItemStack item, IArena arena, ITeam team) {
        if (isRecoverBed(player, item)) {
            if (team.isBedDestroyed()) {
                if (limit_use_map.containsKey(team)) {
                    if (limit_use_map.get(team) >= settings_recover_bed_use_count_limit) {
                        player.sendMessage(messages_recover_bed_limited);
                        return false;
                    }
                }
                if (ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60)) {
                    ShopItemAddon.sendTeamMessage(team, messages_recover_bed_success_msg);
                    ShopItemAddon.sendTeamTitle(team,
                            messages_recover_bed_success_title
                                    .replace("{player}", player.getDisplayName()),
                            messages_recover_bed_success_subtitle
                                    .replace("{player}", player.getDisplayName()),
                            settings_recover_bed_title_stay * 20);
                    for (String msg : messages_recover_bed_success_broadcast) {
                        ShopItemAddon.sendGlobalMessage(arena, msg
                                .replace("{player}", player.getDisplayName())
                                .replace("{tColor}", PlaceholderUtil.getTeamColor(team))
                                .replace("{tName}", PlaceholderUtil.getTeamName(team)));
                    }
                    String[] _sound = settings_recover_bed_recover_sound.split(":");
                    ShopItemAddon.playTeamSound(team, Sound.valueOf(_sound[0]), Float.parseFloat(_sound[1]), Float.parseFloat(_sound[2]));
                    placeBed(team);
                    ShopItemAddon.consumeItem(player, item, 1);
                    team.setBedDestroyed(false);
                    if (!limit_use_map.containsKey(team)) {
                        limit_use_map.put(team, 1);
                    } else {
                        limit_use_map.put(team, limit_use_map.get(team) + 1);
                    }
                    return true;
                } else {
                    player.sendMessage(messages_recover_bed_failed_msg);
                    return false;
                }
            }
        }
        return false;
    }

    private static void placeBed(ITeam team) {
        Location bed_loc = team.getBed();
        Location[] directions = {
                bed_loc.clone().add(1, 0, 0),  // East
                bed_loc.clone().add(-1, 0, 0), // West
                bed_loc.clone().add(0, 0, 1),  // South
                bed_loc.clone().add(0, 0, -1)  // North
        };
        Block bed_1 = bed_loc.getBlock();
        bed_1.setType(Material.BED_BLOCK);
        BlockState bed_state_1 = bed_1.getState();
        bed_state_1.setData(beds.get(bed_loc));
        bed_state_1.update();
        for (Location loc : directions) {
            if (beds.containsKey(loc)) {
                Block bed_2 = loc.getBlock();
                bed_2.setType(Material.BED_BLOCK);
                BlockState bed_state_2 = bed_2.getState();
                bed_state_2.setData(beds.get(loc));
                bed_state_2.update();
                break;
            }
        }
    }
}
