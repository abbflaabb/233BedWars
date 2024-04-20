package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenHolo;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.arena.OreGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GeneratorEditor {
    public static CopyOnWriteArrayList<ArmorStand> rotations = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<IGenerator> oreGenerators = new CopyOnWriteArrayList<>();
    static double period;
    static double horizontal_speed;
    static double vertical_speed;
    static double vertical_amplitude;
    static double vertical_offset;
    static double text_holograms_offset;

    public static void loadConfig(YamlConfiguration cfg) {
        period = cfg.getDouble("period");
        horizontal_speed = cfg.getDouble("horizontal_speed");
        vertical_speed = cfg.getDouble("vertical_speed");
        vertical_amplitude = cfg.getDouble("vertical_amplitude");
        vertical_offset = cfg.getDouble("vertical_offset");
        text_holograms_offset = cfg.getDouble("text_holograms_offset");
        for (IGenerator generator : oreGenerators) {
            setTextHologramsOffset(generator);
        }
    }

    public static void initGame(IArena arena) {
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
            List<IGenerator> generators = new ArrayList<>(arena.getOreGenerators());
            for (IGenerator generator : generators) {
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
                Class<?> cls = armorstands.getClass(); // 获取实例的类对象

                // 获取字段对象
                Field tierField = cls.getDeclaredField("tier");
                Field timerField = cls.getDeclaredField("timer");
                Field nameField = cls.getDeclaredField("name");

                // 如果字段是私有的，确保这一行
                tierField.setAccessible(true);
                timerField.setAccessible(true);
                nameField.setAccessible(true);

                // 读取字段值
                ArmorStand tier = (ArmorStand) tierField.get(armorstands);
                ArmorStand timer = (ArmorStand) timerField.get(armorstands);
                ArmorStand name = (ArmorStand) nameField.get(armorstands);

                Location tier_loc = tier.getLocation();
                tier_loc.setY(generator.getLocation().getY() + 3.0 + text_holograms_offset);
                Location timer_loc = timer.getLocation();
                timer_loc.setY(generator.getLocation().getY() + 2.7 + text_holograms_offset);
                Location name_loc = name.getLocation();
                name_loc.setY(generator.getLocation().getY() + 2.4 + text_holograms_offset);
                tier.teleport(tier_loc);
                timer.teleport(timer_loc);
                name.teleport(name_loc);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LogUtil.consoleLog("&9233BedWars &3&l > &e[GeneratorEditor] &c发生致命错误！");
                e.printStackTrace();
            }
        }
    }

    public static void rotate(ArmorStand item) {
        if (!item.hasMetadata("init_y")) {
            item.setMetadata("horizontal_algebra", new FixedMetadataValue(_233BedWars.getInstance(), 0D));
            item.setMetadata("vertical_algebra", new FixedMetadataValue(_233BedWars.getInstance(), 0D));
            item.setMetadata("init_y", new FixedMetadataValue(_233BedWars.getInstance(), item.getLocation().getY()));
            item.setMetadata("horizontal_direction", new FixedMetadataValue(_233BedWars.getInstance(), true));
            item.setMetadata("vertical_direction", new FixedMetadataValue(_233BedWars.getInstance(), true));
        }

        double horizontal_algebra = (double) item.getMetadata("horizontal_algebra").get(0).value();
        double vertical_algebra = (double) item.getMetadata("vertical_algebra").get(0).value();
        double init_y = (double) item.getMetadata("init_y").get(0).value() + vertical_offset;
        boolean horizontal_direction = (boolean) item.getMetadata("horizontal_direction").get(0).value();
        boolean vertical_direction = (boolean) item.getMetadata("vertical_direction").get(0).value();

        Location location = item.getLocation();

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

        location.setY(move_y);
        location.setYaw(move_yaw);
        item.teleport(location);

        item.setMetadata("vertical_algebra", new FixedMetadataValue(_233BedWars.getInstance(), vertical_algebra));
        item.setMetadata("horizontal_algebra", new FixedMetadataValue(_233BedWars.getInstance(), horizontal_algebra));
        item.setMetadata("vertical_direction", new FixedMetadataValue(_233BedWars.getInstance(), vertical_direction));
        item.setMetadata("horizontal_direction", new FixedMetadataValue(_233BedWars.getInstance(), horizontal_direction));
    }

    public static void resetArena(IArena arena) {
        for (IGenerator generator : arena.getOreGenerators()) {
            rotations.remove(generator.getHologramHolder());
            oreGenerators.remove(generator);
        }
    }

    public static void initOneTickTask() {
        Bukkit.getScheduler().runTaskTimer(_233BedWars.getInstance(), () -> {
            for (ArmorStand item : rotations) {
                rotate(item);
            }
        }, 120L, 1L);
    }
}
