package cn.serendipityr._233bedwars.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class SkullUtil {
    static Field GAME_PROFILE_FIELD;
    static Method CREATE_PLAYER_PROFILE_METHOD;
    static Method SET_PLAYER_PROFILE_PROPERTY_METHOD;
    static Method SET_PLAYER_PROFILE_METHOD;

    public static void setSkullTexture(SkullMeta skullMeta, String texture) {
        if (texture.trim().isEmpty()) {
            return;
        }
        UUID uuid = UUID.randomUUID();
        Property prop = new Property("textures", texture);
        // Modern API (1.21+)
        try {
            if (CREATE_PLAYER_PROFILE_METHOD == null) {
                CREATE_PLAYER_PROFILE_METHOD = Bukkit.class.getMethod("createProfile", UUID.class, String.class);
            }
            Object playerProfile = CREATE_PLAYER_PROFILE_METHOD.invoke(null, uuid, "");
            if (SET_PLAYER_PROFILE_PROPERTY_METHOD == null) {
                SET_PLAYER_PROFILE_PROPERTY_METHOD = playerProfile.getClass().getMethod("setProperty", String.class, prop.getClass());
            }
            SET_PLAYER_PROFILE_PROPERTY_METHOD.invoke(playerProfile, "textures", prop);
            if (SET_PLAYER_PROFILE_METHOD == null) {
                for (Method m : skullMeta.getClass().getMethods()) {
                    if (m.getName().equals("setPlayerProfile") && m.getParameterCount() == 1) {
                        SET_PLAYER_PROFILE_METHOD = m;
                        SET_PLAYER_PROFILE_METHOD.setAccessible(true);
                        break;
                    }
                }
            }
            if (SET_PLAYER_PROFILE_METHOD == null) {
                LogUtil.consoleLog("&9233BedWars &3&l> &e[BedWarsShopUtil] &c头颅材质设置失败: SET_PLAYER_PROFILE_METHOD");
                return;
            }
            SET_PLAYER_PROFILE_METHOD.invoke(skullMeta, playerProfile);
        } catch (Exception e1) {
            // Legacy API
            try {
                GameProfile profile = new GameProfile(uuid, "");
                profile.getProperties().put("textures", prop);
                if (GAME_PROFILE_FIELD == null) {
                    GAME_PROFILE_FIELD = skullMeta.getClass().getDeclaredField("profile");
                    GAME_PROFILE_FIELD.setAccessible(true);
                }
                GAME_PROFILE_FIELD.set(skullMeta, profile);
            } catch (Exception e2) {
                LogUtil.consoleLog("&9233BedWars &3&l> &e[BedWarsShopUtil] &c头颅材质设置失败！");
                e2.printStackTrace();
            }
        }
    }
}
