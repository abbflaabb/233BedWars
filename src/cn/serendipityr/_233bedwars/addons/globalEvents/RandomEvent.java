package cn.serendipityr._233bedwars.addons.globalEvents;

import cn.serendipityr._233bedwars.addons.GlobalEvents;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomEvent {
    public static Boolean enable;
    public static List<String> disable_events = new ArrayList<>();

    public static void loadConfig(YamlConfiguration cfg) {
        enable = cfg.getBoolean("events.random.enable");
        disable_events = cfg.getStringList("events.random.disable_events");
    }

    public static String getRandomEvent() {
        List<String> random = new ArrayList<>(GlobalEvents.enable_events);
        random.removeAll(disable_events);
        if (random.isEmpty()) {
            return "none";
        }
        return random.get(new Random().nextInt(random.size()));
    }
}
