package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.globalEvents.*;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.events.handler.InteractEventHandler;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.upgrades.MenuContent;
import com.andrei1058.bedwars.api.upgrades.TeamUpgrade;
import com.andrei1058.bedwars.api.upgrades.UpgradeAction;
import com.andrei1058.bedwars.upgrades.UpgradesManager;
import com.andrei1058.bedwars.upgrades.menu.MenuUpgrade;
import com.andrei1058.bedwars.upgrades.menu.UpgradeTier;
import com.andrei1058.bedwars.upgrades.upgradeaction.GeneratorEditAction;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GlobalEvents {
    static Boolean vote_events_enable;
    static HashMap<String, String> force_groups = new HashMap<>();
    static String default_event;
    static String vote_msg;
    static String vote_result;
    static String vote_result_none;
    static List<String> event_broadcast = new ArrayList<>();
    static HashMap<String, String[]> event_info = new HashMap<>();
    static HashMap<Integer, ItemStack> items = new HashMap<>();
    static HashMap<Integer, ItemStack> gui_items = new HashMap<>();
    static Integer gui_size;
    static String gui_title;

    public static List<String> enable_events = new ArrayList<>();
    public static ConcurrentHashMap<IArena, ConcurrentHashMap<Player, String>> vote_map = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<IArena, String> apply_events = new ConcurrentHashMap<>();


    public static void loadConfig(YamlConfiguration cfg) {
        vote_events_enable = cfg.getBoolean("settings.vote_events.enable");
        force_groups.clear();
        for (String groups : cfg.getStringList("settings.vote_events.force_groups")) {
            String[] _groups = groups.split(":");
            force_groups.put(_groups[0], _groups[1]);
        }
        default_event = cfg.getString("settings.default_event");
        vote_msg = cfg.getString("messages.vote_msg").replace("&", "§");
        vote_result = cfg.getString("messages.vote_result").replace("&", "§");
        vote_result_none = cfg.getString("messages.vote_result_none").replace("&", "§");
        event_broadcast = cfg.getStringList("messages.event_broadcast");
        event_broadcast.replaceAll(s -> s.replace("&", "§"));
        event_info.clear();
        for (String info : cfg.getStringList("messages.event_info")) {
            String[] _info = info.replace("&", "§").split(":");
            event_info.put(_info[0], Arrays.copyOfRange(_info, 1, 3));
        }
        items.clear();
        gui_size = cfg.getInt("GUI.size");
        gui_title = cfg.getString("GUI.title").replace("&", "§");
        gui_items.clear();
        for (String item : cfg.getConfigurationSection("Item").getKeys(false)) {
            ItemStack _item = ConfigManager.parseItem(cfg.getConfigurationSection("Item." + item));
            InteractEventHandler.addPreventDrop(_item);
            items.put(Integer.parseInt(item), _item);
        }
        for (String item : cfg.getConfigurationSection("GUI.items").getKeys(false)) {
            gui_items.put(Integer.parseInt(item), ConfigManager.parseItem(cfg.getConfigurationSection("GUI.items." + item)));
        }

        enable_events.clear();
        RandomEvent.loadConfig(cfg);
        Potions.loadConfig(cfg);
        DoomsdayStrike.loadConfig(cfg);
        InfiniteFirepower.loadConfig(cfg);
        InadequateFirepower.loadConfig(cfg);
        ForgeLeveling.loadConfig(cfg);
    }

    public static void initPlayer(Player player, IArena arena) {
        setPlayerVote(player, arena, default_event, false);
        giveItems(player, arena);
    }

    public static void giveItems(Player player, IArena arena) {
        if (force_groups.containsKey(arena.getGroup())) {
            return;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(_233BedWars.getInstance(), () -> {
            for (Integer slot : items.keySet()) {
                player.getInventory().setItem(slot, items.get(slot));
            }
        }, 16L);
    }

    public static void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(player, gui_size, gui_title);
        IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
        if (arena == null) {
            return;
        }
        Map<String, Long> votes_count = getVoteCountsMap(arena);
        for (Integer slot : gui_items.keySet()) {
            ItemStack item = gui_items.get(slot).clone();
            ItemMeta im = item.getItemMeta();
            if (im != null) {
                List<String> lores = new ArrayList<>();
                for (String lore : im.getLore()) {
                    String _lore = lore;
                    for (String placeHolder : getAllPlaceHolder(lore)) {
                        String[] _p = placeHolder.split("#");
                        if (_p[0].equals("votes")) {
                            _lore = lore
                                    .replace("{" + placeHolder + "}", String.valueOf(votes_count.getOrDefault(_p[1], 0L)));
                        }
                    }
                    lores.add(_lore);
                }
                im.setLore(lores);
            }
            item.setItemMeta(im);
            gui.setItem(slot, item);
        }

        player.openInventory(gui);
    }

    public static void setPlayerVote(Player player, IArena arena, String event, boolean send_msg) {
        if (arena == null) {
            return;
        }
        if (!enable_events.contains(event) && !"none".equals(event)) {
            event = "random";
        }
        ConcurrentHashMap<Player, String> vote = getVoteMap(arena);
        vote.put(player, event);
        vote_map.put(arena, vote);
        if (send_msg) {
            String[] eventInfo = getEventInfo(event);
            player.sendMessage(vote_msg
                    .replace("{event_name}", eventInfo[0])
                    .replace("{event_description}", eventInfo[1])
            );
            player.closeInventory();
        }
    }

    public static void applyEvent(IArena arena) {
        ConcurrentHashMap<Player, String> vote = getVoteMap(arena);
        Map.Entry<String, Long> filter = vote.values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        if (filter != null) {
            if (filter.getKey().equals("none")) {
                ShopItemAddon.sendGlobalMessage(arena, vote_result_none);
                vote_map.remove(arena);
                apply_events.put(arena, "none");
                return;
            }
            ShopItemAddon.sendGlobalMessage(arena, vote_result
                    .replace("{final_event}", getEventInfo(filter.getKey())[0])
                    .replace("{final_votes}", String.valueOf(filter.getValue()))
            );
            apply_events.put(arena, filter.getKey().equals("random") ? RandomEvent.getRandomEvent() : filter.getKey());
            vote_map.remove(arena);
            if (isEnableEvent(arena)) {
                Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> sendEventApplyMsg(arena), 20L);
                initEvent(arena);
            }
        } else {
            ShopItemAddon.sendGlobalMessage(arena, vote_result_none);
            vote_map.remove(arena);
            apply_events.put(arena, "none");
        }
    }

    public static void resetArena(IArena arena) {
        if (getApplyEvent(arena).equals("infinite_firepower")) {
            InfiniteFirepower.resetArena(arena);
        }
        if (getApplyEvent(arena).equals("inadequate_firepower")) {
            InadequateFirepower.resetArena(arena);
        }
        apply_events.remove(arena);
    }

    public static void resetPlayer(IArena arena, Player player) {
        if (vote_map.containsKey(arena)) {
            vote_map.get(arena).remove(player);
        }
    }

    public static boolean isEnableEvent(IArena arena) {
        return !getApplyEvent(arena).equals("none");
    }

    public static String getApplyEvent(IArena arena) {
        return apply_events.getOrDefault(arena, "none");
    }

    public static void handlePlayerDeath(IArena arena, Player victim) {
        if (getApplyEvent(arena).equals("potions")) {
            Potions.handlePlayerDeath(arena, victim);
        }
    }

    public static void handlePlayerRespawn(IArena arena, Player player) {
        if (getApplyEvent(arena).equals("potions")) {
            Potions.handlePlayerRespawn(player);
        }
    }

    public static void handleEntityExplode(Entity entity, List<Block> blocks) {
        DoomsdayStrike.handleEntityExplode(entity, blocks);
    }

    public static boolean handleEntityDamageByEntity(Entity damager, Entity victim) {
        if (DoomsdayStrike.handleEntityDamageByEntity(damager, victim)) {
            return true;
        }
        return false;
    }

    public static void handleGeneratorUpgrade(IArena arena, IGenerator generator) {
        if (getApplyEvent(arena).equals("infinite_firepower")) {
            InfiniteFirepower.handleGeneratorUpdate(generator);
        }
        if (getApplyEvent(arena).equals("inadequate_firepower")) {
            InadequateFirepower.handleGeneratorUpdate(generator);
        }
    }

    public static void handleTeamUpgradeBuy(IArena arena, ITeam team, TeamUpgrade upgrade) {
        if (getApplyEvent(arena).equals("infinite_firepower")) {
            InfiniteFirepower.handleTeamUpgradeBuy(arena, team, upgrade);
        }
        if (getApplyEvent(arena).equals("inadequate_firepower")) {
            InadequateFirepower.handleTeamUpgradeBuy(arena, team, upgrade);
        }
    }

    public static String[] getEventInfo(String event) {
        return event_info.get(event);
    }

    public static MenuUpgrade getForgeUpgrade(IArena arena) {
        for (MenuContent mc : UpgradesManager.getMenuForArena(arena).getMenuContentBySlot().values()) {
            if (mc instanceof MenuUpgrade) {
                MenuUpgrade mu = (MenuUpgrade) mc;
                for (UpgradeTier ut : mu.getTiers()) {
                    for (UpgradeAction ua : ut.getUpgradeActions()) {
                        if (ua instanceof GeneratorEditAction) {
                            return mu;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void initEvent(IArena arena) {
        switch (getApplyEvent(arena)) {
            case "potions":
                Potions.initEvent(arena);
                break;
            case "doomsday_strike":
                DoomsdayStrike.initEvent(arena);
                break;
            case "infinite_firepower":
                InfiniteFirepower.initEvent(arena);
                break;
            case "inadequate_firepower":
                InadequateFirepower.initEvent(arena);
                break;
            case "forge_leveling":
                ForgeLeveling.initEvent(arena);
                break;
        }
    }

    private static ConcurrentHashMap<Player, String> getVoteMap(IArena arena) {
        return vote_map.containsKey(arena) ? vote_map.get(arena) : new ConcurrentHashMap<>();
    }

    private static void sendEventApplyMsg(IArena arena) {
        for (String s : event_broadcast) {
            String[] event_info = getEventInfo(getApplyEvent(arena));
            ShopItemAddon.sendGlobalMessage(arena, s
                    .replace("{event_name}", event_info[0])
                    .replace("{event_description}", event_info[1])
            );
        }
    }

    private static List<String> getAllPlaceHolder(String str) {
        Pattern pattern = Pattern.compile("\\{([^}]+)}");
        Matcher matcher = pattern.matcher(str);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group(1)); // 提取 {} 内部的内容
        }
        return matches;
    }

    private static Map<String, Long> getVoteCountsMap(IArena arena) {
        return vote_map.getOrDefault(arena, new ConcurrentHashMap<>()).values().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}
