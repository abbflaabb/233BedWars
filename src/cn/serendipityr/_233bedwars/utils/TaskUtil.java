package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ActionBar;
import cn.serendipityr._233bedwars.addons.GeneratorEditor;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskUtil {
    public static void initOneTickTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                GeneratorEditor.rotateGenerators();
            }
        }.runTaskTimer(_233BedWars.getInstance(), 120L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                GeneratorEditor.updateGeneratorTexts();
                PlaceholderUtil.updateTeamHeart();
                ActionBar.sendActionBar();
            }
        }.runTaskTimerAsynchronously(_233BedWars.getInstance(), 120L, 1L);
    }
}
