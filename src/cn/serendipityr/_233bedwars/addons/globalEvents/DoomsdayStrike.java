package cn.serendipityr._233bedwars.addons.globalEvents;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.GlobalEvents;
import cn.serendipityr._233bedwars.utils.TitleUtil;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class DoomsdayStrike {
    public static Boolean enable;
    static Integer interval;
    static Integer notice_time;
    static Integer notice_title_fadeIn;
    static Integer notice_title_stay;
    static Integer notice_title_fadeOut;
    static Integer explosives_y_offset;
    static Integer explosives_radius;
    static Integer explosives_amount;
    static Integer explosives_tnt_fuse_ticks;
    static Integer explosives_power;
    static Boolean explosives_break_blocks;
    static Integer explosives_summon_delay;
    static Boolean explosives_protect_resources;
    static String messages_prepare_msg;
    static String messages_prepare_title;
    static String messages_prepare_subtitle;

    public static List<Entity> entities = new CopyOnWriteArrayList<>();

    public static void loadConfig(YamlConfiguration cfg) {
        enable = cfg.getBoolean("events.doomsday_strike.enable");
        if (enable) {
            GlobalEvents.enable_events.add("doomsday_strike");
        }
        interval = cfg.getInt("events.doomsday_strike.interval");
        notice_time = cfg.getInt("events.doomsday_strike.notice.time");
        notice_title_fadeIn = cfg.getInt("events.doomsday_strike.notice.title_fadeIn");
        notice_title_stay = cfg.getInt("events.doomsday_strike.notice.title_stay");
        notice_title_fadeOut = cfg.getInt("events.doomsday_strike.notice.title_fadeOut");
        explosives_y_offset = cfg.getInt("events.doomsday_strike.explosives.y_offset");
        explosives_radius = cfg.getInt("events.doomsday_strike.explosives.radius");
        explosives_amount = cfg.getInt("events.doomsday_strike.explosives.amount");
        explosives_tnt_fuse_ticks = cfg.getInt("events.doomsday_strike.explosives.tnt_fuse_ticks");
        explosives_power = cfg.getInt("events.doomsday_strike.explosives.power");
        explosives_break_blocks = cfg.getBoolean("events.doomsday_strike.explosives.break_blocks");
        explosives_summon_delay = cfg.getInt("events.doomsday_strike.explosives.summon_delay");
        explosives_protect_resources = cfg.getBoolean("events.doomsday_strike.explosives.protect_resources");
        messages_prepare_msg = cfg.getString("messages.doomsday_strike_prepare_msg").replace("&", "§").replace("{radiation}", "☢");
        messages_prepare_title = cfg.getString("messages.doomsday_strike_prepare_title").replace("&", "§").replace("{radiation}", "☢");
        messages_prepare_subtitle = cfg.getString("messages.doomsday_strike_prepare_subtitle").replace("&", "§").replace("{radiation}", "☢");
    }

    public static void initEvent(IArena arena) {
        List<Location> centers = new ArrayList<>();
        World world = arena.getWorld();
        new BukkitRunnable() {
            int i = 0;
            boolean init = false;

            public void run() {
                if (arena.getStatus() != GameState.playing) {
                    resetEntitiesMap(world);
                    this.cancel();
                }

                if (centers.isEmpty()) {
                    for (IGenerator generator : arena.getOreGenerators()) {
                        if (generator.getType() == GeneratorType.EMERALD) {
                            Location ct = generator.getLocation().clone();
                            ct.setY(ct.getY() + explosives_y_offset);
                            centers.add(ct);
                        }
                    }
                }

                i--;

                if (i == notice_time) {
                    for (Player p : arena.getPlayers()) {
                        if (!messages_prepare_msg.trim().isEmpty()) {
                            p.sendMessage(messages_prepare_msg.replace("{notice_time}", String.valueOf(notice_time)));
                        }
                        if (!(messages_prepare_title.trim().isEmpty() && messages_prepare_subtitle.trim().isEmpty())) {
                            TitleUtil.send(p,
                                    messages_prepare_title.replace("{notice_time}", String.valueOf(notice_time)),
                                    messages_prepare_subtitle.replace("{notice_time}", String.valueOf(notice_time)),
                                    notice_title_fadeIn, notice_title_stay, notice_title_fadeOut);
                        }
                    }
                }

                if (i <= 0) {
                    i = interval;
                    if (!init) {
                        init = true;
                        return;
                    }
                    resetEntitiesMap(world);
                    if (centers.isEmpty()) {
                        return;
                    }
                    Random random = new Random();
                    for (int i = 0; i < explosives_amount; i++) {
                        int finalI = i;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Location center = centers.get(random.nextInt(centers.size()));
                                double angle = 2 * Math.PI * finalI / explosives_amount;
                                double xOffset = explosives_radius * Math.cos(angle) * random.nextDouble();
                                double zOffset = explosives_radius * Math.sin(angle) * random.nextDouble();
                                Location spawnLoc = center.clone().add(xOffset, 0, zOffset);
                                if (random.nextBoolean()) {
                                    TNTPrimed tnt = world.spawn(spawnLoc, TNTPrimed.class);
                                    tnt.setFuseTicks(explosives_tnt_fuse_ticks);
                                    tnt.setVelocity(new Vector(0, -1, 0));
                                    tnt.setYield(explosives_power);
                                    if (!explosives_break_blocks || explosives_protect_resources) {
                                        entities.add(tnt);
                                    }
                                } else {
                                    Fireball fireball = world.spawn(spawnLoc, Fireball.class);
                                    fireball.setIsIncendiary(false);
                                    fireball.setDirection(new Vector(0, -1, 0));
                                    fireball.setYield(explosives_power);
                                    if (!explosives_break_blocks || explosives_protect_resources) {
                                        entities.add(fireball);
                                    }
                                }
                            }
                        }.runTaskLater(_233BedWars.getInstance(), explosives_summon_delay * i);
                    }
                }
            }
        }.runTaskTimer(_233BedWars.getInstance(), 0L, 20L);
    }

    public static void handleEntityExplode(Entity entity, List<Block> blocks) {
        if (!explosives_break_blocks && entities.contains(entity)) {
            blocks.clear();
        }
    }

    public static boolean handleEntityDamageByEntity(Entity damager, Entity victim) {
        if (explosives_protect_resources && entities.contains(damager)) {
            ItemStack item = ((Item) victim).getItemStack();
            return List.of(Material.EMERALD, Material.DIAMOND).contains(item.getType());
        }
        return false;
    }

    private static void resetEntitiesMap(World world) {
        entities.removeIf(entity -> entity.getWorld().equals(world));
    }
}
