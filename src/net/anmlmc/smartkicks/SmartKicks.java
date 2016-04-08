package net.anmlmc.smartkicks;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*******************
 * Created by Anml *
 *******************/

public class SmartKicks extends Plugin {

    private static SmartKicks instance;
    private Configuration configuration = null;
    private List<String> lobbies = Lists.newArrayList();
    private List<String> keywords;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = getResourceAsStream("config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error! Was unable to create a configuration file.", e);
            }
        }
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> lobbies = configuration.getStringList("Lobbies");
        if (lobbies == null)
            lobbies = Lists.newArrayList();
        for (String lobby : lobbies) {
            if (BungeeCord.getInstance().getServerInfo(lobby) != null)
                this.lobbies.add(lobby);
        }

        keywords = configuration.getStringList("Keywords");
        if (keywords == null)
            keywords = Lists.newArrayList();

        ProxyServer.getInstance().getPluginManager().registerListener(this, new ServerEvents(this));
        getProxy().getPluginManager().registerCommand(this, new HubCommand(this));
    }

    @Override
    public void onDisable() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance = null;

    }

    public List<String> getLobbies() {
        return lobbies;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public ServerInfo execute() {

        List<String> localLobbies = new ArrayList<>(lobbies);
        Random random = new Random();
        final boolean[] usable = new boolean[1];
        usable[0] = false;

        while (true) {
            if (localLobbies.size() == 0)
                return null;

            int id = localLobbies.size() == 1 ? 0 : random.nextInt(localLobbies.size());
            ServerInfo temp = BungeeCord.getInstance().getServerInfo(localLobbies.get(id));

            try {
                Socket socket = new Socket(temp.getAddress().getAddress(), temp.getAddress().getPort());
                socket.close();
                usable[0] = true;
            } catch (Exception e) {
            }

            if (usable[0]) {
                temp.ping(new Callback<ServerPing>() {
                    @Override
                    public void done(ServerPing result, Throwable error) {
                        if (error != null || result.getPlayers().getOnline() >= result.getPlayers().getMax()) {
                            usable[0] = false;
                        }
                    }
                });
            }

            if (usable[0]) {
                return temp;
            } else {
                localLobbies.remove(temp.getName());
            }
        }

    }
}
