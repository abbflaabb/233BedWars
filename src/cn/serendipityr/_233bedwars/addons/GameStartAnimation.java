package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.TitleUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class GameStartAnimation {
    public static Integer total_ticks = 0;

    static Boolean animation_enable;
    static List<String> animation_content;
    static Boolean sound_enable;
    static String[] sound_content;

    public static void loadConfig(YamlConfiguration cfg) {
        animation_enable = cfg.getBoolean("gameStartAnimation.enable");
        animation_content = cfg.getStringList("gameStartAnimation.animations");
        animation_content.replaceAll(s -> s.replace("&", "§"));
        sound_enable = cfg.getBoolean("gameStartSound.enable");
        sound_content = cfg.getString("gameStartSound.sound").split(":");

        int total = 0;
        for (String s : animation_content) {
            String[] animation = s.split("#");
            long stay_ms = Integer.parseInt(animation[3]);
            int ticks = (int) Math.ceil((double) stay_ms / 50);
            total += ticks;
        }
        if (total_ticks == 0) {
            int last = Integer.parseInt(animation_content.get(animation_content.size() - 1).split("#")[4]);
            total_ticks = total + last;
        }
    }

    public static void initArena(IArena arena) {
        try {
            if (animation_enable) {
                runAnimation(arena);
            }
            if (sound_enable) {
                playSound(arena);
            }
        } catch (Exception e) {
            LogUtil.consoleLog("&9233BedWars &3&l> &c应用游戏开始动画时发生错误！");
            e.printStackTrace();
        }
    }

    private static void runAnimation(IArena arena) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String s : animation_content) {
                    String[] animation = s.split("#");
                    int fadeIn = Integer.parseInt(animation[2]);
                    long stay_ms = Integer.parseInt(animation[3]);
                    int fadeOut = Integer.parseInt(animation[4]);
                    int ticks = (int) Math.ceil((double) stay_ms / 50);
                    for (Player player : arena.getPlayers()) {
                        TitleUtil.send(player, animation[0], animation[1], fadeIn, ticks, fadeOut);
                    }
                    try {
                        Thread.sleep(stay_ms);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.runTaskAsynchronously(_233BedWars.getInstance());
    }

    private static void playSound(IArena arena) {
        Sound sound = Sound.valueOf(sound_content[0]);
        for (Player player : arena.getPlayers()) {
            player.playSound(player.getLocation(), sound, Float.parseFloat(sound_content[1]), Float.parseFloat(sound_content[2]));
        }
    }
}
