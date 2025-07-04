package cn.serendipityr._233bedwars.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NMSUtil {
    public static String nmsVersion;
    private static Method CACHE_PLAYER_GET_HANDLE;
    private static Method CACHE_ARMOR_STAND_GET_HANDLE;
    private static Field CACHE_PLAYER_PLAYER_CONNECTION;
    private static Method CACHE_SEND_PACKET;
    private static Class<?> CACHE_ENTITY_TELEPORT;

    public static void init() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        nmsVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static String getServerVersion() {
        return nmsVersion;
    }

    public static Object getNMSPlayer(Player player) {
        try {
            if (CACHE_PLAYER_GET_HANDLE == null) {
                CACHE_PLAYER_GET_HANDLE = player.getClass().getMethod("getHandle");
            }
            return CACHE_PLAYER_GET_HANDLE.invoke(player);
        } catch (Exception e) {
            LogUtil.consoleLog("&9233BedWars &3&l> &c获取NMSPlayer时发生错误！&7(" + nmsVersion + ")");
            e.printStackTrace();
            return null;
        }
    }

    public static Object getNMSArmorStand(ArmorStand armorStand) {
        try {
            if (CACHE_ARMOR_STAND_GET_HANDLE == null) {
                CACHE_ARMOR_STAND_GET_HANDLE = armorStand.getClass().getMethod("getHandle");
            }
            return CACHE_ARMOR_STAND_GET_HANDLE.invoke(armorStand);
        } catch (Exception e) {
            LogUtil.consoleLog("&9233BedWars &3&l> &c获取NMSArmorStand时发生错误！&7(" + nmsVersion + ")");
            e.printStackTrace();
            return null;
        }
    }

    public static Object getNMSPlayerConnection(Object nmsPlayer) {
        try {
            if (CACHE_PLAYER_PLAYER_CONNECTION == null) {
                CACHE_PLAYER_PLAYER_CONNECTION = nmsPlayer.getClass().getField("playerConnection");
            }
            return CACHE_PLAYER_PLAYER_CONNECTION.get(nmsPlayer);
        } catch (Exception a) {
            try {
                if (CACHE_PLAYER_PLAYER_CONNECTION == null) {
                    CACHE_PLAYER_PLAYER_CONNECTION = nmsPlayer.getClass().getField("b");
                }
                return CACHE_PLAYER_PLAYER_CONNECTION.get(nmsPlayer);
            } catch (Exception b) {
                try {
                    if (CACHE_PLAYER_PLAYER_CONNECTION == null) {
                        CACHE_PLAYER_PLAYER_CONNECTION = nmsPlayer.getClass().getField("c");
                    }
                    return CACHE_PLAYER_PLAYER_CONNECTION.get(nmsPlayer);
                } catch (Exception c) {
                    LogUtil.consoleLog("&9233BedWars &3&l> &c获取NMSPlayerConnection时发生错误！&7(" + nmsVersion + ")");
                    c.printStackTrace();
                    return null;
                }
            }
        }
    }

    public static void sendPacket(Object playerConnection, Object packet) {
        try {
            if (CACHE_SEND_PACKET == null) {
                CACHE_SEND_PACKET = playerConnection.getClass().getMethod("sendPacket", packet.getClass().getInterfaces());
            }
            CACHE_SEND_PACKET.invoke(playerConnection, packet);
        } catch (Exception a) {
            try {
                if (CACHE_SEND_PACKET == null) {
                    CACHE_SEND_PACKET = playerConnection.getClass().getMethod("a", packet.getClass().getInterfaces());
                }
                CACHE_SEND_PACKET.invoke(playerConnection, packet);
            } catch (Exception e) {
                LogUtil.consoleLog("&9233BedWars &3&l> &c发送NMS数据包时发生错误！&7(" + nmsVersion + ")");
                e.printStackTrace();
            }
        }
    }

    public static Object getEntityTeleportPacket(Object entity) {
        String[] teleport = {"PacketPlayOutEntityTeleport", "ClientboundTeleportEntityPacket"};
        if (CACHE_ENTITY_TELEPORT == null) {
            Class<?> packetClass = findClass(teleport);
            if (packetClass == null) {
                LogUtil.consoleLog("&9Pilgrimage &3&l> &c获取NMS数据包时发生错误：实体传送。&7(" + nmsVersion + ")");
                return null;
            }
            CACHE_ENTITY_TELEPORT = packetClass;
        }

        try {
            Constructor<?> packetConstructor = CACHE_ENTITY_TELEPORT.getConstructor(entity.getClass().getSuperclass().getSuperclass());
            return packetConstructor.newInstance(entity);
        } catch (Exception e) {
            LogUtil.consoleLog("&9Pilgrimage &3&l> &c获取NMS数据包时发生错误：实体传送。&7(" + nmsVersion + ")");
            e.printStackTrace();
            return null;
        }
    }

    public static Object getEntityTeleportPacket(int entityId, int x, int y, int z, byte yaw, byte pitch, boolean onGround) {
        String[] teleport = {"PacketPlayOutEntityTeleport", "ClientboundTeleportEntityPacket"};
        if (CACHE_ENTITY_TELEPORT == null) {
            Class<?> packetClass = findClass(teleport);
            if (packetClass == null) {
                LogUtil.consoleLog("&9233BedWars &3&l> &c获取NMS数据包时发生错误：实体传送。&7(" + nmsVersion + ")");
                return null;
            }
            CACHE_ENTITY_TELEPORT = packetClass;
        }

        try {
            Constructor<?> packetConstructor = CACHE_ENTITY_TELEPORT.getConstructor(int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class);
            return packetConstructor.newInstance(entityId, x, y, z, yaw, pitch, onGround);
        } catch (Exception e) {
            LogUtil.consoleLog("&9233BedWars &3&l> &c获取NMS数据包时发生错误：实体传送。&7(" + nmsVersion + ")");
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> findClass(String[] names) {
        Class<?> packetClass = null;
        for (String s : names) {
            try {
                packetClass = Class.forName("net.minecraft.network.protocol.game." + s);
            } catch (Exception ignored) {
            }
            try {
                packetClass = Class.forName("net.minecraft.world.entity.decoration." + s);
            } catch (Exception ignored) {
            }
            try {
                packetClass = Class.forName("net.minecraft.server." + nmsVersion + "." + s);
            } catch (Exception ignored) {
            }
            if (packetClass != null) {
                break;
            }
        }
        return packetClass;
    }
}
