package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars.addons.FastCommands;
import cn.serendipityr._233bedwars.addons.GeneratorEditor;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.addons.XpResMode;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.BedWarsShopUtil;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InteractEventHandler implements Listener {
    public static List<ItemStack> preventDrops = new ArrayList<>();
    public static HashMap<ItemStack, String> executes = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerClickItem(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getClickedInventory();
            if (inventory == null) {
                return;
            }
            int slot = event.getSlot();
            if (handleClick(player, event.getCurrentItem())) {
                event.setCancelled(true);
            }
            if (BedWarsShopUtil.handleShopClick(player, inventory, slot)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        if (ShopItemAddon.handleBlockPlace(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDestroyBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (ShopItemAddon.handleBlockDestroy(player, block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockRedstoneEvent(BlockRedstoneEvent event) {
        if (ConfigManager.addon_shopItemAddon) {
            Block block = event.getBlock();
            int old_state = event.getOldCurrent();
            int new_state = event.getNewCurrent();
            ShopItemAddon.handleBlockRedstone(block, old_state, new_state);
        }
    }

    @EventHandler
    public void onPlayerInteractItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        if (event.getAction() == Action.PHYSICAL) {
            if (ConfigManager.addon_shopItemAddon && ShopItemAddon.handleBlockInteract(player, block)) {
                event.setCancelled(true);
            }
        }
        if (item == null) {
            return;
        }
        if (handleClick(player, item)) {
            event.setCancelled(true);
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (ConfigManager.addon_shopItemAddon && ShopItemAddon.handleItemInteract(player, item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerPickUpItems(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        if (ProviderUtil.bw.getArenaUtil().isPlaying(player)) {
            if (ConfigManager.addon_xpResMode && XpResMode.handlePickUp(player, item)) {
                event.setCancelled(true);
            }

            if (ConfigManager.addon_generatorEditor) {
                GeneratorEditor.handlePickUp(player, item);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItems(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();

        for (ItemStack itemStack : preventDrops) {
            if (isSimilar(itemStack, item.getItemStack())) {
                event.setCancelled(true);
            }
        }

        GeneratorEditor.markThrownItem(player, item);
    }

    @EventHandler
    public void onPlayerToggleShift(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            if (ProviderUtil.bw.getArenaUtil().isPlaying(player)) {
                if (ConfigManager.addon_fastCommands) {
                    FastCommands.handleShiftToggle(player);
                }
            }
        }
    }

    @EventHandler()
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        if (ProviderUtil.bw.getArenaUtil().isPlaying(player)) {
            BedWarsShopUtil.handleShopOpen(player, inventory);
        }
    }

    public static void addPreventDrop(ItemStack itemStack) {
        if (!InteractEventHandler.preventDrops.contains(itemStack)) {
            InteractEventHandler.preventDrops.add(itemStack);
        }
    }

    public static boolean handleClick(Player player, ItemStack item) {
        for (ItemStack _item : executes.keySet()) {
            if (isSimilar(_item, item)) {
                String[] execute = parseExecute(executes.get(_item));
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
                    if ("resModeChange".equals(execute[1])) {
                        if (ConfigManager.addon_xpResMode) {
                            XpResMode.openGUI(player);
                            return true;
                        }
                    }
                }
                if ("changeResMode".equals(execute[0])) {
                    XpResMode.setResMode(player, execute[1]);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private static boolean isSimilar(ItemStack origin, ItemStack check) {
        if (check == null) {
            return false;
        }
        if (!check.hasItemMeta()) {
            return origin.isSimilar(check);
        } else {
            ItemMeta im = check.getItemMeta();
            if (im.getDisplayName() == null) {
                return false;
            }
            return origin.hasItemMeta() && im.getDisplayName().equals(origin.getItemMeta().getDisplayName());
        }
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
