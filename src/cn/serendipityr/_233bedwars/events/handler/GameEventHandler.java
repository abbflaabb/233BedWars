package cn.serendipityr._233bedwars.events.handler;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.*;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
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
            PlaceholderUtil.resetArenaRiskyTeams(arena);
        }
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
}
