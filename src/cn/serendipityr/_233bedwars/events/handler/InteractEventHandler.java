package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.FastCommands;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InteractEventHandler implements Listener {
    public static List<ItemStack> preventDrops = new ArrayList<>();
    public static HashMap<ItemStack, String> executes = new HashMap<>();

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();

        }
    }

    @EventHandler
    public void onPlayerClickItem(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (handleClick(player, event.getCurrentItem())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItems(PlayerDropItemEvent event) {
        if (preventDrops.contains(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerToggleShift(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            if (ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player) != null && ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player).getStatus() == GameState.playing) {
                if (ConfigManager.addon_fastCommands) {
                    FastCommands.handleShiftToggle(player);
                }
            }
        }
    }

    public static boolean handleClick(Player player, ItemStack item) {
        if (executes.containsKey(item)) {
            String[] execute = parseExecute(executes.get(item));
            if (execute.length != 2) {
                return false;
            }
            if ("command".equals(execute[0])) {
                player.performCommand(execute[1]);
                return true;
            }
            if ("say".equals(execute[0])) {
                player.chat(execute[1]);
                return true;
            }
            if ("gui_open".equals(execute[0])) {
                if ("fastCommands".equals(execute[1])) {
                    if (ConfigManager.addon_fastCommands) {
                        FastCommands.openGUI(player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String[] parseExecute(String input) {
        // Initialize an array to hold the result
        String[] result = new String[2];

        // Find the index of the closing bracket
        int endOfCommandIndex = input.indexOf(']');
        if (endOfCommandIndex != -1) {
            // Extract the command inside the brackets
            result[0] = input.substring(1, endOfCommandIndex);
            // Check if there's more content after the command and extract it
            if (input.length() > endOfCommandIndex + 2) {
                result[1] = input.substring(endOfCommandIndex + 2);
            } else {
                // If there's nothing after the command, set the second part to an empty string
                result[1] = "";
            }
        } else {
            // If the input doesn't follow the expected format, set both parts to null or an appropriate error value
            result[0] = null;
            result[1] = null;
        }

        return result;
    }
}
