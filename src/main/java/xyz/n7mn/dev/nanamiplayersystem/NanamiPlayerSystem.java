package xyz.n7mn.dev.nanamiplayersystem;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.n7mn.dev.nanamiplayersystem.data.ServerPermData;

import java.sql.*;
import java.util.Enumeration;
import java.util.UUID;

public final class NanamiPlayerSystem extends JavaPlugin {

    private Connection con;
    private ServerPermData serverData;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        try {
            boolean found = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();

            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.equals(new com.mysql.cj.jdbc.Driver())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }

            con = DriverManager.getConnection("jdbc:mysql://" + getConfig().getString("MySQLServer") + ":" + getConfig().getInt("MySQLPort") + "/" + getConfig().getString("MySQLDatabase") + getConfig().getString("MySQLOption"), getConfig().getString("MySQLUsername"), getConfig().getString("MySQLPassword"));
            con.setAutoCommit(true);

            PreparedStatement statement = con.prepareStatement("SELECT * FROM ServerPermList WHERE ServerName = ? AND Is_Active = 1");
            statement.setString(1, getConfig().getString("ServerName"));
            ResultSet set = statement.executeQuery();
            if (set.next()){
                serverData = new ServerPermData(UUID.fromString(set.getString("UUID")),set.getString("ServerName"), set.getString("ServerDefaultJoinPerm").split(","), set.getString("ServerDefaultPerm").split(","), set.getBoolean("Is_Active"));

                set.close();
                statement.close();
            } else {
                serverData = new ServerPermData(UUID.randomUUID(), getConfig().getString("ServerName"), new String[]{"Admin"}, new String[]{"Admin"}, true);
                PreparedStatement statement1 = con.prepareStatement("INSERT INTO `ServerPermList`(`UUID`, `ServerName`, `ServerDefaultJoinPerm`, `ServerDefaultPerm`, `Is_Active`) VALUES (?,?,?,?,?)");
                statement1.setString(1, serverData.getUuid().toString());
                statement1.setString(2, serverData.getServerName());
                StringBuffer sb = new StringBuffer();

                for (String str : serverData.getServerJoinPerm()){
                    sb.append(str);
                }
                statement1.setString(3, sb.toString());
                sb = new StringBuffer();

                for (String str : serverData.getServerOpPerm()){
                    sb.append(str);
                }

                statement1.setString(4, sb.toString());
                statement1.setBoolean(5, true);
                statement1.execute();
                statement1.close();
            }

            getServer().getPluginManager().registerEvents(new EventListener(this, con), this);
        } catch (SQLException ex){
            ex.printStackTrace();
            getPluginLoader().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (con != null){
            new Thread(()->{
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public ServerPermData getServerData(){
        return serverData;
    }
}
