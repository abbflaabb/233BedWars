package cn.serendipityr._233bedwars.addons;

import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import cn.serendipityr._233bedwars.utils.ScoreBoardUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardEditor {
    static List<String> waiting_title;
    static List<String> waiting_lines;
    static List<String> starting_title;
    static List<String> starting_lines;
    static List<String> in_game_title;
    static List<String> in_game_lines;
    static List<String> restarting_title;
    static List<String> restarting_lines;

    public static void loadConfig(YamlConfiguration cfg) {
        waiting_title = cfg.getStringList("waiting.title");
        waiting_lines = cfg.getStringList("waiting.content");
        starting_title = cfg.getStringList("starting.title");
        starting_lines = cfg.getStringList("starting.content");
        in_game_title = cfg.getStringList("in-game.title");
        in_game_lines = cfg.getStringList("in-game.content");
        restarting_title = cfg.getStringList("restarting.title");
        restarting_lines = cfg.getStringList("restarting.content");
        waiting_title.replaceAll(s -> s.replace("&", "§"));
        waiting_lines.replaceAll(s -> s.replace("&", "§"));
        starting_title.replaceAll(s -> s.replace("&", "§"));
        starting_lines.replaceAll(s -> s.replace("&", "§"));
        in_game_title.replaceAll(s -> s.replace("&", "§"));
        in_game_lines.replaceAll(s -> s.replace("&", "§"));
    }

    public static void editScoreBoard(IArena arena, Player player) {
        switch (arena.getStatus()) {
            case waiting:
                ScoreBoardUtil.setScoreBoardContent(player, waiting_title, waiting_lines);
                break;
            case starting:
                ScoreBoardUtil.setScoreBoardContent(player, starting_title, starting_lines);
                break;
            case playing:
                List<String> in_game_lines = new ArrayList<>(ScoreboardEditor.in_game_lines);
                replaceElementWithElements(in_game_lines, "{allTeams}", getAllTeamsInfo(arena));
                ITeam exTeam = arena.getExTeam(player.getUniqueId());
                if (arena.isSpectator(player) && exTeam != null) {
                    in_game_lines.replaceAll(s -> s
                            .replace("{tHeart}", PlaceholderUtil.getTeamHeart(exTeam))
                            .replace("{tColor}", PlaceholderUtil.getTeamColor(exTeam))
                            .replace("{tName}", PlaceholderUtil.getTeamName(exTeam, player))
                            .replace("{tDanger}", PlaceholderUtil.getTeamDanger(exTeam))
                            .replace("{tDangerF}", PlaceholderUtil.getTeamDangerFull(exTeam))
                            .replace("{tAlive}", String.valueOf(PlaceholderUtil.getTeamAlive(exTeam)))
                            .replace("{tDistance}", String.valueOf(PlaceholderUtil.getTeamDistance(exTeam, player)))
                            .replace("{tIndicator}", PlaceholderUtil.getTeamIndicator(exTeam, player))
                    );
                }
                ScoreBoardUtil.setScoreBoardContent(player, in_game_title, in_game_lines);
                break;
            case restarting:
                List<String> restarting_lines = new ArrayList<>(ScoreboardEditor.restarting_lines);
                replaceElementWithElements(restarting_lines, "{allTeams}", getAllTeamsInfo(arena));
                ScoreBoardUtil.setScoreBoardContent(player, ScoreboardEditor.restarting_title, restarting_lines);
                break;
        }
    }

    static void replaceElementWithElements(List<String> list, String elementToReplace, List<String> newElements) {
        int index = list.indexOf(elementToReplace);
        if (index != -1) {
            // 移除找到的元素
            list.remove(index);
            // 在原位置插入新的元素列表
            list.addAll(index, newElements);
        }
    }

    static List<String> getAllTeamsInfo(IArena arena) {
        List<String> allTeamsInfo = new ArrayList<>();
        List<ITeam> teams = arena.getTeams();

        // 使用循环遍历所有队伍
        for (int i = 0; i < teams.size(); i += 2) {
            // 获取当前队伍的信息
            String currentTeamInfo = "{tInfo_" + teams.get(i).getName() + "}";

            // 检查是否有下一个队伍
            if (i + 1 < teams.size()) {
                // 如果有，将当前队伍和下一个队伍的信息组合起来
                String nextTeamInfo = "{tInfo_" + teams.get(i + 1).getName() + "}";
                allTeamsInfo.add(currentTeamInfo + "  " + nextTeamInfo);
            } else {
                // 如果没有，只添加当前队伍的信息
                allTeamsInfo.add(currentTeamInfo);
            }
        }
        return allTeamsInfo;
    }
}
