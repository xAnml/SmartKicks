package net.anmlmc.smartkicks;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/*******************
 * Created by Anml *
 *******************/

public class ServerEvents implements Listener {

    private SmartKicks instance;

    ServerEvents(SmartKicks instance) {
        this.instance = instance;
    }

    @EventHandler
    public void kickEvent(ServerKickEvent e) {

        ProxiedPlayer player = e.getPlayer();

        String reason = e.getKickReason();
        boolean containsKeywords = false;

        for (String word : instance.getKeywords()) {
            if (reason.toLowerCase().contains(word.toLowerCase()))
                containsKeywords = true;
        }

        BaseComponent[] component = new ComponentBuilder("Kicked from server with reason: ").color(ChatColor.RED)
                .append(e.getKickReason()).color(ChatColor.WHITE).create();

        if (containsKeywords || instance.getLobbies().size() == 0) {
            player.disconnect(component);
            return;
        }

        ServerInfo info = instance.execute();

        if (info == null) {
            player.disconnect(component);
            return;
        }

        e.setCancelServer(info);
        e.setCancelled(true);
        player.sendMessage(component);
    }
}
