package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.NMSUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenHolo;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.arena.OreGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    public static void loadConfig(YamlConfiguration cfg) {
        gen_split = cfg.getBoolean("gen_split");
        period = cfg.getDouble("period");
        horizontal_speed = cfg.getDouble("horizontal_speed");
        vertical_speed = cfg.getDouble("vertical_speed");
        vertical_amplitude = cfg.getDouble("vertical_amplitude");
        vertical_offset = cfg.getDouble("vertical_offset");
        text_holograms_offset = cfg.getDouble("text_holograms_offset");

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
        }
    }

    public static void initGame(IArena arena) {
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
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
        }, 65L);
    }

    public static void setTextHologramsOffset(IGenerator generator) {
        for (Language lang : Language.getLanguages()) {
            String iso = lang.getIso();
            IGenHolo armorstands = generator.getLanguageHolograms().get(iso);
            try {
                Class<?> cls = armorstands.getClass();
                Field tierField = cls.getDeclaredField("tier");
                Field timerField = cls.getDeclaredField("timer");
                Field nameField = cls.getDeclaredField("name");
                tierField.setAccessible(true);
                timerField.setAccessible(true);
                nameField.setAccessible(true);
                ArmorStand tier = (ArmorStand) tierField.get(armorstands);
                ArmorStand timer = (ArmorStand) timerField.get(armorstands);
                ArmorStand name = (ArmorStand) nameField.get(armorstands);

                double baseY = generator.getLocation().getY() + text_holograms_offset;

                Location tier_loc = tier.getLocation();
                tier_loc.setY(baseY + 3.0);
                tier.teleport(tier_loc);

                Location timer_loc = timer.getLocation();
                timer_loc.setY(baseY + 2.7);
                timer.teleport(timer_loc);

                Location name_loc = name.getLocation();
                name_loc.setY(baseY + 2.4);
                name.teleport(name_loc);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LogUtil.consoleLog("&9233BedWars &3&l> &e[GeneratorEditor] &c发生致命错误！");
                e.printStackTrace();
            }
        }
    }

    public static void rotate(ArmorStand item) {
        Location location = item.getLocation();

        if (!item.hasMetadata("temp")) {
            Object[] temp = {item.getLocation().getY() + vertical_offset, 0D, 0D, true, true};
            item.setMetadata("temp", new FixedMetadataValue(_233BedWars.getInstance(), temp));
        }

        Object[] temp = (Object[]) item.getMetadata("temp").get(0).value();
        double init_y = (double) temp[0];
        double horizontal_algebra = (double) temp[1];
        double vertical_algebra = (double) temp[2];
        boolean horizontal_direction = (boolean) temp[3];
        boolean vertical_direction = (boolean) temp[4];

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

        Object[] _temp = {init_y ,horizontal_algebra, vertical_algebra, horizontal_direction, vertical_direction};
        item.setMetadata("temp", new FixedMetadataValue(_233BedWars.getInstance(), _temp));
    }

    public static void resetArena(IArena arena) {
        for (IGenerator generator : arena.getOreGenerators()) {
            rotations.remove(generator.getHologramHolder());
            oreGenerators.remove(generator);
        }
    }

    public static void rotateGenerators() {
        for (ArmorStand item : rotations) {
            rotate(item);
            // rotate_new(item);
        }
    }

    public static void markThrownItem(Player player, Item item) {
        if (gen_split) {
            IArena arena = ProviderUtil.bw.getArenaUtil().getArenaByPlayer(player);
            if (arena == null) {
                return;
            }
            item.setMetadata("thrown_item", new FixedMetadataValue(_233BedWars.getInstance(), true));
        }
    }

    public static void handlePickUp(Player player, Item item) {
        ItemStack itemStack = item.getItemStack();
        // Gen Split
        if (gen_split) {
            if (item.hasMetadata("thrown_item")) {
                return;
            }
            int giveLevels = XpResMode.calcExpLevel(itemStack.getType(), itemStack.getAmount(), false);
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
}
