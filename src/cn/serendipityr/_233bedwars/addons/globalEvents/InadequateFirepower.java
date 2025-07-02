package cn.serendipityr._233bedwars.addons.globalEvents;

import cn.serendipityr._233bedwars.addons.GlobalEvents;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.upgrades.TeamUpgrade;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InadequateFirepower {
    public static Boolean enable;
    static Double generator_ratio;
    static Boolean generators_iron;
    static Boolean generators_gold;
    static Boolean generators_diamond;
    static Boolean generators_emerald;

    static List<IArena> arenas = new CopyOnWriteArrayList<>();

    public static void loadConfig(YamlConfiguration cfg) {
        enable = cfg.getBoolean("events.inadequate_firepower.enable");
        if (enable) {
            GlobalEvents.enable_events.add("inadequate_firepower");
        }
        generator_ratio = cfg.getDouble("events.inadequate_firepower.generator_ratio");
        generators_iron = cfg.getBoolean("events.inadequate_firepower.generators.iron");
        generators_gold = cfg.getBoolean("events.inadequate_firepower.generators.gold");
        generators_diamond = cfg.getBoolean("events.inadequate_firepower.generators.diamond");
        generators_emerald = cfg.getBoolean("events.inadequate_firepower.generators.emerald");
    }

    public static void initEvent(IArena arena) {
        arenas.add(arena);
        for (IGenerator generator : arena.getOreGenerators()) {
            editGenerator(generator);
        }
        for (ITeam team : arena.getTeams()) {
            for (IGenerator generator : team.getGenerators()) {
                editGenerator(generator);
            }
        }
    }

    public static void resetArena(IArena arena) {
        arenas.remove(arena);
    }

    public static void handleGeneratorUpdate(IGenerator generator) {
        editGenerator(generator);
    }

    public static void handleTeamUpgradeBuy(ITeam team, TeamUpgrade upgrade) {
        if (upgrade.getName().equals("upgrade-forge")) {
            for (IGenerator generator : team.getGenerators()) {
                editGenerator(generator);
            }
        }
    }

    private static void editGenerator(IGenerator generator) {
        if ((generators_iron && generator.getType().equals(GeneratorType.IRON))
                || (generators_gold && generator.getType().equals(GeneratorType.GOLD))
                || (generators_diamond && generator.getType().equals(GeneratorType.DIAMOND))
                || (generators_emerald && generator.getType().equals(GeneratorType.EMERALD))
        ) {
            int delay = (int) Math.round(generator.getDelay() / generator_ratio);
            generator.setDelay(delay);
            generator.setNextSpawn(delay);
        }
    }
}
