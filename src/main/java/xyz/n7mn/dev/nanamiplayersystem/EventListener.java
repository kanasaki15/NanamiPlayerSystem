package xyz.n7mn.dev.nanamiplayersystem;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventListener implements Listener {
    private final NanamiPlayerSystem plugin;
    private Map<UUID, String> kickData = new HashMap<>();

    private String[] JoinPermList;
    private String[] JoinPermPlayerList;

    private String[] SetPermList;
    private String[] SetPermPlayerList;

    private Map<Integer, String> protocolVersionList = new HashMap<>();

    public EventListener(NanamiPlayerSystem plugin) {
        this.plugin = plugin;
        this.SetPermList = plugin.getConfig().getString("ServerOPPermList").split(",");
        this.SetPermPlayerList = plugin.getConfig().getString("ServerOPPlayerList").split(",");
        this.JoinPermList = plugin.getConfig().getString("ServerJoinPermList").split(",");
        this.JoinPermPlayerList = plugin.getConfig().getString("ServerJoinPlayerList").split(",");

        try {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://gitlab.bixilon.de/bixilon/minosoft/-/raw/master/src/main/resources/assets/minosoft/mapping/versions.json?inline=false")
                    .build();
            Response response = client.newCall(request).execute();
            String json = response.body().string();

            JsonObject list = new Gson().fromJson(json, JsonObject.class);
            int i = 0;
            while (true){

                if (i > 1000){
                    return;
                }

                String s = String.valueOf(i);
                if (list.get(s) == null){
                    i++;
                    continue;
                }

                JsonElement element = list.get(s);
                JsonObject object = element.getAsJsonObject();
                if (object.get("protocol_id") == null){
                    protocolVersionList.put(i, object.get("name").getAsString());
                } else {
                    protocolVersionList.put(object.get("protocol_id").getAsInt(), object.get("name").getAsString());
                }
                i++;
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent e){

        if (!plugin.getConfig().getBoolean("JoinCheck")){
            return;
        }

        String permName = "User";

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption") , plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement = con.prepareStatement("SELECT UserList.*, RoleList.RoleName, RoleList.RoleDisplayName  FROM UserList, RoleList WHERE UserList.Active = 1 AND UserList.RoleUUID = RoleList.UUID AND UserList.MinecraftUserID = ?");
            statement.setString(1, e.getUniqueId().toString());

            ResultSet set = statement.executeQuery();
            if (set.next()){
                //permDisplayName = set.getString("RoleList.RoleDisplayName");
                permName = set.getString("RoleList.RoleName");
            }
            set.close();
            statement.close();

            con.close();
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        boolean isOK = false;

        if (JoinPermPlayerList.length != 0){
            for (String perm : JoinPermList){
                if (permName.toLowerCase().equals(perm.toLowerCase())){
                    isOK = true;
                    break;
                }
            }
        }

        if (JoinPermPlayerList.length != 0){
            for (String player : JoinPermPlayerList){
                if (player.equals(e.getName())){
                    isOK = true;
                    break;
                }

                try {
                    if (e.getUniqueId().equals(player)){
                        isOK = true;
                        break;
                    }
                } catch (Exception ex){
                    // なにもしないよ
                }
            }
        }

        if (!isOK){
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "" +
                    "--- ななみ鯖 ---\n" +
                    "現在あなたの権限では入室できません。\n" +
                    "必要権限: "+plugin.getConfig().getString("ServerOPPermList")+"\n" +
                    "あなたの権限: "+permName
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerJoinEvent (PlayerJoinEvent e){

        String permDisplayName = "一般";
        String permName = "User";

        try {
            Connection con = DriverManager.getConnection("jdbc:mysql://" + plugin.getConfig().getString("MySQLServer") + ":" + plugin.getConfig().getInt("MySQLPort") + "/" + plugin.getConfig().getString("MySQLDatabase") + plugin.getConfig().getString("MySQLOption") , plugin.getConfig().getString("MySQLUsername"), plugin.getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement = con.prepareStatement("SELECT UserList.*, RoleList.RoleName, RoleList.RoleDisplayName  FROM UserList, RoleList WHERE UserList.Active = 1 AND UserList.RoleUUID = RoleList.UUID AND UserList.MinecraftUserID = ?");
            statement.setString(1, e.getPlayer().getUniqueId().toString());

            ResultSet set = statement.executeQuery();
            if (set.next()){
                permDisplayName = set.getString("RoleList.RoleDisplayName");
                permName = set.getString("RoleList.RoleName");
            }
            set.close();
            statement.close();

            con.close();
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        if (plugin.getConfig().getBoolean("AutoOP")){
            e.getPlayer().setOp(false);
            boolean isSetOp = false;

            for (String p : SetPermList){
                if (permName.toLowerCase().equals(p.toLowerCase())){
                    e.getPlayer().setOp(true);
                    isSetOp = true;
                    break;
                }
            }

            if (!isSetOp){
                for (String player : SetPermPlayerList) {
                    if (e.getPlayer().getName().equals(player)){
                        e.getPlayer().setOp(true);
                        break;
                    }

                    try {
                        if (e.getPlayer().getUniqueId().equals(UUID.fromString(player))){
                            e.getPlayer().setOp(true);
                            break;
                        }
                    } catch (Exception ex){
                        // するー
                    }
                }
            }
        }


        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        int protocolVersion = manager.getProtocolVersion(e.getPlayer());

        String opMessage = ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("Prefix")) + ChatColor.RESET+" " + e.getPlayer().getName() + "さんが入室しました。 (Ver: "+protocolVersionList.get(protocolVersion)+" 権限: " + permDisplayName + ")";
        plugin.getLogger().info(opMessage);
        if (plugin.getConfig().getBoolean("SendOPMessage")){
            new Thread(()->{
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.isOp()){
                        player.sendMessage(opMessage);
                    }
                }

            }).start();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void d (PlayerKickEvent e){
        kickData.put(e.getPlayer().getUniqueId(), e.getReason());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerQuitEvent (PlayerQuitEvent e){
        if (e.getPlayer().isOp()){
            e.getPlayer().setOp(false);
        }

        String reason = kickData.get(e.getPlayer().getUniqueId());

        String permName = "";
        if (plugin.getConfig().getBoolean("SendOPMessage")){
            new Thread(()->{
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.isOp()){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("Prefix")) +ChatColor.RESET+" "+e.getPlayer().getName()+"さんが退出しました。 ");
                        if (reason != null && reason.length() > 0){
                            player.sendMessage("--- kick理由 ---\n"+reason+"\n---------");

                            kickData.remove(e.getPlayer().getUniqueId());
                        }
                    }
                }

            }).start();
        }
    }
}
