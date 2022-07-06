package xyz.n7mn.dev.nanamiplayersystem;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.n7mn.dev.nanamiplayersystem.command.Join;
import xyz.n7mn.dev.nanamiplayersystem.data.ProtocolVersion;

import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

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

        getCommand("join").setExecutor(new Join(this));
        getCommand("join").setTabCompleter(new Join(this));

        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {

                Date NowDate = new Date();
                Calendar NowDateCalendar = Calendar.getInstance();
                NowDateCalendar.setTime(NowDate);
                NowDateCalendar.set(Calendar.SECOND, 0);

                List<String> list1 = getConfig().getStringList("ServerOpenTime1");
                List<String> list2 = getConfig().getStringList("ServerOpenTime2");

                int i = NowDateCalendar.get(Calendar.DAY_OF_WEEK) - 1;
                //System.out.println(i);
                String OpenTime1 = list1.get(i);
                String OpenTime2 = list2.get(i);

                if (OpenTime1.length() == 0){
                    OpenTime1 = "00:00";
                }
                if (OpenTime2.length() == 0){
                    OpenTime2 = "23:59";
                }

                Calendar OpenTimeInstance1 = Calendar.getInstance();
                Calendar OpenTimeInstance2 = Calendar.getInstance();

                OpenTimeInstance1.setTime(NowDate);
                OpenTimeInstance1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(OpenTime1.split(":")[0]));
                OpenTimeInstance1.set(Calendar.MINUTE, Integer.parseInt(OpenTime1.split(":")[1]));
                OpenTimeInstance1.set(Calendar.SECOND, 0);
                OpenTimeInstance2.setTime(NowDate);
                OpenTimeInstance2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(OpenTime2.split(":")[0]));
                OpenTimeInstance2.set(Calendar.MINUTE, Integer.parseInt(OpenTime2.split(":")[1]));
                OpenTimeInstance2.set(Calendar.SECOND, 0);

                if (OpenTimeInstance2.getTime().getTime() > NowDate.getTime()){
                    OpenTimeInstance1.set(Calendar.DAY_OF_MONTH, NowDateCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                    if (i <= 5){
                        OpenTime1 = list1.get(NowDateCalendar.get(Calendar.DAY_OF_WEEK));
                        OpenTime2 = list2.get(NowDateCalendar.get(Calendar.DAY_OF_WEEK));
                    } else {
                        OpenTime1 = list1.get(0);
                        OpenTime2 = list2.get(0);
                    }
                    if (OpenTime1.length() == 0){
                        OpenTime1 = "00:00";
                    }
                    if (OpenTime2.length() == 0){
                        OpenTime2 = "00:00";
                    }

                    OpenTimeInstance1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(OpenTime1.split(":")[0]));
                    OpenTimeInstance1.set(Calendar.MINUTE, Integer.parseInt(OpenTime1.split(":")[1]));
                    OpenTimeInstance1.set(Calendar.SECOND, 0);
                    OpenTimeInstance2.setTime(NowDate);
                    OpenTimeInstance2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(OpenTime2.split(":")[0]));
                    OpenTimeInstance2.set(Calendar.MINUTE, Integer.parseInt(OpenTime2.split(":")[1]));
                    OpenTimeInstance2.set(Calendar.SECOND, 0);
                }

                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                //System.out.println(format.format(NowDateCalendar.getTime()) + " / " + format.format(OpenTimeInstance1.getTime())+ " / " + format.format(OpenTimeInstance2.getTime()));

                Date date1 = OpenTimeInstance1.getTime();
                Date date2 = OpenTimeInstance2.getTime();

                if (NowDateCalendar.get(Calendar.HOUR_OF_DAY) == OpenTimeInstance1.get(Calendar.HOUR_OF_DAY) && NowDateCalendar.get(Calendar.MINUTE) == OpenTimeInstance1.get(Calendar.MINUTE)){
                    getConfig().set("JoinCheck", false);
                    getLogger().info(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix")) + ChatColor.RESET + " 指定時間("+format.format(date1)+")になったため自動開放をしました。");
                    this.cancel();
                    return;
                }

                if (NowDateCalendar.get(Calendar.HOUR_OF_DAY) == OpenTimeInstance2.get(Calendar.HOUR_OF_DAY) && NowDateCalendar.get(Calendar.MINUTE) == OpenTimeInstance2.get(Calendar.MINUTE)){
                    getConfig().set("JoinCheck", false);
                    getLogger().info(ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix")) + ChatColor.RESET + " 指定時間("+format.format(date2)+")になったため自動開放をしました。");
                    this.cancel();
                }

            }
        };

        if (getConfig().getBoolean("ServerAutoOpen")){
            bukkitRunnable.runTaskTimerAsynchronously(this, 0L, 20L);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

}
