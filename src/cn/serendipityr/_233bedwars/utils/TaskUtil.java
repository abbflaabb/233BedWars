package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.GeneratorEditor;
import org.bukkit.Bukkit;

public class TaskUtil {
    public static void initOneTickTask() {
        Bukkit.getScheduler().runTaskTimer(_233BedWars.getInstance(), () -> {
            GeneratorEditor.rotateGenerators();
            PlaceholderUtil.updateTeamHeart();
        }, 120L, 1L);
    }
}
