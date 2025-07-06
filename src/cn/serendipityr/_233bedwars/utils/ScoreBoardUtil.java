package cn.serendipityr._233bedwars.utils;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.config.ConfigManager;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.sidebar.ISidebar;
import com.andrei1058.bedwars.api.sidebar.ISidebarService;
import com.andrei1058.bedwars.libs.sidebar.PlaceholderProvider;
import com.andrei1058.bedwars.libs.sidebar.Sidebar;
import com.andrei1058.bedwars.sidebar.BwSidebarLine;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class ScoreBoardUtil {
    static ISidebarService sms;

    public static void init() {
        sms = ProviderUtil.bw.getScoreboardManager();
        if (ConfigManager.addon_scoreBoardEditor) {
            for (Language lang : Language.getLanguages()) {
                lang.getYml().set(Messages.FORMATTING_SCOREBOARD_TEAM_GENERIC, "");
            }
        }
    }

    public static void setScoreBoardContent(Player player, List<String> titles, List<String> _lines) {
        ISidebar sm = sms.getSidebar(player);
        ArrayList<String> lines = new ArrayList<>(_lines);
        Collections.reverse(lines);
        if (sm != null) {
            Sidebar handle = sm.getHandle();
            new BukkitRunnable() {
                @Override
                public void run() {
                    int currentLineCount = handle.lineCount();
                    int desiredLineCount = lines.size();

                    for (int i = currentLineCount; i < desiredLineCount; i++) {
                        handle.addLine(new BwSidebarLine("", ""));
                    }

                    for (int i = currentLineCount - 1; i >= desiredLineCount; i--) {
                        handle.removeLine(i);
                    }
                }
            }.runTaskLater(_233BedWars.getInstance(), 3L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    handle.setTitle(sm.normalizeTitle(titles));

                    PlaceholderUtil.addNativeScoreBoardPlaceHolders(player);

                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        BwSidebarLine content = new BwSidebarLine(line, "");
                        handle.setLine(content, i);
                    }
                }
            }.runTaskLater(_233BedWars.getInstance(), 5L);
        }
    }

    public static void addPlaceHolder(Player player, String placeHolder, Callable<String> value) {
        ISidebar sm = sms.getSidebar(player);
        if (sm != null) {
            Sidebar handle = sm.getHandle();
            new BukkitRunnable() {
                @Override
                public void run() {
                    handle.addPlaceholder(new PlaceholderProvider(placeHolder, value));
                    handle.refreshPlaceholders();
                }
            }.runTaskLater(_233BedWars.getInstance(), 2L);
        }
    }
}
