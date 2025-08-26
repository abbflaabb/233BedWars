package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars._233BedWars;
import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import cn.serendipityr._233bedwars.utils.LogUtil;
import cn.serendipityr._233bedwars.utils.PlaceholderUtil;
import cn.serendipityr._233bedwars.utils.ProviderUtil;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.shop.IBuyItem;
import com.andrei1058.bedwars.api.arena.shop.ICategoryContent;
import com.andrei1058.bedwars.api.arena.shop.IContentTier;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RecoverBed {
    public static String recover_bed_material;
    public static String recover_bed_section;
    public static Boolean settings_recover_bed_enable = false;
    static String settings_recover_bed_recover_sound;
    static Integer settings_recover_bed_valid_minutes;
    static Integer settings_recover_bed_use_count_limit;
    static Integer settings_recover_bed_title_stay;
    static String messages_recover_bed_destroy_tips;
    static String messages_recover_bed_success_title;
    static String messages_recover_bed_success_subtitle;
    static String messages_recover_bed_success_msg;
    static String messages_recover_bed_invalid_msg;
    static String messages_recover_bed_failed_msg;
    static String messages_recover_bed_cant_buy_alive;
    static String messages_recover_bed_cant_buy_invalid;
    static String messages_recover_bed_limited;
    static List<String> messages_recover_bed_success_broadcast;

    public static void init(boolean enable, String material, String section) {
        recover_bed_material = material;
        recover_bed_section = section;
        settings_recover_bed_enable = enable;
    }

    public static void loadConfig(YamlConfiguration cfg) {
        settings_recover_bed_recover_sound = cfg.getString("settings.recover_bed.recover_sound");
        settings_recover_bed_valid_minutes = cfg.getInt("settings.recover_bed.valid_minutes");
        settings_recover_bed_use_count_limit = cfg.getInt("settings.recover_bed.use_count_limit");
        settings_recover_bed_title_stay = cfg.getInt("settings.recover_bed.title_stay");

        messages_recover_bed_destroy_tips = cfg.getString("messages.recover_bed_destroy_tips").replace("&", "§");
        messages_recover_bed_success_title = cfg.getString("messages.recover_bed_success_title").replace("&", "§");
        messages_recover_bed_success_subtitle = cfg.getString("messages.recover_bed_success_subtitle").replace("&", "§");
        messages_recover_bed_success_msg = cfg.getString("messages.recover_bed_success_msg").replace("&", "§");
        messages_recover_bed_success_broadcast = cfg.getStringList("messages.recover_bed_success_broadcast");
        messages_recover_bed_success_broadcast.replaceAll(s -> s.replace("&", "§"));
        messages_recover_bed_invalid_msg = cfg.getString("messages.recover_bed_invalid").replace("&", "§");
        messages_recover_bed_failed_msg = cfg.getString("messages.recover_bed_failed").replace("&", "§");
        messages_recover_bed_cant_buy_alive = cfg.getString("messages.recover_bed_cant_buy_alive").replace("&", "§");
        messages_recover_bed_cant_buy_invalid = cfg.getString("messages.recover_bed_cant_buy_invalid").replace("&", "§");
        messages_recover_bed_limited = cfg.getString("messages.recover_bed_limited").replace("&", "§");
    }

    public static boolean handleBlockPlace(Block block) {
        return block.getType().toString().contains("BED");
    }

    public static void handleBedDestroy(IArena arena, ITeam team) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60) && (!limit_use_map.containsKey(team) || limit_use_map.get(team) < settings_recover_bed_use_count_limit)) {
                    ProviderUtil.sendTeamMessage(team, messages_recover_bed_destroy_tips);
                } else {
                    ProviderUtil.sendTeamMessage(team, messages_recover_bed_invalid_msg);
                }
            }
        }.runTaskLater(_233BedWars.getInstance(), 1L);
    }

    public static boolean handleShopBuy(Player player, IArena arena, ICategoryContent content) {
        if (content.getIdentifier().contains("recover_bed")) {
            ITeam team = arena.getTeam(player);
            if (!team.isBedDestroyed()) {
                player.sendMessage(messages_recover_bed_cant_buy_alive);
                return true;
            }
            if (!ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60)) {
                player.sendMessage(messages_recover_bed_cant_buy_invalid);
                return true;
            }

            for (IContentTier tier : content.getContentTiers()) {
                for (IBuyItem buyItem : tier.getBuyItemsList()) {
                    ItemStack itemStack = buyItem.getItemStack().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(Language.getMsg(player, RecoverBed.recover_bed_section + "-name").replace("{color}", "§e"));
                    itemStack.setItemMeta(itemMeta);
                    buyItem.setItemStack(itemStack);
                }
            }
        }
        return false;
    }

    public static boolean handleItemInteract(Player player, ItemStack item, IArena arena, ITeam team) {
        if (isRecoverBed(player, item)) {
            if (!ShopItemAddon.checkCooling(player, "recover_bed")) {
                return recoverBed(player, item, arena, team);
            }
            return true;
        }
        return false;
    }

    private static boolean isRecoverBed(Player player, ItemStack item) {
        // ...高版本下红色床会识别成BED_BLOCK
        return (item.getType().toString().equals(recover_bed_material) || item.getType().toString().replace("_BLOCK", "").equals(recover_bed_material.replace("LEGACY_", ""))) && ShopItemAddon.compareAddonItem(player, item, recover_bed_section);
    }

    static HashMap<ITeam, Integer> limit_use_map = new HashMap<>();

    static HashMap<ITeam, List<Object>> beds = new HashMap<>();

    public static void initArena(IArena arena) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (ITeam team : arena.getTeams()) {
                    if (team.isBedDestroyed()) {
                        continue;
                    }

                    Block bed_block = team.getBed().getBlock();
                    Location bed_loc = bed_block.getLocation();

                    Location[] directions = {
                            bed_loc.clone().add(1, 0, 0),  // East
                            bed_loc.clone().add(-1, 0, 0), // West
                            bed_loc.clone().add(0, 0, 1),  // South
                            bed_loc.clone().add(0, 0, -1), // North
                    };
                    for (Location loc : directions) {
                        Block ano_bed = loc.getBlock();
                        if (isBedBlock(ano_bed)) {
                            if (isModernApi()) {
                                Object[] captured = captureModernBed(bed_block);
                                if (captured != null) {
                                    // MODERN - ["MODERN", footLoc, facingName, material]
                                    beds.put(team, Arrays.asList("MODERN", captured[0], captured[1], captured[2]));
                                    break;
                                }
                            } else {
                                // LEGACY - [footData, otherLoc, otherData]
                                beds.put(team, Arrays.asList(
                                        bed_loc.getBlock().getState().getData(),
                                        loc,
                                        loc.getBlock().getState().getData()
                                ));
                                break;
                            }
                        }
                    }
                }
            }
        }.runTaskLater(_233BedWars.getInstance(), 20L);
    }


    public static void resetArena(IArena arena) {
        for (ITeam team : arena.getTeams()) {
            limit_use_map.remove(team);
            beds.remove(team);
        }
    }

    private static boolean recoverBed(Player player, ItemStack item, IArena arena, ITeam team) {
        if (team.isBedDestroyed()) {
            if (limit_use_map.containsKey(team)) {
                if (limit_use_map.get(team) >= settings_recover_bed_use_count_limit) {
                    player.sendMessage(messages_recover_bed_limited);
                    return false;
                }
            }
            if (ShopItemAddon.isBeforeInstant(arena.getStartTime(), settings_recover_bed_valid_minutes * 60)) {
                ProviderUtil.sendTeamMessage(team, messages_recover_bed_success_msg);
                ProviderUtil.sendTeamTitle(team,
                        messages_recover_bed_success_title
                                .replace("{player}", player.getDisplayName()),
                        messages_recover_bed_success_subtitle
                                .replace("{player}", player.getDisplayName()),
                        0, settings_recover_bed_title_stay * 20, 0);
                for (String msg : messages_recover_bed_success_broadcast) {
                    ProviderUtil.sendGlobalMessage(arena, msg
                            .replace("{player}", player.getDisplayName())
                            .replace("{tColor}", PlaceholderUtil.getTeamColor(team))
                            .replace("{tName}", PlaceholderUtil.getTeamName(team, player)));
                }
                String[] _sound = settings_recover_bed_recover_sound.split(":");
                ProviderUtil.playTeamSound(team, Sound.valueOf(_sound[0]), Float.parseFloat(_sound[1]), Float.parseFloat(_sound[2]));
                placeBed(team);
                team.getArena().getPlayers().forEach(p -> team.destroyBedHolo(p));
                ShopItemAddon.consumeItem(player, item, 1);
                ShopItemAddon.setCooling(player, "recover_bed");
                team.setBedDestroyed(false);
                if (!limit_use_map.containsKey(team)) {
                    limit_use_map.put(team, 1);
                } else {
                    limit_use_map.put(team, limit_use_map.get(team) + 1);
                }
                return true;
            } else {
                player.sendMessage(messages_recover_bed_failed_msg);
                return false;
            }
        }
        return false;
    }

    private static void placeBed(ITeam team) {
        List<Object> bed_data = beds.get(team);
        if (bed_data == null || bed_data.isEmpty()) return;

        // MODERN - ["MODERN", Location footLoc, String facingName, String materialName]
        if (bed_data.get(0) instanceof String && "MODERN".equals(bed_data.get(0))) {
            try {
                Location footLoc = (Location) bed_data.get(1);
                BlockFace facing = BlockFace.valueOf((String) bed_data.get(2));
                Material material = (Material) bed_data.get(3);

                Block foot = footLoc.getBlock();
                Block head = foot.getRelative(facing);

                setTypeCompat(foot, material, false);
                setTypeCompat(head, material, false);

                setModernBedData(foot, "FOOT", facing, false);
                setModernBedData(head, "HEAD", facing, true);
            } catch (Throwable t) {
                LogUtil.consoleLog("&9233BedWars &3&l> &e[ShopItemAddon#RecoverBed] &c设置床属性失败！");
                t.printStackTrace();
            }
            return;
        }

        // LEGACY - [MaterialData footData, Location otherLoc, MaterialData otherData]
        Location bed_loc = team.getBed();
        Block bed_1 = bed_loc.getBlock();
        bed_1.setType(Material.BED_BLOCK);
        BlockState bed_state_1 = bed_1.getState();
        bed_state_1.setData((MaterialData) bed_data.get(0));
        bed_state_1.update(true, false);

        Block bed_2 = ((Location) bed_data.get(1)).getBlock();
        bed_2.setType(Material.BED_BLOCK);
        BlockState bed_state_2 = bed_2.getState();
        bed_state_2.setData((MaterialData) bed_data.get(2));
        bed_state_2.update(true, false);
    }


    private static boolean isModernApi() {
        try {
            Class.forName("org.bukkit.block.data.type.Bed");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean hasSetTypeWithPhysics() {
        try {
            Block.class.getMethod("setType", Material.class, boolean.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static void setTypeCompat(Block b, Material m, boolean applyPhysics) {
        try {
            if (hasSetTypeWithPhysics()) {
                Block.class.getMethod("setType", Material.class, boolean.class).invoke(b, m, applyPhysics);
            } else {
                // 旧服只有 setType(Material)
                b.setType(m);
            }
        } catch (Exception ex) {
            // 回退
            b.setType(m);
        }
    }

    private static boolean isBedBlock(Block block) {
        String name = block.toString();
        return name.contains("BED"); // 1.8: BED_BLOCK/BED; 新版: RED_BED 等
    }

    private static Object[] captureModernBed(Block anyBedBlock) {
        try {
            Object blockData = Block.class.getMethod("getBlockData").invoke(anyBedBlock);
            Class<?> bedIntf = Class.forName("org.bukkit.block.data.type.Bed");
            if (!bedIntf.isInstance(blockData)) return null;

            BlockFace facing = (BlockFace) bedIntf.getMethod("getFacing").invoke(blockData);
            Object partEnum = bedIntf.getMethod("getPart").invoke(blockData); // Bed.Part
            String partName = String.valueOf(partEnum); // "HEAD" or "FOOT"

            Block footBlock = partName.equals("HEAD")
                    ? anyBedBlock.getRelative(facing.getOppositeFace())
                    : anyBedBlock;

            Material material = getModernBedMaterial(anyBedBlock); // 例如 "RED_BED"
            return new Object[]{footBlock.getLocation(), facing.name(), material};
        } catch (Throwable t) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static void setModernBedData(Block target, String part /*FOOT/HEAD*/, BlockFace facing, boolean applyPhysics) throws Exception {
        Object data = Block.class.getMethod("getBlockData").invoke(target);
        Class<?> bedIntf = Class.forName("org.bukkit.block.data.type.Bed");
        Class<?> partEnum = Class.forName("org.bukkit.block.data.type.Bed$Part");
        Class<?> blockDataIntf = Class.forName("org.bukkit.block.data.BlockData");
        if (!bedIntf.isInstance(data)) {
            // 某些服务端需要先用 Bukkit.createBlockData(material) 来拿到正确类型
            try {
                Material mat = target.getType();
                Object newData = Class.forName("org.bukkit.Bukkit")
                        .getMethod("createBlockData", Material.class)
                        .invoke(null, mat);
                data = newData;
            } catch (Throwable ignored) {
            }
        }
        // data.setFacing(facing)
        bedIntf.getMethod("setFacing", BlockFace.class).invoke(data, facing);
        // data.setPart(Bed.Part.valueOf(part))
        Object enumPart = Enum.valueOf((Class<Enum>) partEnum, part);
        bedIntf.getMethod("setPart", partEnum).invoke(data, enumPart);
        // block.setBlockData(data, applyPhysics)
        Block.class.getMethod("setBlockData", blockDataIntf, boolean.class).invoke(target, data, applyPhysics);
    }

    private static Material getModernBedMaterial(Block block) {
        try {
            Object bd = Block.class.getMethod("getBlockData").invoke(block);
            try {
                Class<?> bdIntf = Class.forName("org.bukkit.block.data.BlockData");
                Object mat = bdIntf.getMethod("getMaterial").invoke(bd);
                if (mat instanceof Material) return (Material) mat;
            } catch (Throwable ignored) {
            }
            Class<?> bdIntf = Class.forName("org.bukkit.block.data.BlockData");
            String as = (String) bdIntf.getMethod("getAsString").invoke(bd);
            String key = as.split("\\[", 2)[0];
            int colon = key.indexOf(':');
            String matName = (colon >= 0 ? key.substring(colon + 1) : key).toUpperCase();
            return Material.valueOf(matName);
        } catch (Throwable t) {
            try {
                return Material.valueOf("WHITE_BED");
            } catch (Throwable ignored) {
                return Material.BED;
            }
        }
    }
}
