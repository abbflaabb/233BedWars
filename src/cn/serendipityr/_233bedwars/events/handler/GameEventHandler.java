package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.*;
import cn.serendipityr._233bedwars.config.ConfigManager;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GameEventHandler implements Listener {
    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        GameState state = event.getNewState();
        IArena arena = event.getArena();
        if (state.equals(GameState.playing)) {
            if (ConfigManager.addon_teamNameThemes) {
                TeamNameThemes.initGame(arena);
                Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                    for (Player player : arena.getPlayers()) {
                        FastCommands.giveItems(player);
                    }
                }, 20L);
            }
            if (ConfigManager.addon_dalaoWarning) {
                DalaoWarning.initGame(arena);
            }
            GeneratorEditor.initGame(arena);
        }
        if (state.equals(GameState.restarting)) {
            GeneratorEditor.resetArena(arena);
        }
        if (ConfigManager.addon_scoreBoardEditor) {
            for (Player player : arena.getPlayers()) {
                ScoreboardEditor.editScoreBoard(arena, player);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerReSpawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> FastCommands.giveItems(player), 20L);
    }

    @EventHandler
    public void onPlayerJoinGame(PlayerJoinArenaEvent event) {
        IArena arena = event.getArena();
        Player player = event.getPlayer();
        if (ConfigManager.addon_scoreBoardEditor) {
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> ScoreboardEditor.editScoreBoard(arena, player), 5L);
        }
    }

    @EventHandler
    public void onPlayerKillEvent(PlayerKillEvent event) {
        IArena arena = event.getArena();
        Player killer = event.getKiller();
        Player victim = event.getVictim();
        if (killer != null) {
            if (killer != victim) {
                CombatDetails.checkPlayerKillDistance(killer, victim);
                CombatDetails.calcKillStreak(arena, killer, victim);
            }
        }
    }

    /*@EventHandler
    public void onPlayerDeath(PlayerKillEvent event) {
        Player player = event.getVictim();
        ConcurrentHashMap<Player, Integer> respawnSession = event.getArena().getRespawnSessions();
        respawnSession.put(player, 10);
        LogUtil.consoleLog("&cTest....................");
    }

    @EventHandler
    public void onPlayerOpenShop(ShopOpenEvent event) {
        players.add(event.getPlayer());
    }

    List<Player> players = new ArrayList<>();

    @EventHandler
    public void onPlayerOpenGUI(InventoryOpenEvent event) {
        Player p = (Player) event.getPlayer();
        if (players.contains(p)) {
            LogUtil.consoleLog("&a玩家打开商店！");
            ItemStack itemStack = event.getInventory().getItem(19);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(itemMeta.getDisplayName().replace("§c", "§a"));
            for (int i = 0; i < itemMeta.getLore().size(); i++) {
                String lore = itemMeta.getLore().get(i);
                if (isTextDerivedFromTemplate(lore, ProviderUtil.bw.getPlayerLanguage(p).m("shop-lore-status-cant-afford"))) {
                    List<String> lores = itemMeta.getLore();
                    lores.set(i, ProviderUtil.bw.getPlayerLanguage(p).m("shop-lore-status-can-buy"));
                    itemMeta.setLore(lores);
                }

                if (isTextDerivedFromTemplate(lore, ProviderUtil.bw.getPlayerLanguage(p).m("shop-lore-status-cant-afford"))) {
                    List<String> lores = itemMeta.getLore();
                    lores.set(i, ProviderUtil.bw.getPlayerLanguage(p).m("shop-lore-status-can-buy"));
                    itemMeta.setLore(lores);
                }
            }

            itemStack.setItemMeta(itemMeta);
            event.getInventory().setItem(19, itemStack);
        }
    }

    @EventHandler
    public void onPlayerCloseGUI(InventoryCloseEvent event) {
        Player p = (Player) event.getPlayer();
        if (players.contains(p)) {
            LogUtil.consoleLog("&a玩家关闭商店！");
            players.remove(p);
        }
    }

    public static boolean isTextDerivedFromTemplate(String text, String template) {
        // 将模板中的变量占位符转换为正则表达式，匹配任意非空白字符序列
        String regex = template.replaceAll("\\{\\w+}", "\\\\E.+?\\\\Q");
        regex = "\\Q" + regex + "\\E";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        return matcher.matches();
    }

    @EventHandler
    public void onPlayerBuy(ShopBuyEvent event) {
        event.setCancelled(true);
    }*/
}
