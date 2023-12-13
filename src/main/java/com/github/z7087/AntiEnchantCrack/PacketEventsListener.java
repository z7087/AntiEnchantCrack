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
����һ���ħ̨����ʱ����������1.14+���ᷢ��һ��open_window���ݰ� containerIdΪ12
Ȼ��������ᷢ��window_property���ݰ� ����idΪ3�����ݰ���������ҵĸ�ħseed���ݵ�Ƭ�Σ�int��ֵ��ʹ��short�ضϣ�
���� ��ħseedΪ468479399 ��Ƭ��Ϊ28071 �����ƣ������ǰ���ֱ�Ϊ
00011011 11101100 01101101 10100111
                               01101101 10100111
��ħseedΪ881389686 ��Ƭ��Ϊ-3978 �����ƣ������ǰ���ֱ�Ϊ
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

    // ���Window_Property�� windowid��Ӧ��window�Ƿ�Ϊ��ħ̨ ��id�Ƿ�Ϊseed����1.19��3��
    // Ȼ���滻valueΪrandom��̶�ֵ
    // Ŀǰ��֪�������汾�ĸ�ħ̨id�Ƿ�һ�� ����1.19��12��

    // ���������ܿ��Է�����ħ�ƽ⣬���޷�����ʹ�õ��������������ƽ⣬��ʹ��Paper�������η����

    // �������ֻ�ܼ򵥵�ȡ�����ݰ�����Ϊgrim��HIGHEST���ȼ�ɾ�������ж�wrapper�ĸ���
    // ���Ҫ��seed���ĳɱ�ĵĻ��Ͱ���������ȼ��ĳ�MONITOR��Ȼ������value

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