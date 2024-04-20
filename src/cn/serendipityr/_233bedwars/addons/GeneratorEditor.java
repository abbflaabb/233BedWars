package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars._233BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.arena.OreGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GeneratorEditor {
    public static CopyOnWriteArrayList<ArmorStand> rotations = new CopyOnWriteArrayList<>();

    public static void test(IArena arena) {
        Bukkit.getScheduler().runTaskLater(_233BedWars.getInstance(), () -> {
            List<IGenerator> generators = new ArrayList<>(arena.getOreGenerators());
            for (IGenerator generator : generators) {
                if (!generator.getType().equals(GeneratorType.IRON) && !generator.getType().equals(GeneratorType.GOLD)) {
                    OreGenerator.getRotation().remove((OreGenerator) generator);
                    ArmorStand item = generator.getHologramHolder();
                    item.setHeadPose(new EulerAngle(0, 0, 0));
                    rotations.add(item);
                }
            }
        }, 65L);
    }

    public static void rotate(ArmorStand item) {
        if (!item.hasMetadata("algebra")) {
            item.setMetadata("algebra", new FixedMetadataValue(_233BedWars.getInstance(), 0));
            item.setMetadata("init_y", new FixedMetadataValue(_233BedWars.getInstance(), item.getLocation().getY() - 0.25));
            item.setMetadata("direction", new FixedMetadataValue(_233BedWars.getInstance(), true));
        }

        int algebra = (int) item.getMetadata("algebra").get(0).value();
        double init_y = (double) item.getMetadata("init_y").get(0).value();
        boolean direction = (boolean) item.getMetadata("direction").get(0).value();

        double rotateSpeed = 1.0;
        double period = 60.0;
        double vertical_amplitude = 0.25;

        Location location = item.getLocation();
        if (algebra >= period) {
            algebra = 0;
            direction = !direction;
        } else {
            algebra += rotateSpeed;
        }

        double sin = Math.sin(Math.PI * algebra / period);
        double sin_offset = Math.sin((Math.PI * algebra / period) + (2 / Math.PI));
        double move_y;
        float move_yaw = (float) sin * 180;

        if (!direction) {
            move_y = init_y - vertical_amplitude * sin_offset;
            move_yaw = -move_yaw;
        } else {
            move_y = init_y + vertical_amplitude * sin_offset;
        }

        location.setY(move_y);
        location.setYaw(move_yaw);

        item.teleport(location);
        item.setMetadata("algebra", new FixedMetadataValue(_233BedWars.getInstance(), algebra));
        item.setMetadata("direction", new FixedMetadataValue(_233BedWars.getInstance(), direction));
    }


    public static void resetArena(IArena arena) {
        for (IGenerator generator : arena.getOreGenerators()) {
            rotations.remove(generator.getHologramHolder());
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
