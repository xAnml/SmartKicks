package net.anmlmc.smartkicks;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/*******************
 * Created by Anml *
 *******************/
public class HubCommand extends Command {

    private SmartKicks instance;

    public HubCommand(SmartKicks instance) {
        super("hub");
        this.instance = instance;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new ComponentBuilder("This command can only be executed by a player.").color(ChatColor.RED).create());
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (instance.getLobbies().size() == 0) {
            player.sendMessage(new ComponentBuilder("No lobbies were found on the network.").color(ChatColor.RED).create());
            return;
        }

        if (instance.getLobbies().contains(player.getServer().getInfo().getName())) {
            player.sendMessage(new ComponentBuilder("You are already connected to a lobby!").color(ChatColor.RED).create());
            return;
        }

        ServerInfo info = instance.execute();

        if (info == null) {
            player.sendMessage(new ComponentBuilder("No online lobbies were found on the network.").color(ChatColor.RED).create());
            return;
        }

        player.connect(info);
        player.sendMessage(new ComponentBuilder("You have been connected to lobby: ").color(ChatColor.GREEN)
                .append(info.getName()).color(ChatColor.AQUA).create());
    }
}
