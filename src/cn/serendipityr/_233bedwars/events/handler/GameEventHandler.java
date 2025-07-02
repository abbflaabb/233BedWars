package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.*;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.gameplay.GeneratorUpgradeEvent;
import com.andrei1058.bedwars.api.events.player.*;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.api.events.upgrades.UpgradeBuyEvent;
import com.andrei1058.bedwars.api.upgrades.TeamUpgrade;
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
            if (ConfigManager.addon_globalEvents) {
                GlobalEvents.applyEvent(arena);
            }
            if (ConfigManager.addon_teamNameThemes) {
                TeamNameThemes.initGame(arena);
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
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
                for (Player player : arena.getPlayers()) {
                    FastCommands.giveItems(player);
                }
            }, 20L);
        }
        // 游戏结束时
        if (state.equals(GameState.restarting)) {
            if (ConfigManager.addon_globalEvents) {
                GlobalEvents.resetArena(arena);
            }
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
        IArena arena = event.getArena();
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> FastCommands.giveItems(player), 20L);
        if (ConfigManager.addon_xpResMode && XpResMode.isExpMode(player)) {
            player.setLevel(0);
        }
        if (ConfigManager.addon_globalEvents) {
            GlobalEvents.handlePlayerRespawn(arena, player);
        }
    }

    @EventHandler
    public void onPlayerJoinGame(PlayerJoinArenaEvent event) {
        IArena arena = event.getArena();
        Player player = event.getPlayer();
        if (ConfigManager.addon_scoreBoardEditor) {
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> ScoreboardEditor.editScoreBoard(arena, player), 5L);
        }
        if (arena.getStatus() == GameState.playing) {
            return;
        }
        if (ConfigManager.addon_actionBar) {
            ActionBar.initGame(arena, true);
        }
        if (ConfigManager.addon_xpResMode) {
            XpResMode.giveItems(player, arena);
            XpResMode.initPlayer(player, arena);
        }
        if (ConfigManager.addon_globalEvents) {
            GlobalEvents.initPlayer(player, arena);
        }
    }

    @EventHandler
    public void onPlayerRejoinGame(PlayerReJoinEvent event) {
        IArena arena = event.getArena();
        Player player = event.getPlayer();
        if (ConfigManager.addon_scoreBoardEditor) {
            Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> ScoreboardEditor.editScoreBoard(arena, player), 5L);
        }
        if (ConfigManager.addon_xpResMode && XpResMode.isExpMode(player)) {
            player.setLevel(0);
        }
    }

    @EventHandler
    public void onPlayerKillEvent(PlayerKillEvent event) {
        IArena arena = event.getArena();
        Player killer = event.getKiller();
        Player victim = event.getVictim();
        if (killer != null) {
            if (killer != victim) {
                if (ConfigManager.addon_combatDetails) {
                    CombatDetails.checkPlayerKillDistance(killer, victim);
                    CombatDetails.calcKillStreak(arena, killer, victim);
                }
            }
        }
        if (ConfigManager.addon_globalEvents) {
            GlobalEvents.handlePlayerDeath(arena, victim);
        }
    }

    @EventHandler
    public void onPlayerBuyItem(ShopBuyEvent event) {
        Player player = event.getBuyer();
        IArena arena = event.getArena();
        ICategoryContent content = event.getCategoryContent();
        if (ConfigManager.addon_shopItemAddon && ShopItemAddon.handleShopBuy(player, arena, content)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedDestroy(PlayerBedBreakEvent event) {
        IArena arena = event.getArena();
        ITeam victim = event.getVictimTeam();
        if (ConfigManager.addon_shopItemAddon) {
            ShopItemAddon.handleBedDestroy(arena, victim);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveArenaEvent event) {
        IArena arena = event.getArena();
        Player player = event.getPlayer();
        if (arena.getStatus() == GameState.waiting || arena.getStatus() == GameState.starting) {
            if (ConfigManager.addon_globalEvents) {
                GlobalEvents.resetPlayer(arena, player);
            }
        }
    }

    @EventHandler
    public void onGeneratorUpgrade(GeneratorUpgradeEvent event) {
        IGenerator generator = event.getGenerator();
        IArena arena = generator.getArena();
        if (ConfigManager.addon_globalEvents) {
            GlobalEvents.handleGeneratorUpgrade(arena, generator);
        }
    }

    @EventHandler
    public void onTeamUpgradeBuy(UpgradeBuyEvent event) {
        IArena arena = event.getArena();
        ITeam team = event.getTeam();
        TeamUpgrade upgrade = event.getTeamUpgrade();
        if (event.isCancelled()) {
            return;
        }
        if (ConfigManager.addon_globalEvents) {
            GlobalEvents.handleTeamUpgradeBuy(arena, team, upgrade);
        }
    }
}
