package com.github.z7087.AntiEnchantCrack;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.UserDisconnectEvent;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowProperty;
import org.bukkit.ChatColor;

import java.util.concurrent.ConcurrentHashMap;

/*
玩家右击附魔台方块时，服务器（1.14+）会发送一个open_window数据包 containerId为12
然后服务器会发送window_property数据包 其中id为3的数据包包含此玩家的附魔seed数据的片段（int数值，使用short截断）
例如 附魔seed为468479399 其片段为28071 二进制（大端在前）分别为
00011011 11101100 01101101 10100111
                               01101101 10100111
附魔seed为881389686 其片段为-3978 二进制（大端在前）分别为
00110100 10001000 11110000 01110110
                               11110000 01110110
*/

public class PacketEventsListener extends SimplePacketListenerAbstract {
    // maybe memory leak but idc
    public final ConcurrentHashMap<User, Integer> playerEnchantingMap = new ConcurrentHashMap<>();

    public PacketEventsListener() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onUserDisconnect(UserDisconnectEvent event) {
        User user = event.getUser();
        playerEnchantingMap.remove(user);
    }

    // 检查Window_Property包 windowid对应的window是否为附魔台 且id是否为seed（在1.19是3）
    // 然后替换value为random或固定值
    // 目前不知道各个版本的附魔台id是否一样 （在1.19是12）

    // 这个插件可能可以防御附魔破解，但无法防御使用掉落物的随机种子破解，请使用Paper及其下游服务端

    // 这个现在只能简单的取消数据包，因为grim在HIGHEST优先级删除了所有对wrapper的更改
    // 如果要把seed栏改成别的的话就把上面的优先级改成MONITOR，然后设置value

    // This is just a test, can be bypassed or just ignore the seed value in packet and use more time to crack it
    @Override
    public void onPacketPlaySend(PacketPlaySendEvent event) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_14))
            return;

        User user = event.getUser();
        if (event.getPacketType() == PacketType.Play.Server.WINDOW_PROPERTY) {
            WrapperPlayServerWindowProperty windowProperty = new WrapperPlayServerWindowProperty(event);

            int windowId = (int)windowProperty.getWindowId();

            if (windowId != 0 && windowId == playerEnchantingMap.getOrDefault(user, 0)) {
                int id = windowProperty.getId();

                //user.sendMessage(ChatColor.GOLD + "WindowProperty:  windowId="+windowId+" id="+id+" value="+windowProperty.getValue());

                if (id == 3) {
                    //windowProperty.setValue(69); // why dont work
                    event.setCancelled(true);
                    PacketEvents.getAPI().getProtocolManager().sendPacketSilently(user.getChannel(), new WrapperPlayServerWindowProperty(windowProperty.getWindowId(), id, windowId));
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow openWindow = new WrapperPlayServerOpenWindow(event);

            //user.sendMessage(ChatColor.GOLD + "OpenWindow:  windowId="+openWindow.getContainerId()+" type="+openWindow.getType()+" title="+openWindow.getTitle());

            int windowId = openWindow.getContainerId();
            if (windowId != 0) {
                if (openWindow.getType() == 12) {
                    playerEnchantingMap.put(user, openWindow.getContainerId());
                }
                else if (windowId == playerEnchantingMap.getOrDefault(user, 0)) {
                    playerEnchantingMap.put(user, 0);
                }
            }
        }

    }

}
