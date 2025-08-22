package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.MathUtil;
import cn.serendipityr._233bedwars.utils.NMSUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenHolo;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.OreGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GeneratorEditor {
    public static CopyOnWriteArrayList<ArmorStand> rotations = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<IGenerator> oreGenerators = new CopyOnWriteArrayList<>();
    static Boolean gen_split;
    static Boolean gen_split_backup;
    static Boolean rotate_gen_backup;
    static double period;
    static double horizontal_speed;
    static double vertical_speed;
    static double vertical_amplitude;
    static double vertical_offset;
    static double text_holograms_offset;
    static Boolean disable_nms_packet;
    static Boolean edit_holograms_texts_enable;
    static Boolean edit_holograms_texts_tier_enable;
    static String edit_holograms_texts_tier;
    static Boolean edit_holograms_texts_timer_enable;
    static String edit_holograms_texts_timer;
    static Boolean edit_holograms_texts_name_enable;
    static String edit_holograms_texts_name;
    static String edit_holograms_place_holders_non_full;
    static String edit_holograms_place_holders_full;
    static int edit_holograms_place_holders_progress_length;
    static String edit_holograms_place_holders_progress_unit;
    static String edit_holograms_place_holders_color_current;
    static String edit_holograms_place_holders_color_left;

    static Class<?> IGENHOLO_CLASS;
    static Class<?> IGENERATOR_CLASS;
    static Field TIER_FIELD;
    static Field TIMER_FIELD;
    static Field NAME_FIELD;
    static Field UPGRADE_STAGE_FIELD;

    static ConcurrentHashMap<IGenerator, ArmorStand> timer_holograms = new ConcurrentHashMap<>();
    static ConcurrentHashMap<IGenerator, ArmorStand> name_holograms = new ConcurrentHashMap<>();
    static ConcurrentHashMap<IGenerator, Boolean> generator_full_check = new ConcurrentHashMap<>();
    static ConcurrentHashMap<IGenHolo, String> holo_name_map = new ConcurrentHashMap<>();
    static int spawn_check_ticks = 20;
    static int current_check_ticks = 0;

    public static void loadConfig(YamlConfiguration cfg) {
        gen_split = cfg.getBoolean("gen_split");
        period = cfg.getDouble("period");
        horizontal_speed = cfg.getDouble("horizontal_speed");
        vertical_speed = cfg.getDouble("vertical_speed");
        vertical_amplitude = cfg.getDouble("vertical_amplitude");
        vertical_offset = cfg.getDouble("vertical_offset");
        text_holograms_offset = cfg.getDouble("text_holograms_offset");
        disable_nms_packet = cfg.getBoolean("disable_nms_packet");

        edit_holograms_texts_enable = cfg.getBoolean("edit_holograms_text.enable");
        edit_holograms_texts_tier_enable = cfg.getBoolean("edit_holograms_text.texts.tier.enable");
        edit_holograms_texts_tier = cfg.getString("edit_holograms_text.texts.tier.content").replace("&", "§");
        edit_holograms_texts_timer_enable = cfg.getBoolean("edit_holograms_text.texts.timer.enable");
        edit_holograms_texts_timer = cfg.getString("edit_holograms_text.texts.timer.content").replace("&", "§");
        edit_holograms_texts_name_enable = cfg.getBoolean("edit_holograms_text.texts.name.enable");
        edit_holograms_texts_name = cfg.getString("edit_holograms_text.texts.name.content").replace("&", "§");
        edit_holograms_place_holders_non_full = cfg.getString("edit_holograms_text.place_holders.seconds_or_full.non_full").replace("&", "§");
        edit_holograms_place_holders_full = cfg.getString("edit_holograms_text.place_holders.seconds_or_full.full").replace("&", "§");
        edit_holograms_place_holders_progress_length = cfg.getInt("edit_holograms_text.place_holders.progress.length");
        edit_holograms_place_holders_progress_unit = cfg.getString("edit_holograms_text.place_holders.progress.unit");
        edit_holograms_place_holders_color_current = cfg.getString("edit_holograms_text.place_holders.progress.color_current").replace("&", "§");
        edit_holograms_place_holders_color_left = cfg.getString("edit_holograms_text.place_holders.progress.color_left").replace("&", "§");

        if (gen_split_backup != null) {
            ProviderUtil.bw.getConfigs().getMainConfig().getYml().set(ConfigPath.GENERAL_CONFIGURATION_ENABLE_GEN_SPLIT, gen_split_backup);
        } else {
            gen_split_backup = ProviderUtil.bw.getConfigs().getMainConfig().getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_GEN_SPLIT);
        }

        if (rotate_gen_backup != null) {
            ProviderUtil.bw.getConfigs().getMainConfig().getYml().set(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_ROTATE_GEN, rotate_gen_backup);
        } else {
            rotate_gen_backup = ProviderUtil.bw.getConfigs().getMainConfig().getYml().getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_ROTATE_GEN);
        }

        if (ConfigManager.addon_generatorEditor) {
            ProviderUtil.bw.getConfigs().getMainConfig().getYml().set(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_ROTATE_GEN, false);
            if (gen_split) {
                ProviderUtil.bw.getConfigs().getMainConfig().getYml().set(ConfigPath.GENERAL_CONFIGURATION_ENABLE_GEN_SPLIT, false);
            }
            for (IGenerator generator : oreGenerators) {
                if (gen_split) {
                    generator.setStack(false);
                } else {
                    generator.setStack(BedWars.getGeneratorsCfg().getBoolean("stack-items"));
                }
                setTextHologramsOffset(generator);
            }
            if (edit_holograms_texts_enable && edit_holograms_texts_timer_enable) {
                for (Language lang : Language.getLanguages()) {
                    lang.getYml().set(Messages.GENERATOR_HOLOGRAM_TIMER, "§f");
                }
            }
        }
    }

    public static void initArena(IArena arena) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<IGenerator> generators = new ArrayList<>(arena.getOreGenerators());
                for (IGenerator generator : generators) {
                    if (gen_split) {
                        generator.setStack(false);
                    } else {
                        generator.setStack(BedWars.getGeneratorsCfg().getBoolean("stack-items"));
                    }
                    if (!generator.getType().equals(GeneratorType.IRON) && !generator.getType().equals(GeneratorType.GOLD)) {
                        OreGenerator.getRotation().remove((OreGenerator) generator);
                        ArmorStand item = generator.getHologramHolder();
                        item.setHeadPose(new EulerAngle(0, 0, 0));
                        setTextHologramsOffset(generator);
                        rotations.add(item);
                        oreGenerators.add(generator);
                    }
                }
            }
        }.runTaskLater(_233BedWars.getInstance(), 65L);
    }

    public static void resetArena(IArena arena) {
        for (IGenerator generator : arena.getOreGenerators()) {
            rotations.remove(generator.getHologramHolder());
            oreGenerators.remove(generator);
            timer_holograms.remove(generator);
            name_holograms.remove(generator);
            generator_full_check.remove(generator);
            for (IGenHolo holo : generator.getLanguageHolograms().values()) {
                holo_name_map.remove(holo);
            }
        }
    }

    public static void setTextHologramsOffset(IGenerator generator) {
        for (Language lang : Language.getLanguages()) {
            String iso = lang.getIso();
            IGenHolo armor_stands = generator.getLanguageHolograms().get(iso);
            try {
                if (IGENHOLO_CLASS == null) {
                    IGENHOLO_CLASS = armor_stands.getClass();
                    TIER_FIELD = IGENHOLO_CLASS.getDeclaredField("tier");
                    TIER_FIELD.setAccessible(true);
                    TIMER_FIELD = IGENHOLO_CLASS.getDeclaredField("timer");
                    TIMER_FIELD.setAccessible(true);
                    NAME_FIELD = IGENHOLO_CLASS.getDeclaredField("name");
                    NAME_FIELD.setAccessible(true);
                }

                ArmorStand tier = (ArmorStand) TIER_FIELD.get(armor_stands);
                ArmorStand timer = (ArmorStand) TIMER_FIELD.get(armor_stands);
                ArmorStand name = (ArmorStand) NAME_FIELD.get(armor_stands);

                double baseY = generator.getLocation().getY() + text_holograms_offset;

                Location tier_loc = tier.getLocation();
                tier_loc.setY(baseY + 3.0);
                tier.teleport(tier_loc);

                Location timer_loc = timer.getLocation();
                if (edit_holograms_texts_enable && edit_holograms_texts_timer_enable) {
                    timer_loc.setY(baseY + 256);
                } else {
                    timer_loc.setY(baseY + 2.7);
                }
                timer.teleport(timer_loc);

                Location name_loc = name.getLocation();
                if (edit_holograms_texts_enable && edit_holograms_texts_name_enable) {
                    name_loc.setY(baseY + 256);
                } else {
                    name_loc.setY(baseY + 2.4);
                }
                name.teleport(name_loc);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LogUtil.consoleLog("&9233BedWars &3&l> &e[GeneratorEditor] &c发生致命错误！");
                e.printStackTrace();
            }
        }
    }

    /*public static void rotate(ArmorStand item) {
        Location location = item.getLocation();

        if (!item.hasMetadata("init_y")) {
            item.setMetadata("horizontal_algebra", new FixedMetadataValue(_233BedWars.getInstance(), 0D));
            item.setMetadata("vertical_algebra", new FixedMetadataValue(_233BedWars.getInstance(), 0D));
            item.setMetadata("init_y", new FixedMetadataValue(_233BedWars.getInstance(), location.getY()));
            item.setMetadata("horizontal_direction", new FixedMetadataValue(_233BedWars.getInstance(), true));
            item.setMetadata("vertical_direction", new FixedMetadataValue(_233BedWars.getInstance(), true));
        }

        double horizontal_algebra = (double) item.getMetadata("horizontal_algebra").get(0).value();
        double vertical_algebra = (double) item.getMetadata("vertical_algebra").get(0).value();
        double init_y = (double) item.getMetadata("init_y").get(0).value() + vertical_offset;
        boolean horizontal_direction = (boolean) item.getMetadata("horizontal_direction").get(0).value();
        boolean vertical_direction = (boolean) item.getMetadata("vertical_direction").get(0).value();

        if (vertical_algebra >= period) {
            vertical_algebra = 0;
            vertical_direction = !vertical_direction;
        } else {
            vertical_algebra += vertical_speed;
        }

        if (horizontal_algebra >= period) {
            horizontal_algebra = 0;
            horizontal_direction = !horizontal_direction;
        } else {
            horizontal_algebra += horizontal_speed;
        }

        double horizontal_sin = Math.sin(Math.PI * horizontal_algebra / period);
        double vertical_sin = Math.sin(Math.PI * vertical_algebra / period);

        double move_y;
        if (!vertical_direction) {
            move_y = init_y + vertical_amplitude * vertical_sin;
        } else {
            move_y = init_y - vertical_amplitude * vertical_sin;
        }

        float move_yaw;
        if (!horizontal_direction) {
            move_yaw = (float) -horizontal_sin * 360;
        } else {
            move_yaw = (float) horizontal_sin * 360;
        }

        Object teleportPacket = NMSUtil.getEntityTeleportPacket(item.getEntityId(), (int) (location.getX() * 32.0D), (int) (move_y * 32.0D), (int) (location.getZ() * 32.0D), (byte) (move_yaw * 256.0f / 360.0f), (byte) (location.getPitch() * 256.0f / 360.0f), true);
        for (Player p : location.getWorld().getPlayers()) {
            Object nmsPlayer = NMSUtil.getNMSPlayer(p);
            Object nmsPlayerConnection = NMSUtil.getNMSPlayerConnection(nmsPlayer);
            NMSUtil.sendPacket(nmsPlayerConnection, teleportPacket);
        }

        item.setMetadata("vertical_algebra", new FixedMetadataValue(_233BedWars.getInstance(), vertical_algebra));
        item.setMetadata("horizontal_algebra", new FixedMetadataValue(_233BedWars.getInstance(), horizontal_algebra));
        item.setMetadata("vertical_direction", new FixedMetadataValue(_233BedWars.getInstance(), vertical_direction));
        item.setMetadata("horizontal_direction", new FixedMetadataValue(_233BedWars.getInstance(), horizontal_direction));
    }*/

    static ConcurrentHashMap<ArmorStand, Double> yaw_angle_map = new ConcurrentHashMap<>();
    static ConcurrentHashMap<ArmorStand, Double> vertical_algebra_map = new ConcurrentHashMap<>();
    static ConcurrentHashMap<ArmorStand, Double> init_y_map = new ConcurrentHashMap<>();
    static ConcurrentHashMap<ArmorStand, Boolean> vertical_direction_map = new ConcurrentHashMap<>();

    public static void rotate(ArmorStand item) {
        Location location = item.getLocation().clone();

        if (!init_y_map.containsKey(item)) {
            yaw_angle_map.put(item, 0D);
            vertical_algebra_map.put(item, 0D);
            init_y_map.put(item, location.getY());
            vertical_direction_map.put(item, true);
        }

        double move_yaw = yaw_angle_map.get(item);
        double vertical_algebra = vertical_algebra_map.get(item);
        double init_y = init_y_map.get(item) + vertical_offset;
        boolean vertical_direction = vertical_direction_map.get(item);

        if (vertical_algebra >= period) {
            vertical_algebra = 0;
            vertical_direction = !vertical_direction;
        } else {
            vertical_algebra += vertical_speed;
        }

        double vertical_sin = Math.sin(Math.PI * vertical_algebra / period);
        double move_y = vertical_direction
                ? init_y - vertical_amplitude * vertical_sin
                : init_y + vertical_amplitude * vertical_sin;

        move_yaw += 8 * horizontal_speed;

        Object nmsArmorStand = NMSUtil.getNMSArmorStand(item);
        Object teleportPacket;
        if (NMSUtil.getServerVersion().equals("v1_8_R3") && !disable_nms_packet) {
            if (move_yaw >= 256.0) {
                move_yaw -= 256.0;
            }
            teleportPacket = NMSUtil.getEntityTeleportPacket(item.getEntityId(), (int) (location.getX() * 32.0D), (int) (move_y * 32.0D), (int) (location.getZ() * 32.0D), (byte) move_yaw, (byte) (location.getPitch() * 256.0f / 360.0f), true);
        } else {
            if (move_yaw >= 360.0) {
                move_yaw -= 360.0;
            }
            location.setY(move_y);
            location.setYaw((float) move_yaw);
            item.teleport(location);
            teleportPacket = NMSUtil.getEntityTeleportPacket(nmsArmorStand);
        }

        if (!disable_nms_packet) {
            for (Player p : location.getWorld().getPlayers()) {
                Object nmsPlayer = NMSUtil.getNMSPlayer(p);
                Object nmsPlayerConnection = NMSUtil.getNMSPlayerConnection(nmsPlayer);
                NMSUtil.sendPacket(nmsPlayerConnection, teleportPacket);
            }
        }

        yaw_angle_map.put(item, move_yaw);
        vertical_algebra_map.put(item, vertical_algebra);
        vertical_direction_map.put(item, vertical_direction);
    }

    public static void rotateGenerators() {
        for (ArmorStand item : rotations) {
            rotate(item);
        }
    }

    public static void updateGeneratorTexts() {
        if (!edit_holograms_texts_enable) {
            return;
        }

        boolean check_spawn = false;
        current_check_ticks++;
        if (current_check_ticks >= spawn_check_ticks) {
            current_check_ticks = 0;
            check_spawn = true;
        }

        for (IGenerator generator : oreGenerators) {
            if (check_spawn) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        generator_full_check.put(generator, checkFull(generator));
                    }
                }.runTask(_233BedWars.getInstance());
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (IGENERATOR_CLASS == null) {
                            IGENERATOR_CLASS = generator.getClass();
                        }

                        if (UPGRADE_STAGE_FIELD == null) {
                            UPGRADE_STAGE_FIELD = IGENERATOR_CLASS.getDeclaredField("upgradeStage");
                            UPGRADE_STAGE_FIELD.setAccessible(true);
                        }

                        if (!UPGRADE_STAGE_FIELD.canAccess(generator)) {
                            UPGRADE_STAGE_FIELD.setAccessible(true);
                            return;
                        }

                        Integer upgrade_stage = (Integer) UPGRADE_STAGE_FIELD.get(generator);
                        Boolean full_check = generator_full_check.getOrDefault(generator, false);

                        for (Language lang : Language.getLanguages()) {
                            String iso = lang.getIso();
                            IGenHolo holo = generator.getLanguageHolograms().get(iso);

                            if (!holo_name_map.containsKey(holo)) {
                                holo_name_map.put(holo, getIHoloName(iso, generator.getOre().getType()));
                            }

                            String display_name = holo_name_map.get(holo);

                            if (IGENHOLO_CLASS == null) {
                                IGENHOLO_CLASS = holo.getClass();
                                NAME_FIELD = IGENHOLO_CLASS.getDeclaredField("name");
                                NAME_FIELD.setAccessible(true);
                            }

                            if (edit_holograms_texts_tier_enable) {
                                holo.setTierName(setPlaceHolders(edit_holograms_texts_tier, generator, upgrade_stage, display_name, full_check));
                            }

                            if (edit_holograms_texts_timer_enable) {
                                String timer_name = setPlaceHolders(edit_holograms_texts_timer, generator, upgrade_stage, display_name, full_check);
                                if (timer_holograms.containsKey(generator)) {
                                    timer_holograms.get(generator).setCustomName(timer_name);
                                } else {
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (timer_holograms.containsKey(generator)) {
                                                return;
                                            }
                                            ArmorStand timer_new = createArmorStand(timer_name, generator.getLocation().clone().add(0.0, 2.7 + text_holograms_offset, 0.0));
                                            timer_holograms.put(generator, timer_new);
                                        }
                                    }.runTask(_233BedWars.getInstance());
                                }
                            }

                            if (edit_holograms_texts_name_enable) {
                                String name = setPlaceHolders(edit_holograms_texts_name, generator, upgrade_stage, display_name, full_check);
                                if (name_holograms.containsKey(generator)) {
                                    name_holograms.get(generator).setCustomName(name);
                                    ((ArmorStand) NAME_FIELD.get(holo)).setCustomName("§f");
                                } else {
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            if (name_holograms.containsKey(generator)) {
                                                return;
                                            }
                                            ArmorStand name_new = createArmorStand(name, generator.getLocation().clone().add(0.0, 2.4 + text_holograms_offset, 0.0));
                                            name_holograms.put(generator, name_new);
                                        }
                                    }.runTask(_233BedWars.getInstance());
                                }
                            }
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        LogUtil.consoleLog("&9233BedWars &3&l> &e[GeneratorEditor#INIT] &c发生致命错误！");
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(_233BedWars.getInstance());
        }
    }

    public static void markThrownItem(Player player, Item item) {
        if (gen_split) {
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
            if (arena == null) {
                return;
            }
        }
        item.setMetadata("thrown_item", new FixedMetadataValue(_233BedWars.getInstance(), true));
    }

    public static void handlePickUp(Player player, Item item) {
        ItemStack itemStack = item.getItemStack();
        // Gen Split
        if (gen_split) {
            if (item.hasMetadata("thrown_item")) {
                return;
            }
            int giveLevels = XpResMode.calcExpLevel(itemStack.getType(), itemStack.getAmount(), false, null);
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
            if (arena == null) {
                return;
            }
            Collection<Entity> nearby;
            Material type = itemStack.getType();
            if (type == Material.IRON_INGOT || type == Material.GOLD_INGOT) {
                nearby = player.getWorld().getNearbyEntities(player.getLocation(), 2, 2, 2);
            } else {
                nearby = player.getWorld().getNearbyEntities(player.getLocation(), 1, 1, 1);
            }
            for (Entity entity : nearby) {
                if (entity instanceof Player && arena.isPlayer(player) && entity != player) {
                    Player _player = (Player) entity;
                    if (arena.isReSpawning(_player) || arena.isSpectator(_player)) {
                        continue;
                    }
                    if (ConfigManager.addon_xpResMode && XpResMode.isExpMode(_player)) {
                        _player.setLevel(_player.getLevel() + giveLevels);
                        _player.playSound(_player.getLocation(), Sound.valueOf(XpResMode.pick_up_sound[0]), Float.parseFloat(XpResMode.pick_up_sound[1]), Float.parseFloat(XpResMode.pick_up_sound[2]));
                    } else {
                        _player.getInventory().addItem(itemStack);
                        _player.playSound(player.getLocation(), Sound.valueOf(BedWars.getForCurrentVersion("ITEM_PICKUP", "ENTITY_ITEM_PICKUP", "ENTITY_ITEM_PICKUP")), 0.6f, 1.3f);
                    }
                }
            }
        }
    }

    private static String setPlaceHolders(String text, IGenerator generator, int upgrade_stage, String displayName, Boolean isFull) {
        return text
                .replace("{seconds_or_full}", isFull ? edit_holograms_place_holders_full : edit_holograms_place_holders_non_full)
                .replace("{progress}", getProgress(generator))
                .replace("{name}", displayName)
                .replace("{tier_int}", String.valueOf(upgrade_stage))
                .replace("{tier_roman}", MathUtil.intToRoman(upgrade_stage))
                .replace("{seconds}", String.valueOf(generator.getNextSpawn()))
                .replace("{unicode_right_arrow}", "➤");
    }

    private static String getIHoloName(String iso, Material type) {
        return Language.getLang(iso).m(type == Material.DIAMOND ? Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND : Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD);
    }


    private static Boolean checkFull(IGenerator generator) {
        int ore_count = 0;
        Location loc = generator.getLocation();
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 3, 3, 3)) {
            if (entity.getType() == EntityType.DROPPED_ITEM) {
                Item item = (Item) entity;
                if (item.getItemStack().getType() == generator.getOre().getType()) {
                    if (item.hasMetadata("thrown_item")) {
                        continue;
                    }
                    ore_count += item.getItemStack().getAmount();
                    if (ore_count >= generator.getSpawnLimit()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static String getProgress(IGenerator generator) {
        int current = Math.round((float) generator.getNextSpawn() / generator.getDelay() * edit_holograms_place_holders_progress_length);
        int left = edit_holograms_place_holders_progress_length - current;
        if (left < 0) {
            left = 0;
        }
        return edit_holograms_place_holders_color_current + String.join("", Collections.nCopies(left, edit_holograms_place_holders_progress_unit)) + edit_holograms_place_holders_color_left + String.join("", Collections.nCopies(current, edit_holograms_place_holders_progress_unit));
    }

    private static ArmorStand createArmorStand(String name, Location l) {
        ArmorStand a = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        a.setGravity(false);
        if (name != null) {
            a.setCustomName(name);
            a.setCustomNameVisible(true);
        }

        a.setRemoveWhenFarAway(false);
        a.setVisible(false);
        a.setCanPickupItems(false);
        a.setArms(false);
        a.setBasePlate(false);
        a.setMarker(true);
        return a;
    }
}
