package xyz.n7mn.dev.nanamiplayersystem;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public final class NanamiPlayerSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        try {

            boolean newLoad = false;
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()){
                Driver driver = drivers.nextElement();

                if (driver.equals(new com.mysql.cj.jdbc.Driver())){
                    newLoad = true;
                    break;
                }
            }

            if (newLoad){
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
        }


        getServer().getPluginManager().registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

}
