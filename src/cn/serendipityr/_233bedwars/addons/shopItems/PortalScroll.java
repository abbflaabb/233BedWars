package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

import java.util.Collections;

public class PortalScroll {
    public static String portal_scroll_material;
    public static String portal_scroll_section;
    public static Boolean settings_portal_scroll_enable = false;
    public static Integer settings_portal_scroll_portal_time;
    public static Integer settings_portal_scroll_title_stay;
    public static Integer settings_portal_scroll_progress_length;
    public static String settings_portal_scroll_progress_unit;
    public static String settings_portal_scroll_progress_color_current;
    public static String settings_portal_scroll_progress_color_left;
    public static String messages_portal_scroll_tips;
    public static String messages_portal_scroll_broadcast;
    public static String messages_portal_scroll_use_title;
    public static String messages_portal_scroll_use_subtitle;
    public static String messages_portal_scroll_success;
    public static String messages_portal_scroll_failed;

    public static void loadConfig(YamlConfiguration cfg) {
        settings_portal_scroll_portal_time = cfg.getInt("settings.portal_scroll.portal_time");
        settings_portal_scroll_title_stay = cfg.getInt("settings.portal_scroll.title_stay");
        settings_portal_scroll_progress_length = cfg.getInt("settings.portal_scroll.progress_length");
        settings_portal_scroll_progress_unit = cfg.getString("settings.portal_scroll.progress_unit");
        settings_portal_scroll_progress_color_current = cfg.getString("settings.portal_scroll.progress_color_current").replace("&", "§");
        settings_portal_scroll_progress_color_left = cfg.getString("settings.portal_scroll.progress_color_left").replace("&", "§");
        messages_portal_scroll_tips = cfg.getString("messages.portal_scroll_tips").replace("&", "§");
        messages_portal_scroll_broadcast = cfg.getString("messages.portal_scroll_broadcast").replace("&", "§");
        messages_portal_scroll_use_title = cfg.getString("messages.portal_scroll_use_title").replace("&", "§");
        messages_portal_scroll_use_subtitle = cfg.getString("messages.portal_scroll_use_subtitle").replace("&", "§");
        messages_portal_scroll_success = cfg.getString("messages.portal_scroll_success").replace("&", "§");
        messages_portal_scroll_failed = cfg.getString("messages.portal_scroll_failed").replace("&", "§");
    }

    public static void init(boolean enable, String material, String section) {
        settings_portal_scroll_enable = enable;
        portal_scroll_material = material;
        portal_scroll_section = section;
    }

    public static boolean handleItemInteract(Player player, ItemStack item) {
        if (isPortalScroll(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "portal_scroll")) {
                portalScroll(player);
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "portal_scroll");
            }
            return true;
        }
        return false;
    }

    public static boolean handlePlayerMovement(Player player, Location from, Location to) {
        if (player.hasMetadata("portal_scroll")) {
            if (from.distance(to) >= 0.08) {
                player.removeMetadata("portal_scroll", _233BedWars.getInstance());
            }
        }
        return false;
    }

    private static void portalScroll(Player player) {
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        ITeam team = arena.getTeam(player);
        String broadcast = PlaceholderUtil.formatText(player, arena, team, messages_portal_scroll_broadcast).replace("{player}", player.getDisplayName());
        ShopItemAddon.sendGlobalMessage(arena, broadcast);
        player.setMetadata("portal_scroll", new FixedMetadataValue(_233BedWars.getInstance(), ""));
        new BukkitRunnable() {
            int delay = settings_portal_scroll_portal_time;

            @Override
            public void run() {
                delay--;
                if (!player.hasMetadata("portal_scroll")) {
                    player.sendMessage(messages_portal_scroll_failed);
                    this.cancel();
                    return;
                }
                if (delay <= 0) {
                    player.sendMessage(messages_portal_scroll_success);
                    player.teleport(team.getSpawn());
                    player.removeMetadata("portal_scroll", _233BedWars.getInstance());
                    this.cancel();
                    return;
                }
                player.sendMessage(messages_portal_scroll_tips.replace("{left_time}", String.valueOf(delay)));
                int current = Math.round((float) delay / settings_portal_scroll_portal_time * settings_portal_scroll_progress_length);
                int left = settings_portal_scroll_progress_length - current;
                String progress = settings_portal_scroll_progress_color_current + String.join("", Collections.nCopies(left, settings_portal_scroll_progress_unit)) + settings_portal_scroll_progress_color_left + String.join("", Collections.nCopies(current, settings_portal_scroll_progress_unit));
                player.sendTitle(new Title(messages_portal_scroll_use_title, messages_portal_scroll_use_subtitle.replace("{portal_progress}", PlaceholderUtil.formatTextUnicode(progress)), 0, settings_portal_scroll_title_stay, 0));
            }
        }.runTaskTimer(_233BedWars.getInstance(), 0, 20);
    }

    private static boolean isPortalScroll(Player player, ItemStack item) {
        return item.getType().toString().equals(portal_scroll_material) && ShopItemAddon.compareAddonItem(player, item, portal_scroll_section);
    }
}
