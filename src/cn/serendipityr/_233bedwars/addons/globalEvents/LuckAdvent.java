package cn.serendipityr._233bedwars.addons.globalEvents;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.GlobalEvents;
import cn.serendipityr._233bedwars.addons.shopItems.LuckyBlock;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class LuckAdvent {
    public static Boolean enable;
    static Integer interval;
    static Integer radius;
    static Integer amount;
    static Integer y_min;
    static Integer y_max;
    static Integer place_blocks_per_tick;
    static String message_luck_advent_refresh;

    public static void loadConfig(YamlConfiguration cfg) {
        enable = cfg.getBoolean("events.luck_advent.enable");
        if (enable) {
            GlobalEvents.enable_events.add("luck_advent");
        }
        interval = cfg.getInt("events.luck_advent.interval");
        radius = cfg.getInt("events.luck_advent.radius");
        amount = cfg.getInt("events.luck_advent.amount");
        y_min = cfg.getInt("events.luck_advent.y_min");
        y_max = cfg.getInt("events.luck_advent.y_max");
        place_blocks_per_tick = cfg.getInt("events.luck_advent.place_blocks_per_tick");
        message_luck_advent_refresh = cfg.getString("messages.luck_advent_refresh").replace("&", "§");
    }

    public static void initEvent(IArena arena) {
        new BukkitRunnable() {
            int i = 0;
            public void run() {
                if (arena.getStatus() != GameState.playing) {
                    LuckyBlock.getPlacedLuckyBlocks().removeIf(block -> block.getWorld().equals(arena.getWorld()));
                    this.cancel();
                }
                i--;
                if (i <= 0) {
                    i = interval;
                    placeLuckyBlocks(arena, arena.getReSpawnLocation(), y_max, y_min, radius, amount);
                }
            }
        }.runTaskTimerAsynchronously(_233BedWars.getInstance(), 0L, 20L);
    }

    public static void placeLuckyBlocks(IArena arena, Location center, int maxY, int minY, double radius, int amount) {
        World world = center.getWorld();
        List<Block> addBlocks = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            double angle = 2 * Math.PI * i / amount;

            double distance = radius * Math.sqrt(Math.random());
            double xOffset = distance * Math.cos(angle);
            double zOffset = distance * Math.sin(angle);

            int y = minY + (int) (Math.random() * (maxY - minY + 1));

            Location blockLoc = new Location(world, center.getX() + xOffset, y, center.getZ() + zOffset);
            if (arena.isProtected(blockLoc)) {
                continue;
            }
            addBlocks.add(blockLoc.getBlock());
        }

        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                int perTick = place_blocks_per_tick;

                for (int i = 0; i < perTick; i++, index++) {
                    if (index >= addBlocks.size()) {
                        for (Player player : arena.getPlayers()) {
                            player.sendMessage(message_luck_advent_refresh);
                        }
                        this.cancel();
                        return;
                    }
                    Block block = addBlocks.get(index);
                    block.setType(Material.getMaterial(LuckyBlock.lucky_block_material));
                    arena.addPlacedBlock(block);
                    LuckyBlock.getPlacedLuckyBlocks().add(block);
                }
            }
        }.runTaskTimer(_233BedWars.getInstance(), 0L, 1L);
    }
}
