package xyz.n7mn.dev.nanamiplayersystem;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventListener implements Listener {
    private final NanamiPlayerSystem plugin;
    private Map<UUID, String> kickData = new HashMap<>();
    private String[] SetPermList;
    private String[] SetPermPlayerList;

    public EventListener(NanamiPlayerSystem plugin) {
        this.plugin = plugin;
        this.SetPermList = plugin.getConfig().getString("ServerOPPermList").split(",");
        this.SetPermPlayerList = plugin.getConfig().getString("ServerOPPlayerList").split(",");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent e){

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerJoinEvent (PlayerJoinEvent e){

        String permDisplayName = "";
        String permName = "";

        e.getPlayer().setOp(false);

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

        String opMessage = ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("Prefix")) + ChatColor.RESET+" " + e.getPlayer().getName() + "さんが入室しました。 (権限: " + permDisplayName + ")";
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
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("Prefix")) +"&f "+e.getPlayer().getName()+"さんが退出しました。 (権限: "+permName+")");
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
