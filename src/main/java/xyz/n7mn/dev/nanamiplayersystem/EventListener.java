package xyz.n7mn.dev.nanamiplayersystem;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class EventListener implements Listener {
    private final NanamiPlayerSystem plugin;
    private final Connection con;
    public EventListener(NanamiPlayerSystem plugin, Connection con) {
        this.plugin = plugin;
        this.con = con;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent e){
        String p = "User";
        try {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM `UserList`, `RoleList` WHERE UserList.RoleUUID = RoleList.UUID AND MinecraftUserID = ? AND UserList.Active = 1");
            statement.setString(1, e.getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()){
                p = set.getString("RoleName");
                set.close();
            }
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        if (plugin.getServerData().getServerJoinPerm().length == 1 && plugin.getServerData().getServerJoinPerm()[0].toLowerCase().equals("all")){
            e.allow();
            return;
        }

        boolean isOK = false;
        StringBuilder sb = new StringBuilder();

        for (String perm : plugin.getServerData().getServerJoinPerm()){
            if (perm.equals(p)){
                isOK = true;
            }
            sb.append(perm);
            sb.append(",");
        }

        if (isOK){
            e.allow();
        } else {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "" +
                    "--- ななみ鯖 ---\n" +
                    "あなたは今サーバーに入る権限がありません！\n" +
                    "必要な権限 : "+sb.substring(0, sb.length()-1)+"\n" +
                    "あなたの権限 : "+p
            );

            String finalP = p;
            new Thread(()->{
                for (Player player : plugin.getServer().getOnlinePlayers()){
                    if (player.isOp()){
                        player.sendMessage("" +
                                ChatColor.YELLOW+"[ななみ鯖] "+e.getName()+"さん (権限: "+ finalP +")が参加しようとしました。"
                        );
                    }
                }
            }).start();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerJoinEvent (PlayerJoinEvent e){
        String p = "User";
        try {
            PreparedStatement statement = con.prepareStatement("SELECT * FROM `UserList`, `RoleList` WHERE UserList.RoleUUID = RoleList.UUID AND MinecraftUserID = ? AND UserList.Active = 1");
            statement.setString(1, e.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()){
                p = set.getString("RoleName");
                set.close();
            }
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        boolean isOK = false;
        if (plugin.getServerData().getServerOpPerm().length == 1 && plugin.getServerData().getServerOpPerm()[0].toLowerCase().equals("all")) {
            isOK  = true;
        }


        for (String perm : plugin.getServerData().getServerJoinPerm()){
            if (perm.equals(p)){
                isOK = true;
                break;
            }
        }

        e.getPlayer().setOp(isOK);

        boolean skip = false;
        for (Plugin plugin : plugin.getServer().getPluginManager().getPlugins()){
            if (plugin.getName().matches(".*Survival.*")){
                skip = true;
                break;
            }
        }

        if (skip){
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()){
            if (player.isOp()){

                player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+e.getPlayer().getName()+"さんが参加しました。");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerQuitEvent (PlayerQuitEvent e){
        e.getPlayer().setOp(false);

        for (Player player : plugin.getServer().getOnlinePlayers()){
            if (player.isOp()){

                player.sendMessage(ChatColor.YELLOW+"[ななみ鯖] "+e.getPlayer().getName()+"さんが退出しました。");
            }
        }
    }
}
