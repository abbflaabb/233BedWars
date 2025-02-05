package cn.serendipityr._233bedwars.addons.shopItems;

import cn.serendipityr._233bedwars.addons.ShopItemAddon;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class LuckyBlock {
    public static String lucky_block_material;
    public static String lucky_block_section;
    public static Boolean settings_lucky_block_enable = false;
    public static HashMap<String, Double> settings_lucky_block_possibilities;
    public static String messages_lucky_block_lucky;
    public static String messages_lucky_block_unlucky;

    public static void init(boolean enable, String material, String section) {
        lucky_block_material = material;
        lucky_block_section = section;
        settings_lucky_block_enable = enable;
    }

    public static void loadConfig(YamlConfiguration cfg) {
        settings_lucky_block_possibilities = new HashMap<>();
        List<String> possibilities = cfg.getStringList("settings.lucky_block.possibilities");
        for (String str : possibilities) {
            String[] sp = separateString(str.replace("&", "§"));
            settings_lucky_block_possibilities.put(sp[1], Double.parseDouble(sp[0]));
        }
        messages_lucky_block_lucky = cfg.getString("messages.lucky_block_lucky").replace("&", "§");
        messages_lucky_block_unlucky = cfg.getString("messages.lucky_block_unlucky").replace("&", "§");
    }

    static List<Block> blocks = new ArrayList<>();

    public static boolean handleBlockPlace(Player player, Block block, ItemStack item) {
        if (isLuckyBlock(player, item)) {
            if (ShopItemAddon.checkCooling(player, "lucky_block")) {
                return true;
            }
            blocks.add(block);
            ShopItemAddon.setCooling(player, "lucky_block");
        }
        return false;
    }

    public static boolean handleBlockDestroy(Player player, Block block) {
        if (blocks.contains(block)) {
            block.setType(Material.AIR);
            parseExecute(random(), player, block.getLocation());
            return true;
        }

        return false;
    }

    private static String[] separateString(String input) {
        int colonIndex = input.indexOf(":");
        if (colonIndex == -1) {
            return new String[]{input, ""};
        }
        String firstPart = input.substring(0, colonIndex);
        String remainingPart = input.substring(colonIndex + 1);
        return new String[]{firstPart, remainingPart};
    }

    private static String random() {
        double totalWeight = settings_lucky_block_possibilities.values().stream().mapToDouble(Double::doubleValue).sum();
        double randomValue = new Random().nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        for (Map.Entry<String, Double> entry : settings_lucky_block_possibilities.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue <= cumulativeWeight) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static void parseExecute(String str, Player player, Location blockLoc) {
        String[] _p = separateString(str);
        if (_p[0].equals("LUCKY")) {
            player.sendMessage(messages_lucky_block_lucky);
        } else {
            player.sendMessage(messages_lucky_block_unlucky);
        }

        String[] p = _p[1].split(":");
        String execute = p[0];
        switch (execute) {
            case "ITEM":
                String name = p[1];
                String[] lores = p[2].split("#");
                String item = p[3];
                int amount = Integer.parseInt(p[4]);
                String[] enchantments = p[5].split("#");
                ItemStack itemStack = getItemStack(name, lores, item, amount, enchantments);
                blockLoc.getWorld().dropItem(blockLoc, itemStack);
                break;
            case "EFFECT":
                String effect = p[1];
                int level = Integer.parseInt(p[2]);
                int duration = Integer.parseInt(p[3]) * 20;
                boolean particle = Boolean.getBoolean(p[4]);
                boolean force = Boolean.getBoolean(p[5]);
                PotionEffect potionEffect = getEffect(effect, duration, level, particle);
                player.addPotionEffect(potionEffect, force);
                break;
            case "EXPLOSION":
                int power = Integer.parseInt(p[1]);
                boolean fire = Boolean.getBoolean(p[2]);
                boolean damage = Boolean.getBoolean(p[3]);
                blockLoc.getWorld().createExplosion(blockLoc.getX(), blockLoc.getY(), blockLoc.getZ(), power, fire, damage);
                break;
            case "TELEPORT":
                double x = Double.parseDouble(p[1]);
                double y = Double.parseDouble(p[2]);
                double z = Double.parseDouble(p[3]);
                player.teleport(player.getLocation().add(x, y, z));
                break;
            case "COMMAND":
                String cmd = p[1].replace("%player_name%", player.getDisplayName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                break;
            case "SAY":
                String chat = p[1].replace("%player_name%", player.getDisplayName());
                player.chat(chat);
                break;
        }
    }

    private static ItemStack getItemStack(String name, String[] lores, String type, int amount, String[] enchantments) {
        ItemStack itemStack = new ItemStack(Material.getMaterial(type));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(List.of(lores));
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(amount);
        for (String s : enchantments) {
            String[] _s = s.split("\\.");
            itemStack.addEnchantment(Enchantment.getByName(_s[0]), Integer.parseInt(_s[1]));
        }
        return itemStack;
    }

    private static PotionEffect getEffect(String effect, int duration, int level, boolean particle) {
        return new PotionEffect(PotionEffectType.getByName(effect), duration, level, particle);
    }

    private static boolean isLuckyBlock(Player player, ItemStack item) {
        return item.getType().toString().equals(lucky_block_material) && ShopItemAddon.compareAddonItem(player, item, lucky_block_section);
    }
}
