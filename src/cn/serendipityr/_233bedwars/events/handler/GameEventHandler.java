package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.*;
import cn.serendipityr._233bedwars.addons.shopItems.RecoverBed;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReSpawnEvent;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GameEventHandler implements Listener {
    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        GameState state = event.getNewState();
        IArena arena = event.getArena();
        // 游戏开局时
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
            if (ConfigManager.addon_generatorEditor) {
                GeneratorEditor.initGame(arena);
            }
            if (ConfigManager.addon_shopItemAddon) {
                ShopItemAddon.initGame(arena);
            }
        }
        // 游戏结束时
        if (state.equals(GameState.restarting)) {
            if (ConfigManager.addon_generatorEditor) {
                GeneratorEditor.resetArena(arena);
            }
            PlaceholderUtil.resetArenaRiskyTeams(arena);
            if (ConfigManager.addon_shopItemAddon) {
                ShopItemAddon.resetGame(arena);
            }
        }
        // 每当游戏状态改变时
        if (ConfigManager.addon_scoreBoardEditor) {
            for (Player player : arena.getPlayers()) {
                ScoreboardEditor.editScoreBoard(arena, player);
            }
        }
        if (ConfigManager.addon_actionBar) {
            ActionBar.initGame(arena, false);
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
        if (ConfigManager.addon_actionBar) {
            ActionBar.initGame(arena, true);
        }
        if (ConfigManager.addon_xpResMode) {
            XpResMode.giveItems(player);
            XpResMode.initPlayer(player);
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

    @EventHandler
    public void onPlayerBuyItem(ShopBuyEvent event) {
        Player player = event.getBuyer();
        IArena arena = event.getArena();
        ICategoryContent content = event.getCategoryContent();
        if (ShopItemAddon.handleShopBuy(player, arena, content)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedDestroy(PlayerBedBreakEvent event) {
        IArena arena = event.getArena();
        ITeam victim = event.getVictimTeam();
        ShopItemAddon.handleBedDestroy(arena, victim);
    }
}
