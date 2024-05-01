package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class RecoverBed {
    public static String recover_bed_material = "";
    public static String recover_bed_section = "";
    public static Boolean settings_recover_bed_enable;
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
    static List<String> messages_recover_bed_success_broadcast;


    public static void loadConfig(YamlConfiguration cfg) {
        settings_recover_bed_enable = cfg.getBoolean("settings.recover_bed.enable");
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
    }

    public static void handleBedDestroy(IArena arena, ITeam team) {
        if (settings_recover_bed_enable) {
            if (ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60)) {
                ShopItemAddon.sendTeamMessage(team, messages_recover_bed_invalid_msg);
            } else {
                ShopItemAddon.sendTeamMessage(team, messages_recover_bed_destroy_tips);
            }
        }
    }

    public static boolean handleShopBuy(Player player, IArena arena, ICategoryContent content) {
        if (content.getIdentifier().contains("recover-bed")) {
            ITeam team = arena.getTeam(player);
            if (!team.isBedDestroyed()) {
                player.sendMessage(messages_recover_bed_cant_buy_alive);
                return true;
            }
            if (!ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60)) {
                player.sendMessage(messages_recover_bed_cant_buy_invalid);
                return true;
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
        return item.getType() != Material.getMaterial(recover_bed_material) && ShopItemAddon.compareAddonItem(player, item, recover_bed_section);
    }

    static HashMap<Player, Integer> limit_use_map = new HashMap<>();

    public static void initPlayer(Player player) {
        limit_use_map.remove(player);
    }

    private static boolean recoverBed(Player player, ItemStack item, IArena arena, ITeam team) {
        if (isRecoverBed(player, item)) {
            if (team.isBedDestroyed()) {
                if (limit_use_map.containsKey(player)) {
                    if (limit_use_map.get(player) >= settings_recover_bed_use_count_limit) {
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
                            settings_recover_bed_title_stay);
                    for (String msg : messages_recover_bed_success_broadcast) {
                        ShopItemAddon.sendGlobalMessage(arena, msg
                                .replace("{player}", player.getDisplayName())
                                .replace("{tColor}", PlaceholderUtil.getTeamColor(team))
                                .replace("{tName}", PlaceholderUtil.getTeamName(team)));
                    }
                    String[] _sound = settings_recover_bed_recover_sound.split(":");
                    ShopItemAddon.playTeamSound(team, Sound.valueOf(_sound[0]), Float.parseFloat(_sound[1]), Float.parseFloat(_sound[2]));
                    team.setBedDestroyed(false);
                    if (!limit_use_map.containsKey(player)) {
                        limit_use_map.put(player, 1);
                    } else {
                        limit_use_map.put(player, limit_use_map.get(player) + 1);
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
}
